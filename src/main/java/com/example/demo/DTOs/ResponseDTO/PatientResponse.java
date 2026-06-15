package com.example.demo.DTOs.ResponseDTO;

import com.example.demo.Entities.Patient;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
public class PatientResponse {
    private Integer     patientId;
    private String      fullName;
    private String      gender;
    private LocalDate   dateOfBirth;
    private String      address;
    private String      medicalHistory;
    private UserResponse user;

    public static PatientResponse from(Patient patient) {
        return PatientResponse.builder()
                .patientId(patient.getPatientId())
                .fullName(patient.getFullName())
                .gender(patient.getGender())
                .dateOfBirth(patient.getDateOfBirth())
                .address(patient.getAddress())
                .medicalHistory(patient.getMedicalHistory())
                .user(UserResponse.from(patient.getUser()))
                .build();
    }
}