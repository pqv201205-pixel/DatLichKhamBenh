package com.example.demo.Controllers;

import com.example.demo.DTOs.RequestDTO.ScheduleRequest;
import com.example.demo.DTOs.ResponseDTO.ScheduleResponse;
import com.example.demo.Services.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@Slf4j
public class DoctorScheduleController {

    private final ScheduleService scheduleService;

    /**
     * API Tạo khung giờ làm việc mới cho Bác sĩ (Dành cho Admin/Bác sĩ)
     * URL: POST http://localhost:8080/api/schedules
     */
    @PostMapping
    public ResponseEntity<String> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        log.info("REST request tạo lịch làm việc mới cho Doctor ID: {}", request.getDoctorId());
        scheduleService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Tạo khung giờ làm việc mới thành công.");
    }

    /**
     * API Lấy danh sách lịch khám CÒN TRỐNG SLOTS của bác sĩ theo ngày cụ thể (Dành cho Bệnh nhân chọn đặt lịch)
     * URL: GET http://localhost:8080/api/schedules/available?doctorId=1&date=2026-06-20
     */
    @GetMapping("/available")
    public ResponseEntity<List<ScheduleResponse>> getAvailableSchedules(
            @RequestParam Integer doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("REST request lấy danh sách lịch khám trống của Doctor ID: {} vào ngày: {}", doctorId, date);
        List<ScheduleResponse> availableSchedules = scheduleService.getAvailableSchedules(doctorId, date);
        return ResponseEntity.ok(availableSchedules);
    }
}