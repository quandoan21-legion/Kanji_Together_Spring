package org.t2404e.kanji_together_db.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.t2404e.kanji_together_db.dto.DeviceTokenRequest;
import org.t2404e.kanji_together_db.dto.DeviceTokenResponse;
import org.t2404e.kanji_together_db.service.UserDeviceTokensService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/device-tokens")
public class UserDeviceTokensController {

    @Autowired
    private UserDeviceTokensService userDeviceTokensService;

    @PostMapping
    public ResponseEntity<DeviceTokenResponse> register(@RequestBody DeviceTokenRequest request) {
        return ResponseEntity.ok(userDeviceTokensService.register(request));
    }

    @GetMapping
    public ResponseEntity<List<DeviceTokenResponse>> list(@RequestParam Long userId) {
        return ResponseEntity.ok(userDeviceTokensService.listByUser(userId));
    }

    @PatchMapping("/deactivate")
    public ResponseEntity<DeviceTokenResponse> deactivate(@RequestBody DeviceTokenRequest request) {
        return ResponseEntity.ok(userDeviceTokensService.deactivate(request));
    }
}
