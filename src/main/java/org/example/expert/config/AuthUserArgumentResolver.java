package org.example.expert.config;

import lombok.NonNull;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(Auth.class);
        boolean hasLoginUserType = AuthUser.class.isAssignableFrom(parameter.getParameterType());
        return hasAnnotation && hasLoginUserType;
    }

    //    @Override
//    public Object resolveArgument(
//            @Nullable MethodParameter parameter,
//            @Nullable ModelAndViewContainer mavContainer,
//            NativeWebRequest webRequest,
//            @Nullable WebDataBinderFactory binderFactory
//    ) {
//        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
//
//        // JwtFilter 에서 set 한 userId, email, userRole 값을 가져옴
//        Long userId = (Long) request.getAttribute("userId");
//        String email = (String) request.getAttribute("email");
//        UserRole userRole = UserRole.of((String) request.getAttribute("userRole"));
//
//        return new AuthUser(userId, email, userRole);
//    }
    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                  @Nullable ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest,
                                  @Nullable WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나 비로그인(anonymous) 상태이면 즉시 예외 발생
        if (authentication == null
                || authentication.getPrincipal().equals("anonymousUser")
                || !(authentication.getPrincipal() instanceof AuthUser)) {
            throw new InvalidRequestException("로그인이 필요한 서비스입니다.");
        }

        // JwtAuthenticationFilter에서 Principal에 저장한 AuthUser 객체를 그대로 반환
        return authentication.getPrincipal();
    }
}
