package org.t2404e.kanji_together_db.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode; // <--- [MỚI] Import
import lombok.ToString;          // <--- [MỚI] Import
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.t2404e.kanji_together_db.enums.QuestionType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
public class Questions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "question_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    @Column(name = "wrong_answer_1", nullable = false)
    private String wrongAnswer1;

    @Column(name = "wrong_answer_2", nullable = false)
    private String wrongAnswer2;

    @Column(name = "wrong_answer_3", nullable = false)
    private String wrongAnswer3;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    // --- CÁC MỐI QUAN HỆ CẦN CHẶN LOMBOK ---

    @ManyToOne
    @JoinColumn(name = "exam_id")
    @JsonIgnoreProperties("questions")
    @ToString.Exclude          // [QUAN TRỌNG] Chặn vòng lặp toString
    @EqualsAndHashCode.Exclude // [QUAN TRỌNG] Chặn vòng lặp hashCode
    private Exams exam;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "kanji_characters_rel_question",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "kanji_id")
    )
    @JsonIgnoreProperties("questions")
    @ToString.Exclude          // [QUAN TRỌNG] Chặn vòng lặp toString
    @EqualsAndHashCode.Exclude // [QUAN TRỌNG] Chặn vòng lặp hashCode
    private List<KanjiCharacters> kanjiCharacters;

    // --- AUDIT ---

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "edit_at")
    private LocalDateTime editAt;
}