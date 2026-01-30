package org.t2404e.kanji_together_db.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "kanji_characters")
@Data
public class KanjiCharacters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- ĐÃ SỬA: Xóa "unique = true" ---
    // Cho phép lưu nhiều bản ghi cùng tên (1 Active, nhiều Pending)
    @Column(nullable = false, length = 10)
    private String kanji;

    @Column(name = "on_pronunciation")
    private String onPronunciation;


    @Column(name = "kun_pronunciation")
    private String kunPronunciation;

    @Column(name = "num_strokes")
    private Integer numStrokes;

    @Column(name = "JLPT")
    private Integer jlpt;

    @Column(name = "kanji_description")
    private String kanjiDescription;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "translation")
    private String translation;

    @Column(name = "meaning", columnDefinition = "TEXT")
    private String meaning;

    @Column(name = "writing_image_url")
    private String writingImageUrl;

    @Column(name = "components")
    private String components;

    @Column(name = "radical")
    private String radical;

    @Column(name = "vocabulary", columnDefinition = "TEXT")
    private String vocabulary;

    @Column(name = "examples", columnDefinition = "TEXT")
    private String examples;

    // --- CÁC MỐI QUAN HỆ (LIST) CẦN CHẶN LOMBOK VÀ JSON ---

    @ManyToMany(mappedBy = "kanjiCharacters")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore // <--- [ĐÃ THÊM] Chặn vòng lặp qua Bài học (Lessons)
    private List<KanjiLessons> lessons;

    @OneToMany(mappedBy = "kanjiCharacter", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore //
    private List<KanjiStories> stories;

    @ManyToMany(mappedBy = "kanjiCharacters")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
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

    @Column(name = "status")
    private String status;
}
