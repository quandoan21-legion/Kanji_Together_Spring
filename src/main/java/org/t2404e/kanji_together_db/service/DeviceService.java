package org.t2404e.kanji_together_db.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.DeviceRegisterRequest;
import org.t2404e.kanji_together_db.entity.UserDeviceTokens;
import org.t2404e.kanji_together_db.entity.Users;
import org.t2404e.kanji_together_db.repository.UserDeviceTokensRepository;
import org.t2404e.kanji_together_db.repository.UsersRepository;

import java.time.LocalDateTime;

@Service
public class DeviceService {
    private final UserDeviceTokensRepository userDeviceTokensRepository;
    private final UsersRepository usersRepository;

    public DeviceService(UserDeviceTokensRepository userDeviceTokensRepository, UsersRepository usersRepository) {
        this.userDeviceTokensRepository = userDeviceTokensRepository;
        this.usersRepository = usersRepository;
    }

    public void upsertToken(Long userId, DeviceRegisterRequest request) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing user");
        }
        if (request == null || isBlank(request.getFcmToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing fcmToken");
        }
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));

        UserDeviceTokens record = userDeviceTokensRepository.findByFcmToken(request.getFcmToken())
                .orElseGet(UserDeviceTokens::new);

        record.setUser(user);
        record.setFcmToken(request.getFcmToken());
        record.setPlatform(request.getPlatform());
        record.setDeviceId(request.getDeviceId());
        record.setAppVersion(request.getAppVersion());
        record.setIsActive(true);
        record.setLastSeenAt(LocalDateTime.now());

        userDeviceTokensRepository.save(record);
    }

    public void deactivate(String token) {
        if (isBlank(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing token");
        }
        UserDeviceTokens record = userDeviceTokensRepository.findByFcmToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy device token"));
        record.setIsActive(false);
        record.setLastSeenAt(LocalDateTime.now());
        userDeviceTokensRepository.save(record);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
