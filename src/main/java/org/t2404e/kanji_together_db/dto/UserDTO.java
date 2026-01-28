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

    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Display name không được để trống")
    @JsonProperty("display_name")
    private String displayName;

    @NotBlank(message = "Avatar URL không được để trống")
    @JsonProperty("avatar_url")
    private String avatarUrl;

    @NotBlank(message = "Số điện thoại không được để trống")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @JsonProperty("has_entrance_exam")
    private Boolean hasEntranceExam;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_verified")
    private Boolean isVerified;

    @JsonProperty("last_login_at")
    private LocalDateTime lastLoginAt;

    @JsonProperty(value = "password_hash", access = JsonProperty.Access.WRITE_ONLY)
    private String passwordHash;

    @JsonProperty("auth_provider")
    private String authProvider;

    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @JsonProperty("address_line1")
    private String addressLine1;

    private String city;

    private String state;

    @JsonProperty("postal_code")
    private String postalCode;

    private String country;

    @JsonProperty("clazz_id")
    private Long clazzId;

    private Integer role;

    // Thêm rank để hứng dữ liệu (Bronze, Silver, Gold...)
    private String rank;

    // --- OUTPUT ONLY ---
    @JsonProperty("clazz_name")
    private String clazzName;

    @JsonProperty("create_by")
    private LocalDateTime createBy;

    @JsonProperty("edit_by")
    private LocalDateTime editBy;
}