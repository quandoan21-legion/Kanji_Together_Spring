package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.AuthLoginRequest;
import org.t2404e.kanji_together_db.dto.AuthRegisterRequest;
import org.t2404e.kanji_together_db.dto.AuthResponse;
import org.t2404e.kanji_together_db.dto.ChangePasswordRequest;
import org.t2404e.kanji_together_db.dto.OtpRequest;
import org.t2404e.kanji_together_db.dto.OtpVerifyRequest;
import org.t2404e.kanji_together_db.dto.TokenRefreshRequest;
import org.t2404e.kanji_together_db.dto.TokenRefreshResponse;
import org.t2404e.kanji_together_db.dto.UserDTO;
import org.t2404e.kanji_together_db.entity.UserEmailOtp;
import org.t2404e.kanji_together_db.entity.Users;
import org.t2404e.kanji_together_db.repository.UserEmailOtpRepository;
import org.t2404e.kanji_together_db.repository.UsersRepository;
import org.t2404e.kanji_together_db.security.JwtService;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UserEmailOtpRepository userEmailOtpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Value("${otp.ttl-minutes:10}")
    private int otpTtlMinutes;

    @Value("${otp.resend-minutes:1}")
    private int otpResendMinutes;

    @Value("${otp.max-attempts:5}")
    private int otpMaxAttempts;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthResponse register(AuthRegisterRequest request) {
        if (usersRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã tồn tại trong hệ thống");
        }
        if (usersRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username đã tồn tại trong hệ thống");
        }

        Users user = new Users();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setDisplayName(request.getDisplayName());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setIsVerified(false);
        user.setAuthProvider("local");

        Users savedUser = usersRepository.save(user);
        UserDTO userDTO = userService.getUserById(savedUser.getId());
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        return new AuthResponse(userDTO, accessToken, refreshToken);
    }

    public AuthResponse login(AuthLoginRequest request) {
        Users user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng");
        }

        user.setLastLoginAt(LocalDateTime.now());
        usersRepository.save(user);

        UserDTO userDTO = userService.getUserById(user.getId());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(userDTO, accessToken, refreshToken);
    }

    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtService.isTokenValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token không hợp lệ");
        }

        Long userId = jwtService.extractUserId(refreshToken);
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User không tồn tại"));

        String accessToken = jwtService.generateAccessToken(user);
        return new TokenRefreshResponse(accessToken);
    }

    public void changePassword(ChangePasswordRequest request) {
        Users user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Mật khẩu hiện tại không đúng");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);
    }

    public void requestOtp(OtpRequest request) {
        Users user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User đã xác thực");
        }

        LocalDateTime now = LocalDateTime.now();
        userEmailOtpRepository.findTopByUserOrderByIdDesc(user).ifPresent(latest -> {
            if (latest.getLastSentAt() != null
                    && latest.getLastSentAt().plusMinutes(otpResendMinutes).isAfter(now)) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Vui lòng thử lại sau");
            }
        });

        String otpCode = generateOtpCode();
        UserEmailOtp otp = new UserEmailOtp();
        otp.setUser(user);
        otp.setCodeHash(passwordEncoder.encode(otpCode));
        otp.setExpiresAt(now.plusMinutes(otpTtlMinutes));
        otp.setLastSentAt(now);
        otp.setAttempts(0);

        userEmailOtpRepository.save(otp);
        emailService.sendOtpEmail(user.getEmail(), otpCode, otpTtlMinutes);
    }

    public void verifyOtp(OtpVerifyRequest request) {
        Users user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));

        UserEmailOtp otp = userEmailOtpRepository.findTopByUserOrderByIdDesc(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP không tồn tại"));

        if (otp.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP đã được sử dụng");
        }
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP đã hết hạn");
        }
        if (otp.getAttempts() != null && otp.getAttempts() >= otpMaxAttempts) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP đã bị khóa");
        }

        if (!passwordEncoder.matches(request.getOtpCode(), otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            userEmailOtpRepository.save(otp);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP không đúng");
        }

        otp.setConsumedAt(LocalDateTime.now());
        userEmailOtpRepository.save(otp);

        user.setIsVerified(true);
        usersRepository.save(user);
    }
    public void resetPassword(String email, String otpCode, String newPassword) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));
        UserEmailOtp otp = userEmailOtpRepository.findTopByUserOrderByIdDesc(user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP không tồn tại"));
        if (otp.getConsumedAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP chưa được xác thực");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
    }
    private String generateOtpCode() {
        int code = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(code);
    }
}
