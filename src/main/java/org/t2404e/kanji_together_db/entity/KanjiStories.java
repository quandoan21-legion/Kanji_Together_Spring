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

    @Column(name = "kanji_story", columnDefinition = "TEXT") // Đổi sang TEXT để viết truyện dài
    private String kanjiStory;

    // THÊM TRƯỜNG NÀY: Để lọc Chờ duyệt/Đã duyệt
    @Column(name = "status")
    private String status = "pending";

    @ManyToOne
    @JoinColumn(name = "user_id") // Đảm bảo tên cột trong MySQL là user_id
    private Users user;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "edit_at")
    private LocalDateTime editAt;
    @Column(name = "create_by")
    private Integer createBy;
    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "edit_by")
    private Integer editBy;
    @ManyToOne
    @JoinColumn(name = "kanji_id", nullable = false)
    private KanjiCharacters kanjiCharacter;

}