package com.example.demo.Repositories;


import com.example.demo.Entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    // Lấy thông tin hồ sơ bệnh nhân thông qua UserId đăng nhập
    Optional<Patient> findByUser_UserId(Integer userId);

    // Thống kê tổng số bệnh nhân phục vụ Dashboard Quản trị
    @Query("SELECT COUNT(p) FROM Patient p")
    long countTotalPatients();
}