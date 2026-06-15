package com.example.demo.Repositories;

import com.example.demo.Entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    // Tìm hồ sơ bác sĩ bằng UserId đăng nhập
    Optional<Doctor> findByUser_UserId(Integer userId);

    // Tìm danh sách bác sĩ theo tên Chuyên khoa (Hỗ trợ Cache danh sách bác sĩ)
    List<Doctor> findBySpecialty(String specialty);

    // Tìm kiếm bác sĩ kết hợp lọc chuyên khoa và kinh nghiệm
    List<Doctor> findBySpecialtyAndExperienceYearsGreaterThanEqual(String specialty, Integer years);

    // Thống kê tổng số bác sĩ phục vụ Dashboard Quản trị
    @Query("SELECT COUNT(d) FROM Doctor d")
    long countTotalDoctors();
}