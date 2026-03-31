package org.example.expert.domain.comment.repository;

import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

//    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.todo.id = :todoId")
//    List<Comment> findByTodoIdWithUser(@Param("todoId") Long todoId);

    @Query("""
           SELECT new org.example.expert.domain.comment.dto.response.CommentResponse(
                      c.id, 
                      c.contents, 
                      new org.example.expert.domain.user.dto.response.UserResponse(u.id, u.email)
                      )
           FROM Comment c
           JOIN c.user u
           WHERE c.todo.id = :todoId
           """)
    List<CommentResponse> findByTodoIdWithUser(@Param("todoId") Long todoId);
}
