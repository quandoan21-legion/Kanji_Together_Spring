package org.t2404e.kanji_together_db.entity;

import com.fasterxml.jackson.annotation.JsonProperty; // <--- QUAN TRỌNG: Thư viện để map tên JSON
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
public class Questions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ruby gửi: question_type
    @JsonProperty("question_type")
    @Column(name = "question_type", nullable = false)
    private String questionType;

    // Ruby gửi: question_text
    @JsonProperty("question_text")
    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    // --- CÁC ĐÁP ÁN ---

    // Ruby gửi: correct_answer
    @JsonProperty("correct_answer")
    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    // Ruby gửi: wrong_answer_1 -> Java nhận vào wrongAnswer1
    @JsonProperty("wrong_answer_1")
    @Column(name = "wrong_answer_1", nullable = false)
    private String wrongAnswer1;

    // Ruby gửi: wrong_answer_2
    @JsonProperty("wrong_answer_2")
    @Column(name = "wrong_answer_2", nullable = false)
    private String wrongAnswer2;

    // Ruby gửi: wrong_answer_3
    @JsonProperty("wrong_answer_3")
    @Column(name = "wrong_answer_3", nullable = false)
    private String wrongAnswer3;

    // --- TRẠNG THÁI (Manual Soft Delete) ---
    // 1 = Active, 0 = Deleted
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    // --- QUAN HỆ ---
    @ManyToOne
    @JoinColumn(name = "exam_id")
    @JsonBackReference
    private Exams exam;

    @ManyToMany
    @JoinTable(
            name = "kanji_characters_rel_question",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "kanji_id")
    )
    private List<KanjiCharacters> kanjiCharacters;

    // --- AUDIT FIELDS ---
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
