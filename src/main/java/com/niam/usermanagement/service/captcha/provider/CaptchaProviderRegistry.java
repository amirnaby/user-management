package com.niam.usermanagement.service.captcha.provider;

import com.niam.usermanagement.model.enums.CaptchaProviderType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Registry that holds all CaptchaProvider beans.
 * Providers must be registered with @Service("beanName").
 * Example: localCaptchaProvider, googleCaptchaProvider, etc.
 */
@Component
public class CaptchaProviderRegistry {
    private final Map<String, CaptchaProvider> providers;

    public CaptchaProviderRegistry(Map<String, CaptchaProvider> providers) {
        this.providers = providers;
    }

    public CaptchaProvider get(CaptchaProviderType type) {
        return providers.get(type.beanName());
    }
}