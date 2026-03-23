package com.banking.notification.repository;

import com.banking.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientUsernameOrderByCreatedAtDesc(String username, Pageable pageable);

    List<Notification> findByRecipientUsernameAndStatus(String username, Notification.NotificationStatus status);

    long countByRecipientUsernameAndStatus(String username, Notification.NotificationStatus status);
}
