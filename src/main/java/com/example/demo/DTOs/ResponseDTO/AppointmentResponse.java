package com.example.demo.DTOs.ResponseDTO;

import com.example.demo.Entities.Appointment;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentResponse {
    private Integer       appointmentId;
    private PatientResponse patient;
    private DoctorResponse  doctor;
    private LocalDate       appointmentDate;
    private String          timeSlot;
    private String          status;
    private String          paymentStatus;
    private String          symptoms;
    private String          notes;
    private LocalDateTime   createdAt;
    private LocalDateTime   updatedAt;

    public static AppointmentResponse from(Appointment a) {
        return AppointmentResponse.builder()
                .appointmentId(a.getAppointmentId())
                .patient(PatientResponse.from(a.getPatient()))
                .doctor(DoctorResponse.from(a.getDoctor()))
                .appointmentDate(a.getAppointmentDate())
                .timeSlot(a.getTimeSlot())
                .status(a.getStatus())
                .paymentStatus(a.getPaymentStatus())
                .symptoms(a.getSymptoms())
                .notes(a.getNotes())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}