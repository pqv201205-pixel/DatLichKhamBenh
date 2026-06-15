package com.example.demo.DTOs.ResponseDTO;

import com.example.demo.Entities.Notification;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Integer       notificationId;
    private String        title;
    private String        message;
    private Boolean       isRead;
    private String        notificationType;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .notificationType(n.getNotificationType())
                .createdAt(n.getCreatedAt())
                .build();
    }
}