package com.example.demo.DTOs.RequestDTO;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateAppointmentStatusRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "^(PENDING|CONFIRMED|CHECKED_IN|COMPLETED|CANCELLED|NO_SHOW)$",
            message = "Trạng thái không hợp lệ")
    private String status;

    @Size(max = 500)
    private String cancelReason;
}