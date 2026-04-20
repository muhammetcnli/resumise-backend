package com.resumise.backend.controller;

import com.resumise.backend.dto.AuthLoginRequest;
import com.resumise.backend.dto.AuthRegisterRequest;
import com.resumise.backend.model.AuthProvider;
import com.resumise.backend.model.User;
import com.resumise.backend.service.AuthProvisioningService;
import com.resumise.backend.service.LocalAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthProvisioningService authProvisioningService;
    private final LocalAuthService localAuthService;

    public AuthController(AuthProvisioningService authProvisioningService, LocalAuthService localAuthService) {
        this.authProvisioningService = authProvisioningService;
        this.localAuthService = localAuthService;
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        User user;
        try {
            user = authProvisioningService.resolveAuthenticatedUser(authentication);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }

        String provider = (authentication.getPrincipal() instanceof OAuth2User)
                ? AuthProvider.GOOGLE.name()
                : AuthProvider.LOCAL.name();

        return new MeResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                provider
        );
    }

    @PostMapping("/register")
    public ResponseEntity<MeResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        User user = localAuthService.register(request);
        MeResponse response = new MeResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                AuthProvider.LOCAL.name()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<MeResponse> login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletRequest httpRequest
    ) {
        User user = localAuthService.login(request, httpRequest);
        MeResponse response = new MeResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                AuthProvider.LOCAL.name()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return ResponseEntity.noContent().build();
    }

    public record MeResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String profileImageUrl,
            String provider
    ) {
    }
}
