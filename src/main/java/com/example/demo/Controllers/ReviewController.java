package com.example.demo.Controllers;

import com.example.demo.DTOs.RequestDTO.ReviewRequest;
import com.example.demo.Entities.Review;
import com.example.demo.Services.Impl.ReviewServiceImpl;
import com.example.demo.Services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * API Gửi đánh giá phản hồi ca khám mới (Dành cho Bệnh nhân)
     * URL: POST http://localhost:8080/api/reviews
     */
    @PostMapping
    public ResponseEntity<String> createReview(@Valid @RequestBody ReviewRequest request) {
        log.info("REST request tạo đánh giá mới cho Appointment ID: {}", request.getAppointmentId());
        reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cảm ơn bạn đã gửi đánh giá phản hồi ca khám!");
    }

    /**
     * API Xem danh sách toàn bộ đánh giá của một Bác sĩ cụ thể (Dành cho trang cá nhân Bác sĩ)
     * URL: GET http://localhost:8080/api/reviews/doctor/1
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Review>> getDoctorReviews(@PathVariable Integer doctorId) {
        log.info("REST request lấy danh sách đánh giá của Doctor ID: {}", doctorId);
        List<Review> reviews = reviewService.getDoctorReviews(doctorId);
        return ResponseEntity.ok(reviews);
    }
}