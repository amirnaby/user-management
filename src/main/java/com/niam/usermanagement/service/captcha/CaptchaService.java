package com.niam.usermanagement.service.captcha;

import com.niam.usermanagement.model.dto.CaptchaGenerateRequest;
import com.niam.usermanagement.model.dto.CaptchaResponse;
import com.niam.usermanagement.model.dto.CaptchaValidateRequest;
import com.niam.usermanagement.model.enums.CaptchaProviderType;
import com.niam.usermanagement.service.captcha.provider.CaptchaProvider;
import com.niam.usermanagement.service.captcha.provider.CaptchaProviderRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaptchaService {
    private final CaptchaProviderRegistry registry;

    @Value("${app.captcha.enabled:false}")
    private boolean enabled;

    @Value("${app.captcha.provider:LOCAL}")
    private CaptchaProviderType configured;

    public CaptchaResponse generate() {
        if (!enabled) throw new IllegalStateException("Captcha disabled");
        CaptchaProvider p = registry.get(configured);
        return p.generate(new CaptchaGenerateRequest());
    }

    public boolean validate(String token, String userResponse) {
        if (!enabled) return true;
        CaptchaProvider p = registry.get(configured);
        return p.validate(new CaptchaValidateRequest(token, userResponse));
    }
}