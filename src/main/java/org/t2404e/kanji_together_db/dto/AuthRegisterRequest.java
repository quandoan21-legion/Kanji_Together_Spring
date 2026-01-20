package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRegisterRequest {
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

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
