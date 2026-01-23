package org.t2404e.kanji_together_db.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_attempt_answers")
@Data
public class ExamAttemptAnswers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "exam_result_id")
    public ExamResults examResult;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public Users user;

    @ManyToOne
    @JoinColumn(name = "question_id")
    public Questions question;

    @Column(name = "selected_answer_id")
    public Integer selectedAnswerId;

    public Boolean is_correct;

    public LocalDateTime answered_at;

    @Column(name = "time_taken_ms")
    public Long timeTakenMs;
}
