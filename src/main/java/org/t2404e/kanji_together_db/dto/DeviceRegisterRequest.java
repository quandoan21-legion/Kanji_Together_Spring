package org.t2404e.kanji_together_db.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import org.t2404e.kanji_together_db.enums.DevicePlatform;

@Data
public class DeviceRegisterRequest {
    @JsonAlias({"token", "fcm_token"})
    private String fcmToken;

    private DevicePlatform platform;

    @JsonAlias("device_id")
    private String deviceId;

    @JsonAlias("app_version")
    private String appVersion;
}
