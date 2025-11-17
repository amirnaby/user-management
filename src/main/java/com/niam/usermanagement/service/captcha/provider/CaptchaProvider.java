package com.niam.usermanagement.service.captcha.provider;

import com.niam.usermanagement.model.dto.CaptchaGenerateRequest;
import com.niam.usermanagement.model.dto.CaptchaResponse;
import com.niam.usermanagement.model.dto.CaptchaValidateRequest;

public interface CaptchaProvider {
    CaptchaResponse generate(CaptchaGenerateRequest request);

    boolean validate(CaptchaValidateRequest request);
}