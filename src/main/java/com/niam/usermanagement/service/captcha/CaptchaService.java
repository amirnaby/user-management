package com.niam.usermanagement.service.captcha;

import com.niam.common.exception.NotFoundException;
import com.niam.usermanagement.model.enums.CaptchaProviderType;
import com.niam.usermanagement.model.payload.request.CaptchaGenerateRequest;
import com.niam.usermanagement.model.payload.request.CaptchaValidateRequest;
import com.niam.usermanagement.model.payload.response.CaptchaResponse;
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
        if (!enabled) throw new NotFoundException("Captcha is disabled");
        CaptchaProvider p = registry.get(configured);
        return p.generate(new CaptchaGenerateRequest());
    }

    public boolean validate(String token, String userResponse) {
        if (!enabled) return true;
        CaptchaProvider p = registry.get(configured);
        return p.validate(new CaptchaValidateRequest(token, userResponse));
    }
}