package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.t2404e.kanji_together_db.enums.CourseCategory;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "courses")
@Data
public class Courses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private CourseCategory category;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "time_to_finish", nullable = false)
    private String timeToFinish;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(name = "edit_at")
    private LocalDateTime editAt;

    @OneToMany(mappedBy = "course")
    @ToString.Exclude
    private List<KanjiLessons> lessons;
}
