package com.example.demo.DTOs.RequestDTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalTime;

@Data
public class DoctorScheduleRequest {

    @NotNull(message = "Ngày trong tuần không được để trống")
    @Min(value = 1, message = "Ngày trong tuần từ 1 (Thứ 2) đến 7 (Chủ nhật)")
    @Max(value = 7, message = "Ngày trong tuần từ 1 (Thứ 2) đến 7 (Chủ nhật)")
    private Integer dayOfWeek;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime startTime;

    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime endTime;

    @NotNull(message = "Số slot tối đa không được để trống")
    @Min(value = 1, message = "Số slot tối thiểu là 1")
    @Max(value = 50, message = "Số slot tối đa là 50")
    private Integer maxSlots;
}