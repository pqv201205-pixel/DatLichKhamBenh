package com.example.demo.Security;

import com.example.demo.Entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtTokenProvider
 *
 * Sinh và xác thực JWT Access Token + Refresh Token.
 * Cấu hình trong application.yml:
 *
 *   app:
 *     jwt:
 *       secret: "chuoi-bi-mat-dai-it-nhat-32-ky-tu-!!"
 *       access-token-expiration: 900000        # 15 phút (ms)
 *       refresh-token-expiration: 604800000    # 7 ngày  (ms)
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long      accessTokenExpiration;
    private final long      refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration:900000}") long accessExp,
            @Value("${app.jwt.refresh-token-expiration:604800000}") long refreshExp) {

        this.secretKey              = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration  = accessExp;
        this.refreshTokenExpiration = refreshExp;
    }

    // ─── Sinh Access Token ────────────────────────────────────────────────────

    public String generateAccessToken(User user) {
        return buildToken(user, accessTokenExpiration, "ACCESS");
    }

    // ─── Sinh Refresh Token ───────────────────────────────────────────────────

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshTokenExpiration, "REFRESH");
    }

    // ─── Xác thực Token ──────────────────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT hết hạn: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT không được hỗ trợ: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT bị lỗi định dạng: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("JWT chữ ký không hợp lệ: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT chuỗi rỗng: {}", e.getMessage());
        }
        return false;
    }

    // ─── Lấy thông tin từ Token ───────────────────────────────────────────────

    public Integer getUserId(String token) {
        return Integer.valueOf(parseClaims(token).getSubject());
    }

    public String getUsername(String token) {
        return (String) parseClaims(token).get("username");
    }

    public String getRole(String token) {
        return (String) parseClaims(token).get("role");
    }

    public String getTokenType(String token) {
        return (String) parseClaims(token).get("type");
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private String buildToken(User user, long expiration, String type) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim("username", user.getUsername())
                .claim("role",     user.getRole())
                .claim("email",    user.getEmail())
                .claim("type",     type)            // phân biệt ACCESS vs REFRESH
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}