package com.example.demo.DTOs.ResponseDTO;

import com.example.demo.Entities.Doctor;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
public class DoctorResponse {
    private Integer    doctorId;
    private String     fullName;
    private String     specialty;
    private String     degree;
    private Integer    experienceYears;
    private BigDecimal consultationFee;
    private String     biography;
    private UserResponse user;

    public static DoctorResponse from(Doctor doctor) {
        return DoctorResponse.builder()
                .doctorId(doctor.getDoctorId())
                .fullName(doctor.getFullName())
                .specialty(doctor.getSpecialty())
                .degree(doctor.getDegree())
                .experienceYears(doctor.getExperienceYears())
                .consultationFee(doctor.getConsultationFee())
                .biography(doctor.getBiography())
                .user(UserResponse.from(doctor.getUser()))
                .build();
    }
}