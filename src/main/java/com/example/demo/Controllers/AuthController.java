package com.example.demo.Controllers;

import com.example.demo.DTOs.RequestDTO.*;
import com.example.demo.DTOs.ResponseDTO.*;
import com.example.demo.Security.CustomUserDetails;
import com.example.demo.Services.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — Xác thực & Phân quyền ứng dụng đặt lịch khám
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Spring tự động inject triển khai của UserServiceImpl vào đây nhờ cơ chế Polymorphism
    private final UserService userService;

    // ── Đăng ký ───────────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        // Đồng bộ hóa: Thường đăng ký xong hệ thống thực tế sẽ trả về luôn cặp Token (AuthResponse)
        // để người dùng tự động đăng nhập vào app luôn mà không cần bắt họ gõ lại pass.
        AuthResponse auth = userService.register(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.success("Đăng ký tài khoản thành công", auth));
    }

    // ── Đăng nhập ─────────────────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse auth = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", auth));
    }

    // ── Làm mới token ─────────────────────────────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        // Đồng bộ hóa tên hàm truyền vào chuỗi String refreshToken
        AuthResponse auth = userService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Làm mới token thành công", auth));
    }

    // ── Đăng xuất ─────────────────────────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Lấy ID từ token bảo mật để vô hiệu hóa refresh token trên Redis
        userService.logout(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất khỏi hệ thống thành công", null));
    }

    // ── Đổi mật khẩu ─────────────────────────────────────────────────────────
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu tài khoản thành công", null));
    }

    // ── Quên mật khẩu — gửi OTP ──────────────────────────────────────────────
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        // Truyền cả object request vào service xử lý theo chuẩn Interface mới
        userService.forgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success("Hệ thống đã gửi mã OTP xác nhận về email: " + request.getEmail(), null));
    }

    // ── Đặt lại mật khẩu bằng OTP ────────────────────────────────────────────
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Khôi phục mật khẩu tài khoản thành công", null));
    }
}