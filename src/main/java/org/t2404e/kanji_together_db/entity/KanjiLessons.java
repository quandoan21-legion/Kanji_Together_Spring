package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "kanji_lessons")
@Data
public class KanjiLessons {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kanji", length = 50)
    private String kanji;

    @Column(name = "JLPT")
    private Integer jlpt;

    @Column(name = "lesson_description", columnDefinition = "TEXT") // Thêm TEXT để lưu mô tả dài
    private String lessonDescription;

    @Column(name = "status")
    private String status = "ACTIVE";

    @Column(name = "create_at")
    private LocalDateTime createAt;

    @Column(name = "edit_at")
    private LocalDateTime editAt;

    @Column(name = "create_by")
    private Integer createBy;

    @Column(name = "edit_by")
    private Integer editBy;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "kanji_characters_rel_lesson",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "kanji_id")
    )
    @ToString.Exclude
    private List<KanjiCharacters> kanjiCharacters;

    @PrePersist
    protected void onCreate() {
        createAt = LocalDateTime.now();
        editAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        editAt = LocalDateTime.now();
    }
}