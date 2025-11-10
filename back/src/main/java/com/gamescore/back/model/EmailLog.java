package com.gamescore.back.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import com.gamescore.back.model.enums.EmailStatus;
import com.gamescore.back.model.enums.EmailType;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs",
       indexes = {
           @Index(name = "idx_email_logs_user", columnList = "user_id"),
           @Index(name = "idx_email_logs_type", columnList = "email_type"),
           @Index(name = "idx_email_logs_status", columnList = "status"),
           @Index(name = "idx_email_logs_sent", columnList = "sent_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 50)
    private EmailType emailType;
    
    @Column(nullable = false, length = 255)
    private String subject;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EmailStatus status = EmailStatus.PENDING;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;
}