package org.t2404e.kanji_together_db.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExamDTO {
    private Long id;
    private String name;
    private String type; // ENTRANCE, MINI, SKIBIDI, SUPER

    // Các trường bổ sung theo ảnh bạn gửi
    private Integer duration;    // Thời gian làm bài (phút)
    private Integer passScore;   // Điểm đạt
    private Integer status;      // 1: Active, 0: Hidden
    private String targetRank;   // N1, N2... (Nếu có)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<QuestionDTO> fullQuestions;
    // --- QUAN TRỌNG: Danh sách ID câu hỏi để lưu xuống DB ---
    private List<Long> questionIds;
    // --- QUAN TRỌNG: Danh sách ID bài học để liên kết với Exam ---
    private List<Long> lessonIds;

    // Để hiển thị ra ngoài (không cần load full câu hỏi cho nhẹ)
    private Integer totalQuestions;
}
