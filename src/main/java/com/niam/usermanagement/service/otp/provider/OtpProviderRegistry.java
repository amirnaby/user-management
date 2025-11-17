package com.niam.usermanagement.service.otp.provider;

import com.niam.usermanagement.model.enums.OtpProviderType;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Registry for resolving OTP providers by their enum type.
 */
@Component
public class OtpProviderRegistry {
    private final Map<String, OtpProvider> providers;

    public OtpProviderRegistry(Map<String, OtpProvider> providers) {
        this.providers = providers;
    }

    public OtpProvider get(OtpProviderType type) {
        return providers.get(type.beanName());
    }
}