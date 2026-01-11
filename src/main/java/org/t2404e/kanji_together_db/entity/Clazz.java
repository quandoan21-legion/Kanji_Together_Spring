package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clazz")
@Data
public class Clazz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Nên để private

    // Validate DB: Bắt buộc nhập, tối đa 100 ký tự
    @Column(nullable = false, length = 100)
    private String name;

    // Validate DB: Tối đa 500 ký tự
    @Column(length = 500)
    private String description;

    // --- QUAN TRỌNG: Phục vụ Soft Delete ---
    // Mặc định là true khi tạo mới
    @Column(name = "is_active", columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "edit_at") // Map biến Java camelCase sang cột DB snake_case
    private LocalDateTime editAt;

    // Relationship
    @OneToMany(mappedBy = "clazz", fetch = FetchType.LAZY)
    private List<Users> users;
}