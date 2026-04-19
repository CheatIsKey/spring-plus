package org.example.expert.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String ISSUER = "MyAppAuthServer"; // 수정 포인트: 프로젝트 이름으로 변경

    @Value("${jwt.secret.key}")
    private String secretKeyString;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret-key는 최소 32바이트(256bit) 이상이어야 합니다.");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        log.info("HS256 대칭키가 성공적으로 초기화되었습니다.");
    }

    /** Access Token 발급 */
    public String createAccessToken(Long memberId, String nickname, String email, UserRole role) {
        return buildToken(memberId, nickname, email, role);
    }

    /**
     * 실제 JWT 생성 내부 메서드 — jjwt 0.12.x API
     *
     * 0.11.x → 0.12.x 변경 요약:
     *   setClaims()    → claims() 빌더 체이닝으로 대체
     *   setSubject()   → subject()
     *   setIssuer()    → issuer()
     *   setIssuedAt()  → issuedAt()
     *   setExpiration()→ expiration()
     *   setId()        → id()
     *   signWith(key, SignatureAlgorithm.HS256) → signWith(key) (알고리즘 자동 추론)
     */
    private String buildToken(Long memberId, String nickname, String email, UserRole role) {
        // 0.12.x: Jwts.builder()에서 직접 클레임 메서드를 체이닝
        var builder = Jwts.builder()
                .subject(String.valueOf(memberId)) // PK를 Subject로 지정
                .claim("nickname", nickname)
                .claim("email", email)
                .issuer(ISSUER)
                .id(UUID.randomUUID().toString());

        if (role != null) {
            builder.claim("role", role); // Custom Claim 추가
        }

        return builder
                .signWith(key) // 0.12.x: SecretKey 타입에서 알고리즘(HS256) 자동 추론
                .compact();
    }

    /** 토큰에서 회원 PK(Subject) 추출 */
    public Long getMemberId(String token) {
        // 0.12.x: parserBuilder() → parser() / parseClaimsJws() → parseSignedClaims() / getBody() → getPayload()
        String subject = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
        return Long.parseLong(subject);
    }

    /** 토큰에서 역할(role) 추출 — Refresh Token은 role이 없어 null 반환 가능 */
    public String getRole(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public String getEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("email", String.class);
    }

    /**
     * 토큰 유효성 검증 — 케이스 1, 2에서 사용 (예외를 내부에서 처리하고 boolean 반환)
     * 케이스 3에서는 이 메서드 대신 필터에서 try-catch로 직접 예외를 잡는다.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.warn("잘못된 JWT 서명입니다 (위조 의심): {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다: {}", e.getMessage());
        }
        return false;
    }
}