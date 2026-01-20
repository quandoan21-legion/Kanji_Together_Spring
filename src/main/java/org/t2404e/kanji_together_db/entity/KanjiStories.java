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

    @Column(name = "kanji_story", columnDefinition = "TEXT")
    private String kanjiStory;

    @Column(name = "status")
    private String status = "pending";

    // --- CÁC CỘT LƯU THÔNG TIN THÔ TỪ USER ---
    @Column(name = "kanji_text")
    private String kanjiText;

    @Column(name = "user_translation")
    private String userTranslation;

    @Column(name = "user_meaning")
    private String userMeaning;

    @Column(name = "user_num_strokes")
    private Integer userNumStrokes;

    // BỔ SUNG: Hai trường âm đọc người dùng đóng góp
    @Column(name = "user_onyomi")
    private String userOnyomi;

    @Column(name = "user_kunyomi")
    private String userKunyomi;

    @Column(name = "user_radical")
    private String userRadical;

    @Column(name = "user_components")
    private String userComponents;

    @Column(name = "user_vocabulary", columnDefinition = "TEXT")
    private String userVocabulary;

    @Column(name = "user_examples", columnDefinition = "TEXT")
    private String userExamples;
    // ----------------------------------------------

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "edit_at")
    private LocalDateTime editAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "kanji_id", nullable = true)
    private KanjiCharacters kanjiCharacter;
}