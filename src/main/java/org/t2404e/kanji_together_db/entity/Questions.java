package org.t2404e.kanji_together_db.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    // --- CÁC TRƯỜNG DỮ LIỆU CƠ BẢN (GIỮ NGUYÊN) ---

    @JsonProperty("question_type")
    @Column(name = "question_type", nullable = false)
    private String questionType;

    @JsonProperty("question_text")
    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @JsonProperty("correct_answer")
    @Column(name = "correct_answer", nullable = false)
    private String correctAnswer;

    @JsonProperty("wrong_answer_1")
    @Column(name = "wrong_answer_1", nullable = false)
    private String wrongAnswer1;

    @JsonProperty("wrong_answer_2")
    @Column(name = "wrong_answer_2", nullable = false)
    private String wrongAnswer2;

    @JsonProperty("wrong_answer_3")
    @Column(name = "wrong_answer_3", nullable = false)
    private String wrongAnswer3;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    // --- QUAN HỆ 1: EXAMS (SỬA LẠI) ---
    // Trước đây: @ManyToOne (Chỉ thuộc 1 đề) -> Sai logic Ngân hàng câu hỏi
    // Bây giờ: @ManyToMany (Thuộc nhiều đề)
    // mappedBy = "questions" nghĩa là bảng Exams là bên chủ động quản lý mối quan hệ này
    @ManyToMany(mappedBy = "questions")
    @JsonIgnore // Quan trọng: Ngắt vòng lặp vô tận khi Ruby gọi API lấy danh sách câu hỏi
    private List<Exams> exams;

    // --- QUAN HỆ 2: KANJI (GIỮ NGUYÊN LOGIC NHIỀU - NHIỀU) ---
    // Logic: 1 câu hỏi (VD: Cách đọc 学生) thuộc về cả chữ Học (学) và Sinh (生)
    @ManyToMany
    @JoinTable(
            name = "kanji_characters_rel_question",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "kanji_id")
    )
    // Ruby gửi lên dạng: "related_kanjis": [{id: 1}, {id: 2}]
    @JsonProperty("related_kanjis")
    private List<KanjiCharacters> kanjiCharacters;

    // --- AUDIT FIELDS (GIỮ NGUYÊN) ---
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