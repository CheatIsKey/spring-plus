package org.example.expert.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.log.entity.Status;
import org.example.expert.domain.log.service.LogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static org.example.expert.domain.log.entity.Status.*;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ManagerRegisterLogging {

    private final LogService logService;

    @Around("execution(* org.example.expert.domain.manager.controller.ManagerController.saveManager(..))")
    public Object managerRegisterLogging(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs();
        AuthUser authUser = (AuthUser) args[0];
        Long todoId = (Long) args[1];

        try {
            Object result = joinPoint.proceed();
            logService.save(todoId, authUser.getId(), SUCCESS, null);
            return result;
        } catch (Exception e) {
            logService.save(todoId, authUser.getId(), FAILURE, e.getMessage());
            throw e;
        }
    }
}
