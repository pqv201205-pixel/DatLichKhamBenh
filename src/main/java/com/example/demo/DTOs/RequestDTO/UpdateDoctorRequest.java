package com.example.demo.DTOs.RequestDTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateDoctorRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @NotBlank(message = "Chuyên khoa không được để trống")
    @Size(max = 100)
    private String specialty;

    @Size(max = 50)
    private String degree;

    @Min(value = 0, message = "Số năm kinh nghiệm không âm")
    @Max(value = 60, message = "Số năm kinh nghiệm tối đa 60")
    private Integer experienceYears;

    @DecimalMin(value = "0.0", message = "Phí tư vấn không âm")
    private BigDecimal consultationFee;

    private String biography;
}