package com.example.demo.Entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Doctors")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DoctorId")
    private Integer doctorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Column(name = "FullName", length = 100, nullable = false)
    private String fullName;

    @Column(name = "Specialty", length = 100, nullable = false)
    private String specialty;

    @Column(name = "Degree", length = 50)
    private String degree;

    @Column(name = "ExperienceYears")
    private Integer experienceYears;

    @Column(name = "ConsultationFee", precision = 18, scale = 2, nullable = false)
    private BigDecimal consultationFee;

    @Column(name = "Biography", columnDefinition = "NVARCHAR(MAX)")
    private String biography;
}