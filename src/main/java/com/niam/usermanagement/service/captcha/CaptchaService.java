package com.niam.usermanagement.service.captcha;

import com.niam.common.exception.NotFoundException;
import com.niam.usermanagement.config.UMConfigFile;
import com.niam.usermanagement.model.enums.CaptchaProviderType;
import com.niam.usermanagement.model.payload.request.CaptchaGenerateRequest;
import com.niam.usermanagement.model.payload.request.CaptchaValidateRequest;
import com.niam.usermanagement.model.payload.response.CaptchaResponse;
import com.niam.usermanagement.service.captcha.provider.CaptchaProvider;
import com.niam.usermanagement.service.captcha.provider.CaptchaProviderRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Service
@RequiredArgsConstructor
public class CaptchaService {
    private final CaptchaProviderRegistry registry;
    private final UMConfigFile configFile;

    public CaptchaResponse generate() {
        if (!configFile.isCaptchaEnabled()) throw new NotFoundException("Captcha is disabled");
        CaptchaProvider p = registry.get(CaptchaProviderType.valueOf(configFile.getCaptchaProviderName()));
        return p.generate(new CaptchaGenerateRequest());
    }

    public boolean validate(String token, String userResponse) {
        if (!configFile.isCaptchaEnabled()) return true;
        CaptchaProvider p = registry.get(CaptchaProviderType.valueOf(configFile.getCaptchaProviderName()));
        return p.validate(new CaptchaValidateRequest(token, userResponse));
    }
}