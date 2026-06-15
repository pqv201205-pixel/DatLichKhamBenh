package com.example.demo.Repositories;


import com.example.demo.Entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    // Xem lịch sử khám hoặc danh sách lịch hẹn của 1 Bệnh nhân
    List<Appointment> findByPatient_PatientIdOrderByAppointmentDateDesc(Integer patientId);

    // Bác sĩ xem danh sách lịch hẹn khám của mình trong ngày hôm nay
    List<Appointment> findByDoctor_DoctorIdAndAppointmentDate(Integer doctorId, LocalDate date);

    // 14. Chống đặt trùng lịch: Kiểm tra xem Bệnh nhân đã có lịch nào trong khung giờ này chưa
    boolean existsByPatient_PatientIdAndAppointmentDateAndTimeSlotAndStatusNot(
            Integer patientId, LocalDate date, String timeSlot, String status);

    // 15. Chống quá tải: Đếm số lượng bệnh nhân đã đặt vào khung giờ cụ thể của 1 bác sĩ
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.doctorId = :doctorId " +
            "AND a.appointmentDate = :date AND a.timeSlot = :timeSlot AND a.status <> 'CANCELLED'")
    long countBookedSlots(@Param("doctorId") Integer doctorId,
                          @Param("date") LocalDate date,
                          @Param("timeSlot") String timeSlot);

    // 19. Scheduler: Tìm danh sách lịch hẹn có ngày khám là ngày mai để gửi Email nhắc lịch tự động
    List<Appointment> findByAppointmentDateAndStatus(LocalDate date, String status);

    // 19. Scheduler: Tìm các lịch hẹn trạng thái 'PENDING' nhưng chưa thanh toán quá 15 phút để hủy tự động
    // (Kết hợp xử lý logic thời gian ở tầng Service)
    List<Appointment> findByStatusAndPaymentStatus(String status, String paymentStatus);

    // 17. Dashboard: Đếm tổng số lịch hẹn trong hệ thống
    @Query("SELECT COUNT(a) FROM Appointment a")
    long countTotalAppointments();

    // 17. Dashboard: Đếm số lịch bị hủy để tính Tỷ lệ hủy lịch
    long countByStatus(String status);
}