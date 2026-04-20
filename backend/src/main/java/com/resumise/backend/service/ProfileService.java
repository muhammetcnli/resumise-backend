package com.resumise.backend.service;

import com.resumise.backend.dto.NotificationPreferencesUpdateRequest;
import com.resumise.backend.dto.ProfileResponse;
import com.resumise.backend.dto.ProfileUpdateRequest;
import com.resumise.backend.model.User;
import com.resumise.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProfileService {

    private final AuthProvisioningService authProvisioningService;
    private final UserRepository userRepository;

    public ProfileService(AuthProvisioningService authProvisioningService, UserRepository userRepository) {
        this.authProvisioningService = authProvisioningService;
        this.userRepository = userRepository;
    }

    public ProfileResponse getMyProfile(Authentication authentication) {
        User user = requireUser(authentication);
        return toProfileResponse(user);
    }

    @Transactional
    public ProfileResponse updateMyProfile(Authentication authentication, ProfileUpdateRequest request) {
        User user = requireUser(authentication);

        if (request.firstName() != null && StringUtils.hasText(request.firstName())) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null && StringUtils.hasText(request.lastName())) {
            user.setLastName(request.lastName().trim());
        }

        if (request.headline() != null) {
            user.setHeadline(normalizeOptional(request.headline()));
        }
        if (request.location() != null) {
            user.setLocation(normalizeOptional(request.location()));
        }
        if (request.phone() != null) {
            user.setPhone(normalizeOptional(request.phone()));
        }
        if (request.linkedinUrl() != null) {
            user.setLinkedinUrl(normalizeOptional(request.linkedinUrl()));
        }
        if (request.githubUrl() != null) {
            user.setGithubUrl(normalizeOptional(request.githubUrl()));
        }
        if (request.professionalSummary() != null) {
            user.setProfessionalSummary(normalizeOptional(request.professionalSummary()));
        }
        if (request.targetRoles() != null) {
            user.setTargetRoles(normalizeOptional(request.targetRoles()));
        }

        User saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    @Transactional
    public ProfileResponse updateNotificationPreferences(
            Authentication authentication,
            NotificationPreferencesUpdateRequest request
    ) {
        User user = requireUser(authentication);

        if (request.notifyHighMatch() != null) {
            user.setNotifyHighMatch(request.notifyHighMatch());
        }
        if (request.notifyNewsletter() != null) {
            user.setNotifyNewsletter(request.notifyNewsletter());
        }
        if (request.notifyInterviewReminders() != null) {
            user.setNotifyInterviewReminders(request.notifyInterviewReminders());
        }

        User saved = userRepository.save(user);
        return toProfileResponse(saved);
    }

    private User requireUser(Authentication authentication) {
        return authProvisioningService.resolveAuthenticatedUser(authentication);
    }

    private String normalizeOptional(String value) {
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ProfileResponse toProfileResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getHeadline(),
                user.getLocation(),
                user.getPhone(),
                user.getLinkedinUrl(),
                user.getGithubUrl(),
                user.getProfessionalSummary(),
                user.getTargetRoles(),
                user.isNotifyHighMatch(),
                user.isNotifyNewsletter(),
                user.isNotifyInterviewReminders()
        );
    }
}

