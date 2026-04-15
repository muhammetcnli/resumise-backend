package com.resumise.backend.service;

import com.resumise.backend.model.AuthAccount;
import com.resumise.backend.model.AuthProvider;
import com.resumise.backend.model.User;
import com.resumise.backend.repository.AuthAccountRepository;
import com.resumise.backend.repository.UserRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthProvisioningService {

    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;

    public AuthProvisioningService(UserRepository userRepository, AuthAccountRepository authAccountRepository) {
        this.userRepository = userRepository;
        this.authAccountRepository = authAccountRepository;
    }

    @Transactional
    public User provisionGoogleUser(OAuth2User principal) {
        String email = getAttr(principal, "email");
        String providerUserId = getAttr(principal, "sub");
        if (email == null || providerUserId == null) {
            throw new IllegalArgumentException("OAuth user info is missing required fields");
        }

        return authAccountRepository
                .findByProviderAndProviderUserId(AuthProvider.GOOGLE, providerUserId)
                .map(AuthAccount::getUser)
                .map(existingUser -> updateUserBasics(existingUser, principal, email))
                .orElseGet(() -> createOrLinkUserAndAccount(principal, email, providerUserId));
    }

    private User createOrLinkUserAndAccount(OAuth2User principal, String email, String providerUserId) {
        User user = userRepository.findByEmail(email).orElseGet(User::new);

        if (user.getId() == null) {
            user.setEmail(email);
        }
        user.setFirstName(resolveFirstName(principal, email));
        user.setLastName(resolveLastName(principal));
        User savedUser = userRepository.save(user);

        AuthAccount authAccount = new AuthAccount();
        authAccount.setProvider(AuthProvider.GOOGLE);
        authAccount.setProviderUserId(providerUserId);
        authAccount.setUser(savedUser);
        authAccountRepository.save(authAccount);

        return savedUser;
    }

    private User updateUserBasics(User existingUser, OAuth2User principal, String email) {
        existingUser.setEmail(email);
        existingUser.setFirstName(resolveFirstName(principal, email));
        existingUser.setLastName(resolveLastName(principal));
        return userRepository.save(existingUser);
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

