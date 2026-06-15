package com.example.demo.Security;

import com.example.demo.DTOs.ResponseDTO.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JwtAuthEntryPoint
 *
 * Xử lý lỗi 401 Unauthorized — trả về JSON thay vì redirect HTML mặc định.
 * Được kích hoạt khi request không có token hoặc token không hợp lệ
 * mà truy cập vào endpoint yêu cầu xác thực.
 */
@Slf4j
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public void commence(HttpServletRequest  request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.warn("Truy cập trái phép: {} {} — {}",
                request.getMethod(), request.getRequestURI(), authException.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Object> body = ApiResponse.error(
                "Bạn chưa đăng nhập hoặc token không hợp lệ. Vui lòng đăng nhập lại.");

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}