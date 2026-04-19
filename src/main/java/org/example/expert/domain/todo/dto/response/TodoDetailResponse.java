package org.example.expert.domain.todo.dto.response;

public record TodoDetailResponse(
        String title,
        Long managerCount,
        Long totalCommentCount
) {
}
