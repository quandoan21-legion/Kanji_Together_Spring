package org.t2404e.kanji_together_db.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.t2404e.kanji_together_db.dto.DeviceTokenRequest;
import org.t2404e.kanji_together_db.dto.DeviceTokenResponse;
import org.t2404e.kanji_together_db.entity.UserDeviceTokens;
import org.t2404e.kanji_together_db.entity.Users;
import org.t2404e.kanji_together_db.repository.UserDeviceTokensRepository;
import org.t2404e.kanji_together_db.repository.UsersRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserDeviceTokensService {
    @Autowired
    private UserDeviceTokensRepository userDeviceTokensRepository;

    @Autowired
    private UsersRepository usersRepository;

    public DeviceTokenResponse register(DeviceTokenRequest request) {
        validateRequest(request);

        Users user = usersRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy user"));

        Optional<UserDeviceTokens> existing = userDeviceTokensRepository.findByFcmToken(request.getFcmToken());
        UserDeviceTokens record = existing.orElseGet(UserDeviceTokens::new);
        record.setUser(user);
        record.setFcmToken(request.getFcmToken());
        record.setPlatform(request.getPlatform());
        record.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        record.setLastSeenAt(LocalDateTime.now());

        UserDeviceTokens saved = userDeviceTokensRepository.save(record);
        return toResponse(saved);
    }

    public List<DeviceTokenResponse> listByUser(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu user_id");
        }
        return userDeviceTokensRepository.findByUser_Id(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DeviceTokenResponse deactivate(DeviceTokenRequest request) {
        validateRequest(request);

        UserDeviceTokens record = userDeviceTokensRepository.findByUser_IdAndFcmToken(request.getUserId(), request.getFcmToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy device token"));

        record.setIsActive(false);
        record.setLastSeenAt(LocalDateTime.now());
        return toResponse(userDeviceTokensRepository.save(record));
    }

    private void validateRequest(DeviceTokenRequest request) {
        if (request == null || request.getUserId() == null || isBlank(request.getFcmToken())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu user_id hoặc token");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private DeviceTokenResponse toResponse(UserDeviceTokens record) {
        DeviceTokenResponse response = new DeviceTokenResponse();
        response.setId(record.getId());
        response.setUserId(record.getUser() != null ? record.getUser().getId() : null);
        response.setFcmToken(record.getFcmToken());
        response.setPlatform(record.getPlatform());
        response.setDeviceId(record.getDeviceId());
        response.setAppVersion(record.getAppVersion());
        response.setIsActive(record.getIsActive());
        response.setLastSeenAt(record.getLastSeenAt());
        response.setCreateAt(record.getCreateAt());
        response.setEditAt(record.getEditAt());
        return response;
    }
}
