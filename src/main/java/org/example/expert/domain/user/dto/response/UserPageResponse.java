package org.example.expert.domain.user.dto.response;

import java.time.LocalDateTime;

public record UserPageResponse(
        Long userId,
        String nickname,
        String email,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

    public UserPageResponse from() {
        return new UserPageResponse(userId, nickname, email, createdAt, modifiedAt);
    }
}
