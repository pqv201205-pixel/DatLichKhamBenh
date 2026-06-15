package com.example.demo.Repositories;

import com.example.demo.Entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository
        extends JpaRepository<AuditLog,Integer> {
}