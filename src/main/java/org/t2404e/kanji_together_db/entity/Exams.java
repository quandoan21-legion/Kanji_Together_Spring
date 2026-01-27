package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.t2404e.kanji_together_db.enums.ExamType;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ExamType type;

    @Column(name = "duration")
    private Integer duration; // Thời gian làm bài (phút)

    @Column(name = "pass_score")
    private Integer passScore; // Điểm đậu

    @Column(name = "status")
    private Integer status = 1; // 1: Active, 0: Hidden

    @Column(name = "target_rank")
    private String targetRank; // N1, N2... (Dành cho Super Exam)

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "lesson_id")
    private Long lessonId;

    // --- QUAN HỆ MANY-TO-MANY ---
    @ManyToMany
    @JoinTable(
            name = "exam_questions",
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
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