package com.resumise.backend.config;

import com.resumise.backend.service.OAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public SecurityConfig(OAuth2UserService oAuth2UserService,
                          ClientRegistrationRepository clientRegistrationRepository) {
        this.oAuth2UserService = oAuth2UserService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(c -> c.disable())
                .authorizeHttpRequests(a -> a
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/auth/me",
                                "/oauth2/**",
                                "/login/**",
                                "/favicon.ico",
                                "/images/**",
                                "/.well-known/**",
                                "/css/**",
                                "/js/**",
                                "/actuator/health",
                                "/error",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .loginPage("/oauth2/authorization/google")
                        .defaultSuccessUrl("/", true)
                        .authorizationEndpoint(endpoint -> {
                            DefaultOAuth2AuthorizationRequestResolver resolver =
                                    new DefaultOAuth2AuthorizationRequestResolver(
                                            clientRegistrationRepository,
                                            "/oauth2/authorization"
                                    );
                            endpoint.authorizationRequestResolver(new OAuth2AuthorizationRequestResolver() {
                                @Override
                                public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                                    return enrich(resolver.resolve(request));
                                }

                                @Override
                                public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                                    return enrich(resolver.resolve(request, clientRegistrationId));
                                }

                                private OAuth2AuthorizationRequest enrich(OAuth2AuthorizationRequest authRequest) {
                                    if (authRequest == null) {
                                        return null;
                                    }

                                    Map<String, Object> extraParams = new HashMap<>(authRequest.getAdditionalParameters());
                                    extraParams.put("prompt", "select_account");

                                    return OAuth2AuthorizationRequest.from(authRequest)
                                            .additionalParameters(extraParams)
                                            .build();
                                }
                            });
                        })
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oAuth2UserService)));

        return http.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Local frontend denemeleri ve Vercel preview/domainleri icin esnek pattern
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://*.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
