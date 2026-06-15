package com.example.demo.Repositories;


import com.example.demo.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Tìm kiếm người dùng bằng Username để xử lý Đăng nhập & JWT Auth
    Optional<User> findByUsername(String username);

    // Tìm kiếm bằng Email phục vụ tính năng Quên mật khẩu / Kiểm tra trùng lặp
    Optional<User> findByEmail(String email);

    // Kiểm tra xem Username hoặc Email đã tồn tại hay chưa khi Đăng ký
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}