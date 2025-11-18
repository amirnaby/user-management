package com.niam.usermanagement.service.captcha.provider;

import com.niam.usermanagement.model.payload.request.CaptchaGenerateRequest;
import com.niam.usermanagement.model.payload.response.CaptchaResponse;
import com.niam.usermanagement.model.payload.request.CaptchaValidateRequest;

public interface CaptchaProvider {
    CaptchaResponse generate(CaptchaGenerateRequest request);

    boolean validate(CaptchaValidateRequest request);
}