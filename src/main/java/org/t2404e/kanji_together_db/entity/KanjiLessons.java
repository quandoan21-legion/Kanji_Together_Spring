package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "kanji_lessons")
@Data
public class KanjiLessons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Nên để private

    // Map với cột tên bài học trong DB
    @Column(name = "name")
    private String name;

    @Column(name = "JLPT")
    private Integer jlpt;

    @Column(name = "lesson_description")
    private String lessonDescription;

    @ManyToMany
    @JoinTable(
            name = "kanji_characters_rel_lesson",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "kanji_id")
    )
    private List<KanjiCharacters> kanjiCharacters;

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