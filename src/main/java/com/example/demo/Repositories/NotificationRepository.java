package com.example.demo.Repositories;


import com.example.demo.Entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Lấy toàn bộ thông báo của một người dùng cụ thể (Bác sĩ hoặc Bệnh nhân), sắp xếp thông báo mới nhất lên đầu
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Integer userId);

    // Tìm danh sách các thông báo chưa đọc của người dùng
    List<Notification> findByUser_UserIdAndIsReadFalse(Integer userId);
}