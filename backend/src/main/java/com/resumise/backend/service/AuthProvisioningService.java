package com.resumise.backend.service;

import com.resumise.backend.model.AuthAccount;
import com.resumise.backend.model.AuthProvider;
import com.resumise.backend.model.User;
import com.resumise.backend.repository.AuthAccountRepository;
import com.resumise.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthProvisioningService {

    public static final String DEFAULT_PROFILE_IMAGE_URL = "/images/default-profile.png";

    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;

    public AuthProvisioningService(UserRepository userRepository, AuthAccountRepository authAccountRepository) {
        this.userRepository = userRepository;
        this.authAccountRepository = authAccountRepository;
    }

    @Transactional
    public User provisionGoogleUser(OAuth2User principal) {
        String email = normalizeEmail(getAttr(principal, "email"));
        String providerUserId = getAttr(principal, "sub");
        String pictureUrl = getAttr(principal, "picture");
        if (email == null || providerUserId == null) {
            throw new IllegalArgumentException("OAuth user info is missing required fields");
        }

        return authAccountRepository
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, providerUserId)
                .map(AuthAccount::getUser)
                .map(existingUser -> updateUserBasics(existingUser, principal, email, pictureUrl))
                .orElseGet(() -> createOrLinkUserAndAccount(principal, email, providerUserId, pictureUrl));
    }

    @Transactional
    public User resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            return provisionGoogleUser(oauth2User);
        }

        String email;
        if (principal instanceof UserDetails userDetails) {
            email = normalizeEmail(userDetails.getUsername());
        } else if (principal instanceof String principalName && !"anonymousUser".equals(principalName)) {
            email = normalizeEmail(principalName);
        } else {
            email = normalizeEmail(authentication.getName());
        }

        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private User createOrLinkUserAndAccount(OAuth2User principal, String email, String providerUserId, String pictureUrl) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(User::new);

        if (user.getId() == null) {
            user.setEmail(email);
        }
        user.setFirstName(resolveFirstName(principal, email));
        user.setLastName(resolveLastName(principal));
        user.setProfileImageUrl(resolveProfileImage(pictureUrl, user.getProfileImageUrl()));
        User savedUser = userRepository.save(user);

        AuthAccount authAccount = new AuthAccount();
        authAccount.setProvider(AuthProvider.GOOGLE);
        authAccount.setProviderUserId(providerUserId);
        authAccount.setUser(savedUser);
        authAccountRepository.save(authAccount);

        return savedUser;
    }

    private User updateUserBasics(User existingUser, OAuth2User principal, String email, String pictureUrl) {
        existingUser.setEmail(email);
        existingUser.setFirstName(resolveFirstName(principal, email));
        existingUser.setLastName(resolveLastName(principal));
        existingUser.setProfileImageUrl(resolveProfileImage(pictureUrl, existingUser.getProfileImageUrl()));
        return userRepository.save(existingUser);
    }

    private String resolveProfileImage(String googlePicture, String existingValue) {
        if (StringUtils.hasText(googlePicture)) {
            return googlePicture.trim();
        }
        if (StringUtils.hasText(existingValue)) {
            return existingValue;
        }
        return DEFAULT_PROFILE_IMAGE_URL;
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String getAttr(OAuth2User principal, String key) {
        Object attr = principal.getAttributes().get(key);
        if (attr == null) {
            return null;
        }
        String value = String.valueOf(attr).trim();
        return value.isBlank() ? null : value;
    }

    private String resolveFirstName(OAuth2User principal, String email) {
        String givenName = getAttr(principal, "given_name");
        if (givenName != null) {
            return givenName;
        }

        String fullName = getAttr(principal, "name");
        if (fullName != null) {
            String[] parts = fullName.split("\\s+");
            if (parts.length > 0) {
                return parts[0];
            }
        }

        String localPart = email.split("@")[0];
        if (!localPart.isBlank()) {
            return capitalize(localPart);
        }

        return "User";
    }

    private String resolveLastName(OAuth2User principal) {
        String familyName = getAttr(principal, "family_name");
        if (familyName != null) {
            return familyName;
        }

        String fullName = getAttr(principal, "name");
        if (fullName != null) {
            String[] parts = fullName.split("\\s+");
            if (parts.length > 1) {
                return parts[parts.length - 1];
            }
        }

        return "Unknown";
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }
}

