package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "avatar_url", nullable = false)
    private String avatarUrl;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "has_entrance_exam")
    private Boolean hasEntranceExam;

    // Biến chuẩn camelCase, map cột snake_case
    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "auth_provider")
    private String authProvider;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "country")
    private String country;

    // --- TIMESTAMP FIELDS ---
    @UpdateTimestamp
    @Column(name = "edit_at")
    private LocalDateTime editAt;

    // Map cột create_by (kiểu timestamp trong DB của bạn)
    @CreationTimestamp
    @Column(name = "create_by", updatable = false)
    private LocalDateTime createBy;

    // Map cột edit_by (kiểu timestamp trong DB của bạn)
    @UpdateTimestamp
    @Column(name = "edit_by")
    private LocalDateTime editBy;

    // --- RELATIONSHIPS ---
    @ManyToOne
    @JoinColumn(name = "clazz_id")
    private Clazz clazz;

    // Các relationship khác (giữ nguyên nếu không dùng đến trong API này)
    @OneToMany(mappedBy = "user")
    private List<UserSubscriptions> userSubscriptions;

    @OneToMany(mappedBy = "user")
    private List<Transactions> transactions;

    @ManyToMany(mappedBy = "users")
    private List<Categories> categories;

    @OneToMany(mappedBy = "user")
    private List<ExamResults> examResults;
}
