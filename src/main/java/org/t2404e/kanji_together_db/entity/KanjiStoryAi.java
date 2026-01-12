package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "kanji_stories_ai")
@Data
public class KanjiStoryAi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String kanji;

    private String meaning;

    @Column(columnDefinition = "TEXT")
    private String story;


    private String jlpt_level;

    @Column(name = "create_at")
    private LocalDateTime create_at;

    @PrePersist
    protected void onCreate() {
        this.create_at = LocalDateTime.now();
    }
}