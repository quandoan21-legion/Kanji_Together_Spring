package org.t2404e.kanji_together_db.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.t2404e.kanji_together_db.dto.DeviceRegisterRequest;
import org.t2404e.kanji_together_db.security.CustomUserDetails;
import org.t2404e.kanji_together_db.service.DeviceService;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {
    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/register")
    public void register(@RequestBody DeviceRegisterRequest request,
                         @AuthenticationPrincipal CustomUserDetails user) {
        deviceService.upsertToken(user != null ? user.getUser().getId() : null, request);
    }

    @DeleteMapping("/{token}")
    public void deactivate(@PathVariable String token) {
        deviceService.deactivate(token);
    }
}
