package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_email_otps")
@Data
public class UserEmailOtp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_sent_at", nullable = false)
    private LocalDateTime lastSentAt;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;
}
