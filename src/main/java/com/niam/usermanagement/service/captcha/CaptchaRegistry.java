package com.niam.usermanagement.service.captcha;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Registry that holds all CaptchaProvider beans.
 * Providers must be registered with @Service("beanName").
 * Example: localCaptchaProvider, googleCaptchaProvider, etc.
 */
@Component
public class CaptchaRegistry {
    private final Map<String, CaptchaProvider> providers;

    public CaptchaRegistry(Map<String, CaptchaProvider> providers) {
        this.providers = providers;
    }

    /**
     * Returns provider with the given bean name.
     *
     * @param providerName Spring bean name
     * @return CaptchaProvider or null if not found
     */
    public CaptchaProvider get(String providerName) {
        return providers.get(providerName);
    }
}