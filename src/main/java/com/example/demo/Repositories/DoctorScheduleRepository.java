package com.example.demo.Repositories;


import com.example.demo.Entities.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Integer> {

    // Lấy danh sách lịch làm việc cố định trong tuần của 1 bác sĩ (Dùng để cache lên Redis)
    List<DoctorSchedule> findByDoctor_DoctorId(Integer doctorId);

    // Tìm khung giờ làm việc chính xác của bác sĩ dựa vào ngày trong tuần (DayOfWeek)
    Optional<DoctorSchedule> findByDoctor_DoctorIdAndDayOfWeek(Integer doctorId, Integer dayOfWeek);

    // Kiểm tra xem bác sĩ đã đăng ký khung giờ làm việc này chưa để tránh tạo trùng lịch làm việc
    boolean existsByDoctor_DoctorIdAndDayOfWeekAndStartTimeAndEndTime(
            Integer doctorId, Integer dayOfWeek, LocalTime startTime, LocalTime endTime);
}