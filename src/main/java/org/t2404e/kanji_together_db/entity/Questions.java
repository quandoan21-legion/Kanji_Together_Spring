package org.t2404e.kanji_together_db.entity;

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

    @Column(length = 255)
    private String name;

    @Column(name = "question")
    private String questionText;

    // --- SỬA LỖI TẠI ĐÂY (Thêm đoạn này vào) ---
    // Biến này tên là "exam" -> Khớp với mappedBy = "exam" bên file Exams
    @ManyToOne
    @JoinColumn(name = "exam_id") // Tên cột khóa ngoại trong DB
    private Exams exam;

    // --- Quan hệ với KanjiCharacters (Đã làm đúng ở bước trước - Giữ nguyên) ---
    @ManyToMany
    @JoinTable(
            name = "kanji_characters_rel_question",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "kanji_id")
    )
    private List<KanjiCharacters> kanjiCharacters;

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