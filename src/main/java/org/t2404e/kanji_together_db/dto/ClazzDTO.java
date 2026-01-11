package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClazzDTO {
    private Long id;

    // --- VALIDATION INPUT ---
    @NotBlank(message = "Tên lớp không được để trống")
    @Size(min = 3, max = 100, message = "Tên lớp phải từ 3 đến 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;

    // --- READ ONLY ---
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}