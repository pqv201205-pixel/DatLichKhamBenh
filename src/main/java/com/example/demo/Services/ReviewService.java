package com.example.demo.Services;

import com.example.demo.DTOs.RequestDTO.ReviewRequest;
import com.example.demo.Entities.Review;

import java.util.List;

public interface ReviewService {
    void createReview(ReviewRequest request);
    List<Review> getDoctorReviews(Integer doctorId);
}
