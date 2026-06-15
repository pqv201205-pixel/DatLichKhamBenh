package com.example.demo.Services;

import com.example.demo.DTOs.*;
import com.example.demo.DTOs.RequestDTO.*;
import com.example.demo.DTOs.ResponseDTO.AuthResponse;
import com.example.demo.DTOs.ResponseDTO.DoctorResponse;
import com.example.demo.DTOs.ResponseDTO.PatientResponse;

import java.util.List;

public interface UserService {
    // Xác thực
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void changePassword(Integer userId, ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(Integer userId);
    // Hồ sơ người dùng
    DoctorResponse updateDoctorProfile(Long doctorId, UpdateDoctorRequest request);
    PatientResponse updatePatientProfile(Long patientId, UpdatePatientRequest request);
    List<DoctorResponse> getAllDoctors();

}