package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.t2404e.kanji_together_db.enums.EExamType;
import org.t2404e.kanji_together_db.enums.ERank;

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
    @Column(name = "exam_type", nullable = false)
    private EExamType examType;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_rank")
    private ERank targetRank;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "pass_score")
    private Integer passScore;

    @Column(name = "total_questions")
    private Integer totalQuestions;

    @Column(name = "lesson_id")
    private Long lessonId;

    @Column(nullable = false)
    private Integer status = 1;


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