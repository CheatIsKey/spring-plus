package org.example.expert.domain.todo.dto.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record SearchCondition(
        String titleKeyword,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate startDate,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate endDate,

        String nicknameKeyword
) {
}
