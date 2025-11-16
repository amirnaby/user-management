package com.niam.usermanagement.service.captcha;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Registry for captcha providers. Spring will inject all beans implementing CaptchaProvider.
 * You can add new providers with @Service("providerName")
 */
@Component
public class CaptchaRegistry {
    private final Map<String, CaptchaProvider> providers;

    public CaptchaRegistry(Map<String, CaptchaProvider> providers) {
        this.providers = providers;
    }

    /**
     * Get provider by bean name. Returns null if not found.
     */
    public CaptchaProvider get(String providerName) {
        return providers.get(providerName);
    }
}