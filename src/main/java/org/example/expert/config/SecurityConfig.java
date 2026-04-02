package org.example.expert.config;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.auth.entity.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider provider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())        // JWT 사용 환경에서 CSRF 불필요
            .formLogin(form -> form.disable())   // 기본 폼 로그인 비활성화
            .httpBasic(basic -> basic.disable()) // HTTP Basic 인증 비활성화

            .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin())
            )

            // JWT = Stateless → 세션을 생성하지 않도록 설정
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth
                // 수정 포인트: 개발 편의 허용 경로 (운영 배포 전 검토 필요)
                .requestMatchers(
                        "/", "/assets/**", "/img/**", "/error", "/favicon.ico",
                        "/h2-console/**"
                ).permitAll()

                // 수정 포인트: 인증 없이 접근 가능한 API 경로 추가
                .requestMatchers(HttpMethod.POST,
                        "/auth/signin",
                        "/auth/signup"
                ).permitAll()

                .requestMatchers(HttpMethod.GET,
                        "/todos",
                        "/todos/{todoId}"
                ).permitAll()

                .requestMatchers(
                        "admin/**"
                ).hasAuthority("ROLE_ADMIN")

                // 위에서 허용한 경로 외 모든 요청은 인증 필수
                .anyRequest().authenticated()
            )

            // 우리가 만든 JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
            // 폼 로그인 처리 전에 JWT 인증을 먼저 완료시키기 위함
            .addFilterBefore(
                    new JwtAuthenticationFilter(provider),
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}