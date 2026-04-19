package org.example.expert.aop.dto;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)           // 필드(변수)에만 붙일 수 있도록 제한
@Retention(RetentionPolicy.RUNTIME)  // 런타임까지 어노테이션 정보 유지 — 리플렉션으로 읽으려면 필수
public @interface Masking {
}