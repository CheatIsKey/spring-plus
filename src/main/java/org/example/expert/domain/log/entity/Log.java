package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.entity.Timestamped;

@Entity
@Getter
@Table(name = "log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Log extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long todoId;
    private Long requestUserId;

    @Enumerated(EnumType.STRING)
    private Status result;

    private String failReason;

    public Log(Long todoId, Long requestUserId, Status result, String failReason) {
        this.todoId = todoId;
        this.requestUserId = requestUserId;
        this.result = result;
        this.failReason = failReason;
    }
}
