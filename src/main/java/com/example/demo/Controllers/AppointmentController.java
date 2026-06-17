package com.example.demo.Controllers;

import com.example.demo.DTOs.RequestDTO.BookAppointmentRequest;
import com.example.demo.DTOs.ResponseDTO.AppointmentResponse;
import com.example.demo.Entities.Appointment;
import com.example.demo.Enums.AppointmentStatus;
import com.example.demo.Services.AppointmentService; // Giả định bạn đã có AppointmentService
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {

    private final AppointmentService appointmentService;

    /**
     * 1. API Đặt lịch khám mới
     * URL: POST http://localhost:8080/api/appointments/book
     */
    @PostMapping("/book")
    public ResponseEntity<String> bookAppointment(@Valid @RequestBody BookAppointmentRequest request) {
        log.info("REST request đặt lịch khám cho Patient ID: {}", request.getPatientId());

        // Gọi tầng Service xử lý nghiệp vụ đặt lịch (kiểm tra trùng, giữ chỗ, lưu DB, gửi mail...)
        appointmentService.bookAppointment(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Đặt lịch hẹn khám bệnh thành công! Vui lòng kiểm tra email để xem chi tiết.");
    }

    /**
     * 2. API Bệnh nhân xem danh sách lịch sử khám / lịch hẹn của mình
     * URL: GET http://localhost:8080/api/appointments/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getPatientAppointments(@PathVariable Integer patientId) {
        log.info("REST request lấy danh sách lịch hẹn của Patient ID: {}", patientId);

        List<AppointmentResponse> appointments = appointmentService.getPatientAppointments(patientId);

        return ResponseEntity.ok(appointments);
    }

    /**
     * 3. API Bác sĩ xem toàn bộ lịch hẹn khám được phân công
     * URL: GET http://localhost:8080/api/appointments/doctor/{doctorId}
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments(@PathVariable Integer doctorId) {
        log.info("REST request lấy danh sách lịch khám của Doctor ID: {}", doctorId);

        List<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(doctorId);

        return ResponseEntity.ok(appointments);
    }

    /**
     * 4. API Hủy lịch hẹn khám bệnh (Yêu cầu truyền lý do hủy)
     * URL: PUT http://localhost:8080/api/appointments/{appointmentId}/cancel?reason=LyDoHuy
     */
    @PutMapping("/{appointmentId}/cancel")
    public ResponseEntity<String> cancelAppointment(
            @PathVariable Integer appointmentId,
            @RequestParam String reason) {
        log.info("REST request hủy lịch hẹn ID: {} với lý do: {}", appointmentId, reason);

        appointmentService.cancelAppointment(appointmentId, reason);

        return ResponseEntity.ok("Đã hủy lịch hẹn thành công và giải phóng khung giờ khám.");
    }

    /**
     * 5. API Cập nhật trạng thái lịch hẹn (Dành cho Admin/Bác sĩ tại quầy Check-in hoặc phòng khám)
     * URL: PUT http://localhost:8080/api/appointments/{appointmentId}/status?status=CONFIRMED
     */
    @PutMapping("/{appointmentId}/status")
    public ResponseEntity<String> updateAppointmentStatus(
            @PathVariable Integer appointmentId,
            @RequestParam AppointmentStatus status) {
        log.info("REST request cập nhật trạng thái lịch hẹn ID: {} sang: {}", appointmentId, status);

        appointmentService.updateStatus(appointmentId, status);

        return ResponseEntity.ok("Cập nhật trạng thái lịch hẹn thành công thành: " + status);
    }
}