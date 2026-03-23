package com.banking.notification.controller;

import com.banking.notification.dto.NotificationResponse;
import com.banking.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{username}")
    @Operation(summary = "Get notifications for a user")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(notificationService.getUserNotifications(username, page, size));
    }

    @GetMapping("/user/{username}/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@PathVariable String username) {
        return ResponseEntity.ok(notificationService.getUnreadCount(username));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }
}
