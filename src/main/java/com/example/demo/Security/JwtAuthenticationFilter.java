package com.example.demo.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter
 *
 * Chạy một lần cho mỗi request (OncePerRequestFilter).
 *
 * Luồng xử lý:
 *  1. Đọc JWT từ header Authorization: Bearer <token>
 *  2. Validate token (chữ ký + hết hạn)
 *  3. Nạp UserDetails từ DB
 *  4. Set Authentication vào SecurityContext
 *     → các request tiếp theo trong cùng thread đã được xác thực
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider        jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {

                // Chỉ chấp nhận Access Token, không cho dùng Refresh Token để gọi API
                if ("REFRESH".equals(jwtTokenProvider.getTokenType(token))) {
                    log.warn("Refresh token bị dùng để gọi API — từ chối");
                    filterChain.doFilter(request, response);
                    return;
                }

                String      username    = jwtTokenProvider.getUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Xác thực thành công user={} uri={}",
                        username, request.getRequestURI());
            }

        } catch (Exception ex) {
            // Không throw — để SecurityContext rỗng, request bị chặn tại SecurityConfig
            log.error("Lỗi xác thực JWT: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Đọc token từ header Authorization.
     * Chấp nhận cả "Bearer <token>" lẫn token thô (hỗ trợ test Swagger).
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}