package com.resumise.backend.service;

import com.resumise.backend.dto.AuthLoginRequest;
import com.resumise.backend.dto.AuthRegisterRequest;
import com.resumise.backend.model.AuthAccount;
import com.resumise.backend.model.AuthProvider;
import com.resumise.backend.model.Credential;
import com.resumise.backend.model.User;
import com.resumise.backend.repository.AuthAccountRepository;
import com.resumise.backend.repository.CredentialRepository;
import com.resumise.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class LocalAuthService {

    private final UserRepository userRepository;
    private final CredentialRepository credentialRepository;
    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public LocalAuthService(UserRepository userRepository,
                            CredentialRepository credentialRepository,
                            AuthAccountRepository authAccountRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.credentialRepository = credentialRepository;
        this.authAccountRepository = authAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(AuthRegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setFirstName(resolveName(request.firstName(), email));
        user.setLastName(resolveName(request.lastName(), "User"));
        user.setProfileImageUrl(AuthProvisioningService.DEFAULT_PROFILE_IMAGE_URL);
        user = userRepository.save(user);

        Credential credential = new Credential();
        credential.setUser(user);
        credential.setPasswordHash(passwordEncoder.encode(request.password()));
        credential.setEnabled(true);
        credentialRepository.save(credential);

        AuthAccount localAccount = new AuthAccount();
        localAccount.setUser(user);
        localAccount.setProvider(AuthProvider.LOCAL);
        localAccount.setProviderUserId(email);
        authAccountRepository.save(localAccount);

        return user;
    }

    @Transactional
    public User login(AuthLoginRequest request, HttpServletRequest httpRequest) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        Credential credential = credentialRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!credential.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, java.util.List.of());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        httpRequest.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return user;
    }

    private String resolveName(String value, String fallback) {
        if (StringUtils.hasText(value)) {
            return value.trim();
        }
        return fallback;
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

