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

public interface UserService {


     UserResponse register(RegisterRequest request) ;

     AuthResponse login(LoginRequest request) ;


     AuthResponse refreshToken(String refreshToken) ;


     void changePassword(Integer userId, ChangePasswordRequest request) ;


     void forgotPassword(String email) ;

     void resetPassword(ResetPasswordRequest request);

     void logout(Integer userId) ;

     String generateOtp() ;
}