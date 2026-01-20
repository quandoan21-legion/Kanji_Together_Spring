package org.t2404e.kanji_together_db.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.t2404e.kanji_together_db.dto.ApiResponse;
import org.t2404e.kanji_together_db.dto.AuthLoginRequest;
import org.t2404e.kanji_together_db.dto.AuthRegisterRequest;
import org.t2404e.kanji_together_db.dto.AuthResponse;
import org.t2404e.kanji_together_db.dto.ChangePasswordRequest;
import org.t2404e.kanji_together_db.dto.OtpRequest;
import org.t2404e.kanji_together_db.dto.OtpVerifyRequest;
import org.t2404e.kanji_together_db.dto.TokenRefreshRequest;
import org.t2404e.kanji_together_db.dto.TokenRefreshResponse;
import org.t2404e.kanji_together_db.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody AuthRegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(new ApiResponse<>(201, "Register success", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthLoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Login success", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refresh(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Refresh success", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(new ApiResponse<>(200, "Logout success", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Change password success", null));
    }

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse<Void>> requestOtp(@Valid @RequestBody OtpRequest request) {
        authService.requestOtp(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "OTP sent", null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok(new ApiResponse<>(200, "Verify success", null));
    }
}
