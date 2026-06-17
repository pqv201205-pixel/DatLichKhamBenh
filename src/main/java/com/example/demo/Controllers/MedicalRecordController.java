package com.example.demo.Controllers;

import com.example.demo.DTOs.RequestDTO.MedicalRecordRequest;
import com.example.demo.Entities.MedicalRecord;
import com.example.demo.Services.MedicalRecordService; // Giả định bạn đã có Service này
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    /**
     * API Tạo hồ sơ bệnh án mới cho bệnh nhân khi khám xong (Dành cho Bác sĩ)
     * URL: POST http://localhost:8080/api/medical-records
     */

    @PostMapping
    public ResponseEntity<String> createMedicalRecord(@Valid @RequestBody MedicalRecordRequest request) {
        log.info("REST request tạo bệnh án mới.");

        // Gọi hàm void ở tầng Service
        medicalRecordService.createMedicalRecord(request);

        // Trả về HTTP Status 210 (Created) kèm thông báo bằng chữ
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Tạo hồ sơ bệnh án mới cho bệnh nhân thành công.");
    }
    /**
     * API Tải lên tài liệu, hình ảnh chụp chiếu hoặc đơn thuốc đính kèm bệnh án
     * URL: POST http://localhost:8080/api/medical-records/1/upload
     */
    @PostMapping("/{recordId}/upload")
    public ResponseEntity<String> uploadMedicalDocuments(
            @PathVariable Integer recordId,
            @RequestParam("files") MultipartFile[] files) {
        log.info("REST request upload tài liệu cho bệnh án ID: {}", recordId);
        medicalRecordService.uploadMedicalDocuments(recordId, files);
        return ResponseEntity.ok("Tải lên tài liệu đính kèm bệnh án thành công.");
    }

    /**
     * API Lấy toàn bộ lịch sử bệnh án từ trước đến nay của một Bệnh nhân cụ thể (Sắp xếp mới nhất lên đầu)
     * URL: GET http://localhost:8080/api/medical-records/patient/1
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecord>> getPatientHistory(@PathVariable Integer patientId) {
        log.info("REST request lấy lịch sử bệnh án của Patient ID: {}", patientId);
        // Gọi chuẩn hàm lấy lịch sử theo Integer patientId đã bóc tách quan hệ
        List<MedicalRecord> history = medicalRecordService.getPatientHistory(patientId);
        return ResponseEntity.ok(history);
    }
}