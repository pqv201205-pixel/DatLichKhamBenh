package com.example.demo.Services;

import com.example.demo.Entities.AuditLog;
import com.example.demo.Repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(
            Integer userId,
            String action,
            String description){

        AuditLog auditLog =
                AuditLog.builder()
                        .userId(userId)
                        .action(action)
                        .description(description)
                        .createdAt(LocalDateTime.now())
                        .build();

        auditLogRepository.save(auditLog);
    }
}