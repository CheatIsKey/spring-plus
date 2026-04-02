package org.example.expert.domain.todo.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.QComment;
import org.example.expert.domain.manager.entity.QManager;
import org.example.expert.domain.todo.dto.request.SearchCondition;
import org.example.expert.domain.todo.dto.response.TodoDetailResponse;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.QUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.*;
import static org.example.expert.domain.manager.entity.QManager.*;
import static org.example.expert.domain.todo.entity.QTodo.*;
import static org.example.expert.domain.user.entity.QUser.*;

@RequiredArgsConstructor
public class TodoRepositoryCustomImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

//    @Query("SELECT t FROM Todo t " +
//            "LEFT JOIN t.user " +
//            "WHERE t.id = :todoId")
//    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {
        return Optional.ofNullable(queryFactory.selectFrom(todo)
                .leftJoin(todo.user).fetchJoin()
                .where(todo.id.eq(todoId))
                .fetchOne());
    }

    /**
     *  SELECT t.title,
     *         COUNT(DISTINCT m.id),
     *         COUNT(DISTINCT c.id)
     *  FROM todos t
     *  LEFT JOIN manager m ON t.id = m.todo_id
     *  LEFT JOIN users u ON m.user_id = u.id
     *  LEFT JOIN comment c ON t.id = c.todo_id
     *  WHERE t.title LIKE '%:titleKeyword%'
     *  AND (:startDate     IS NULL OR t.createdAt >= :startDate)
     *  AND (:endDate       IS NULL OR t.createdAt <= :endDate)
     *  AND (m.manager.name IS NULL OR m.manager.name LIKE '%:nicknameKeyword%')
     *  GROUP BY t.id, t.title, t.created_at
     *  ORDER BY t.created_at DESC;
     */
    @Override
    public Page<TodoDetailResponse> findAllBySearchCondition(Pageable pageable, SearchCondition condition) {
        BooleanExpression[] conditions = {
                titleKeywordCondition(condition.titleKeyword()),
                startDateCondition(condition.startDate()),
                endDateCondition(condition.endDate()),
                managerKeywordCondition(condition.nicknameKeyword())
        };

        List<TodoDetailResponse> content = queryFactory
                .select(Projections.constructor(
                        TodoDetailResponse.class,
                        todo.title,
                        manager.countDistinct(),
                        comment.countDistinct()
                )).from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .leftJoin(todo.comments, comment)
                .where(conditions)
                .groupBy(todo.id, todo.title, todo.createdAt)
                .orderBy(todo.createdAt.desc())
                .fetch();

        Long total = queryFactory
                .select(todo.countDistinct())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(conditions)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression titleKeywordCondition(String keyword) {
        return (keyword != null) ? todo.title.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression startDateCondition(LocalDate startDate) {
        return (startDate != null) ? todo.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    private BooleanExpression endDateCondition(LocalDate endDate) {
        return (endDate != null) ? todo.createdAt.loe(endDate.atTime(23, 59, 59)) : null;
    }

    private BooleanExpression managerKeywordCondition(String keyword) {
        if (keyword == null) return null;

        return JPAExpressions
                .selectOne()
                .from(manager)
                .join(manager.user, user)
                .where(
                        manager.todo.id.eq(todo.id),
                        user.nickname.containsIgnoreCase(keyword)
                )
                .exists();
    }
}
