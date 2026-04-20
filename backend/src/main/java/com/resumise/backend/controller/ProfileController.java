package com.resumise.backend.controller;

import com.resumise.backend.dto.NotificationPreferencesUpdateRequest;
import com.resumise.backend.dto.ProfileResponse;
import com.resumise.backend.dto.ProfileUpdateRequest;
import com.resumise.backend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Returns editable profile information for the authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            Authentication authentication
    ) {
        return ResponseEntity.ok(profileService.getMyProfile(authentication));
    }

    /**
     * Updates profile fields shown on the profile page.
     */
    @PatchMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return ResponseEntity.ok(profileService.updateMyProfile(authentication, request));
    }

    /**
     * Updates notification preferences from account settings.
     */
    @PatchMapping("/me/notifications")
    public ResponseEntity<ProfileResponse> updateNotificationPreferences(
            Authentication authentication,
            @RequestBody NotificationPreferencesUpdateRequest request
    ) {
        return ResponseEntity.ok(profileService.updateNotificationPreferences(authentication, request));
    }
}

