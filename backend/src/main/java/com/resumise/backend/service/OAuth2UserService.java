package com.resumise.backend.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final AuthProvisioningService authProvisioningService;

    public OAuth2UserService(AuthProvisioningService authProvisioningService) {
        this.authProvisioningService = authProvisioningService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        if (!"google".equalsIgnoreCase(registrationId)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"), "Only Google OAuth is supported for now");
        }

        try {
            authProvisioningService.provisionGoogleUser(oauth2User);
        } catch (IllegalArgumentException ex) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info"), ex.getMessage());
        }

        return oauth2User;
    }
}
