package com.example.demo.Services;

import com.example.demo.DTOs.RequestDTO.*;
import com.example.demo.DTOs.ResponseDTO.*;
import com.example.demo.Entities.User;
import com.example.demo.Enums.Role;
import com.example.demo.Exceptions.*;
import com.example.demo.Repositories.UserRepository;
import com.example.demo.Security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * UserService — Xác thực & Phân quyền (Tính năng 1)
 *
 * Trách nhiệm:
 *  - Đăng ký tài khoản (PATIENT / DOCTOR / ADMIN)
 *  - Đăng nhập & cấp JWT Access Token + Refresh Token
 *  - Xác thực & làm mới Refresh Token
 *  - Đổi mật khẩu (khi đã đăng nhập)
 *  - Quên mật khẩu (gửi OTP qua email, xác nhận OTP, reset password)
 *  - Audit Log: ghi nhận thao tác đăng nhập / đổi mật khẩu (Tính năng 16)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserRepository          userRepository;
    private final PasswordEncoder         passwordEncoder;
    private final JwtTokenProvider        jwtTokenProvider;
    private final AuthenticationManager   authenticationManager;
    private final EmailService            emailService;       // Tính năng 11
    private final AuditLogService         auditLogService;    // Tính năng 16
    private final RedisTokenService       redisTokenService;  // Tính năng 18 — cache refresh token

    // ─────────────────────────────────────────────
    // 1.1 ĐĂNG KÝ
    // ─────────────────────────────────────────────

    /**
     * Đăng ký tài khoản mới.
     * Role mặc định là PATIENT; ADMIN có thể tạo tài khoản DOCTOR.
     *
     * @param request RegisterRequest chứa username, email, password, role
     * @return UserResponse thông tin user vừa tạo
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Kiểm tra trùng username / email
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username đã tồn tại: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email đã được đăng ký: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.PATIENT)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(user);

        // Audit log — tính năng 16
        auditLogService.log(saved.getUserId(), "REGISTER", "Tạo tài khoản mới role=" + saved.getRole());

        log.info("Đăng ký thành công userId={} role={}", saved.getUserId(), saved.getRole());
        return UserResponse.from(saved);
    }

    // ─────────────────────────────────────────────
    // 1.2 ĐĂNG NHẬP — JWT + REFRESH TOKEN
    // ─────────────────────────────────────────────

    /**
     * Xác thực thông tin đăng nhập, trả về Access Token và Refresh Token.
     *
     * @param request LoginRequest chứa username, password
     * @return AuthResponse chứa accessToken, refreshToken, thông tin user
     */
    public AuthResponse login(LoginRequest request) {
        // Spring Security xác thực username/password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));

        if (!user.getIsActive()) {
            throw new AccountDisabledException("Tài khoản đã bị vô hiệu hóa");
        }

        String accessToken  = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Lưu refresh token vào Redis (TTL = 7 ngày) — tính năng 18
        redisTokenService.saveRefreshToken(user.getUserId(), refreshToken);

        // Audit log — tính năng 16
        auditLogService.log(user.getUserId(), "LOGIN", "Đăng nhập thành công");

        log.info("Đăng nhập thành công userId={}", user.getUserId());
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserResponse.from(user))
                .build();
    }

    // ─────────────────────────────────────────────
    // 1.3 REFRESH TOKEN
    // ─────────────────────────────────────────────

    /**
     * Làm mới Access Token bằng Refresh Token còn hiệu lực.
     *
     * @param refreshToken chuỗi refresh token từ client
     * @return AuthResponse với access token mới
     */
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        Integer userId = jwtTokenProvider.getUserId(refreshToken);

        // Kiểm tra refresh token có khớp với Redis không — tống token bị thu hồi
        String storedToken = redisTokenService.getRefreshToken(userId);
        if (!refreshToken.equals(storedToken)) {
            throw new InvalidTokenException("Refresh token đã bị thu hồi");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user id=" + userId));

        String newAccessToken  = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Xoay vòng refresh token — tính năng bảo mật
        redisTokenService.saveRefreshToken(userId, newRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(UserResponse.from(user))
                .build();
    }

    // ─────────────────────────────────────────────
    // 1.4 ĐỔI MẬT KHẨU (khi đã đăng nhập)
    // ─────────────────────────────────────────────

    /**
     * Đổi mật khẩu cho user đang đăng nhập.
     *
     * @param userId      ID của user hiện tại (lấy từ JWT)
     * @param request     ChangePasswordRequest chứa oldPassword, newPassword
     */
    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user id=" + userId));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu cũ không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Thu hồi tất cả refresh token sau khi đổi mật khẩu
        redisTokenService.deleteRefreshToken(userId);

        // Audit log — tính năng 16
        auditLogService.log(userId, "CHANGE_PASSWORD", "Đổi mật khẩu thành công");

        log.info("Đổi mật khẩu thành công userId={}", userId);
    }

    // ─────────────────────────────────────────────
    // 1.5 QUÊN MẬT KHẨU — OTP qua email
    // ─────────────────────────────────────────────

    /**
     * Bước 1: Gửi OTP reset mật khẩu về email.
     *
     * @param email địa chỉ email đã đăng ký
     */
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email chưa được đăng ký: " + email));

        // Sinh OTP 6 số, lưu vào Redis với TTL 15 phút — tính năng 18
        String otp = generateOtp();
        redisTokenService.saveOtp(user.getUserId(), otp);

        // Gửi email chứa OTP — tính năng 11
        emailService.sendPasswordResetOtp(user.getEmail(), user.getUsername(), otp);

        auditLogService.log(user.getUserId(), "FORGOT_PASSWORD", "Gửi OTP về email " + email);
        log.info("Đã gửi OTP reset mật khẩu tới email={}", email);
    }

    /**
     * Bước 2: Xác nhận OTP và đặt mật khẩu mới.
     *
     * @param request ResetPasswordRequest chứa email, otp, newPassword
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email không tồn tại"));

        String storedOtp = redisTokenService.getOtp(user.getUserId());
        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
            throw new InvalidTokenException("OTP không đúng hoặc đã hết hạn");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Xoá OTP và thu hồi refresh token
        redisTokenService.deleteOtp(user.getUserId());
        redisTokenService.deleteRefreshToken(user.getUserId());

        auditLogService.log(user.getUserId(), "RESET_PASSWORD", "Đặt lại mật khẩu thành công");
        log.info("Reset mật khẩu thành công userId={}", user.getUserId());
    }

    // ─────────────────────────────────────────────
    // 1.6 LOGOUT
    // ─────────────────────────────────────────────

    /**
     * Đăng xuất — thu hồi refresh token khỏi Redis.
     *
     * @param userId ID user hiện tại
     */
    public void logout(Integer userId) {
        redisTokenService.deleteRefreshToken(userId);
        auditLogService.log(userId, "LOGOUT", "Đăng xuất");
        log.info("Logout userId={}", userId);
    }

    // ─────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────

    private String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}