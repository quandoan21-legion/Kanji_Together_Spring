package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "exams")
@Data
public class Exams {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Ví dụ các cột khác của bạn
    // private String question;
    // private String examType;

    // --- QUAN HỆ VỚI QUESTIONS ---
    // mappedBy="exam" trỏ tới biến "private Exams exam;" ở file Questions
    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Questions> questions;

    // --- Audit Fields ---
    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "edit_at")
    private LocalDateTime editAt;

    @Column(name = "create_by")
    private Integer createBy;

    @Column(name = "edit_by")
    private Integer editBy;
}
