package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    @JsonProperty("current_password")
    private String currentPassword;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @JsonProperty("new_password")
    private String newPassword;
}
