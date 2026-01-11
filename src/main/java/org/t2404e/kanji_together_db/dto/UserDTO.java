package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDTO {
    // ID chỉ trả về, không nhận từ Client
    private Long id;

    // --- INPUT FIELDS ---
    @NotBlank(message = "Tên user không được để trống")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @JsonProperty("has_entrance_exam")
    private Boolean hasEntranceExam;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_verified")
    private Boolean isVerified;

    // Để gán lớp ngay khi tạo user (Optional)
    @JsonProperty("clazz_id")
    private Long clazzId;

    // --- OUTPUT ONLY ---
    @JsonProperty("clazz_name")
    private String clazzName;

    @JsonProperty("create_by")
    private LocalDateTime createBy;

    @JsonProperty("edit_by")
    private LocalDateTime editBy;
}