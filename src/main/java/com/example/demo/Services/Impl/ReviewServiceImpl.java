package com.example.demo.Services.Impl;

import com.example.demo.DTOs.RequestDTO.ReviewRequest;
import com.example.demo.Entities.Review;
import com.example.demo.Entities.Appointment;
import com.example.demo.Enums.AppointmentStatus;
import com.example.demo.Repositories.ReviewRepository;
import com.example.demo.Repositories.AppointmentRepository;

import com.example.demo.Services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    public void createReview(ReviewRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch khám phù hợp."));

        // Chỉ cho phép đánh giá khi ca khám đã hoàn tất
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Bạn không thể đánh giá lịch hẹn khi chưa hoàn thành ca khám.");
        }

        Review review = new Review();
        review.setDoctor(appointment.getDoctor());
        review.setPatientId(appointment.getPatient().getPatientId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(java.time.LocalDateTime.now());

        reviewRepository.save(review);
    }

    public List<Review> getDoctorReviews(Integer doctorId) {
        return reviewRepository.findByDoctor_DoctorIdOrderByCreatedAtDesc(doctorId);
    }
}