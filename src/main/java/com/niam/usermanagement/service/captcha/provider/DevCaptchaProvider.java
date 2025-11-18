package com.niam.usermanagement.service.captcha.provider;

import com.niam.usermanagement.model.payload.request.CaptchaGenerateRequest;
import com.niam.usermanagement.model.payload.response.CaptchaResponse;
import com.niam.usermanagement.model.payload.request.CaptchaValidateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("devCaptchaProvider")
public class DevCaptchaProvider implements CaptchaProvider {
    @Override
    public CaptchaResponse generate(CaptchaGenerateRequest request) {
        String token = "dev-token";
        String fakeImage = "";
        int ttl = 600;
        log.warn("DEV captcha generated token={}", token);
        return new CaptchaResponse(token, fakeImage, ttl);
    }

    @Override
    public boolean validate(CaptchaValidateRequest request) {
        // in dev allow everything or check token == "dev-token"
        return "dev-token".equals(request.token());
    }
}