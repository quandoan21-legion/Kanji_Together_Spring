package org.t2404e.kanji_together_db.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
// Đảm bảo bạn đã có Enum này, nếu chưa thì tạo file Enum riêng hoặc dùng String
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

    // Nếu bạn chưa tạo file Enum QuestionType, có thể đổi tạm thành String
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

    // --- CẬP NHẬT QUAN HỆ MANY-TO-MANY VỚI EXAMS ---

    // [MỚI] Thay thế @ManyToOne cũ.
    // Một câu hỏi có thể thuộc về nhiều bài thi (Mini, Super, Skibidi...)
    @ManyToMany(mappedBy = "questions") // "questions" là tên biến List trong file Exams.java
    @JsonIgnoreProperties("questions")  // Ngăn chặn vòng lặp JSON vô hạn khi gọi API
    @ToString.Exclude                   // Chặn vòng lặp toString của Lombok
    @EqualsAndHashCode.Exclude          // Chặn vòng lặp hashCode
    private List<Exams> exams;

    // --- CÁC MỐI QUAN HỆ KHÁC (GIỮ NGUYÊN) ---

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "kanji_characters_rel_question",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "kanji_id")
    )
    @JsonIgnoreProperties("questions")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<KanjiCharacters> kanjiCharacters;

    // --- AUDIT ---

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "edit_at")
    private LocalDateTime editAt;
}