package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "kanji_stories")
@Data
public class KanjiStories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên cột trong DB của bạn là kanji_story
    @Column(name = "kanji_story", length = 255)
    private String kanjiStory;

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

    @Column(name = "is_active")
    private Boolean isActive;

    // Quan hệ N-1: Thuộc về 1 Kanji
    @ManyToOne
    @JoinColumn(name = "kanji_id", nullable = false)
    private KanjiCharacters kanjiCharacter;
}