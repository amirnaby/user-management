package com.niam.usermanagement.security;

import com.niam.usermanagement.exception.handlers.CustomAccessDeniedHandler;
import com.niam.usermanagement.exception.handlers.Http401UnauthorizedEntryPoint;
import com.niam.usermanagement.security.filter.*;
import com.niam.usermanagement.service.captcha.provider.CaptchaProviderRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {
    private static final long MAX_AGE = 3600L;
    private static final int CORS_FILTER_ORDER = -102;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final Http401UnauthorizedEntryPoint unauthorizedEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CaptchaProviderRegistry captchaProviderRegistry;
    private final IpRateLimitFilter ipRateLimitFilter;
    private final UsernameRateLimitFilter usernameRateLimitFilter;
    private final CachedBodyFilter cachedBodyFilter;
    private final CaptchaRateLimitFilter captchaRateLimitFilter;

    @Value("${app.captcha.enabled:false}")
    private boolean captchaEnabled;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("default-src 'self'; script-src 'self'; object-src 'none'")
                        )
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(request ->
                        request.requestMatchers(
                                        "/api/v1/auth/**",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/webjars/**",
                                        "/swagger-ui.html",
                                        "/error"
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                .sessionManagement(m -> m.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider);

        http.addFilterBefore(cachedBodyFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(ipRateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        // validate captcha AFTER captcha rate limit
        if (captchaEnabled) {
            http.addFilterBefore(captchaRateLimitFilter, UsernamePasswordAuthenticationFilter.class);
            CaptchaValidationFilter captchaFilter = new CaptchaValidationFilter(captchaProviderRegistry);
            http.addFilterBefore(captchaFilter, UsernamePasswordAuthenticationFilter.class);
        }

        // username-based rate limit
        http.addFilterBefore(usernameRateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        // JWT must be last
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, HttpHeaders.ACCEPT));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setMaxAge(MAX_AGE);
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<org.springframework.web.filter.CorsFilter> bean =
                new FilterRegistrationBean<>(new CorsFilter(source));

        bean.setOrder(CORS_FILTER_ORDER);
        return bean;
    }
}