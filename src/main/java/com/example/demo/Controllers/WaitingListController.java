package com.example.demo.Controllers;

import com.example.demo.DTOs.RequestDTO.WaitingRequest;
import com.example.demo.Services.Impl.WaitingListServiceImpl; // Gọi trực tiếp Service đã viết ở bước trước
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/waiting-list")
@RequiredArgsConstructor
@Slf4j
public class WaitingListController {

    private final WaitingListServiceImpl waitingListService;

    /**
     * API Đăng ký xếp hàng vào danh sách chờ của một khung giờ khám đã bị đầy chỗ (Dành cho Bệnh nhân)
     * URL: POST http://localhost:8080/api/waiting-list/add
     */
    @PostMapping("/add")
    public ResponseEntity<String> addToWaitingList(@Valid @RequestBody WaitingRequest request) {
        log.info("REST request thêm Patient ID: {} vào hàng đợi của Schedule ID: {}", request.getPatientId(), request.getScheduleId());
        waitingListService.addToWaitingList(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Bạn đã được thêm vào danh sách chờ. Hệ thống sẽ tự động xếp lịch và gửi mail nếu có ca hủy.");
    }

    /**
     * API Kích hoạt duyệt thủ công danh sách chờ (Dành cho Admin nếu cần can thiệp)
     * Thường logic này chạy ngầm tự động khi có ca hủy, nhưng mở thêm API này để linh hoạt quản trị.
     * URL: POST http://localhost:8080/api/waiting-list/process/1
     */
    @PostMapping("/process/{scheduleId}")
    public ResponseEntity<String> triggerProcessWaitingList(@PathVariable Integer scheduleId) {
        log.info("REST request xử lý danh sách chờ thủ công cho Schedule ID: {}", scheduleId);
        waitingListService.processWaitingList(scheduleId);
        return ResponseEntity.ok("Đã quét hàng đợi và xử lý chuyển đổi thành công lịch hẹn (nếu có slot trống).");
    }
}