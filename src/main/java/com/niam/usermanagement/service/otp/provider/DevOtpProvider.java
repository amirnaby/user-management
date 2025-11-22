package com.niam.usermanagement.service.otp.provider;

import com.niam.usermanagement.model.payload.request.OtpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Lazy
@Slf4j
@Service("devOtpProvider")
public class DevOtpProvider implements OtpProvider {
    @Value("${app.otp.dev.master-code:999999}")
    private String masterCode;

    @Override
    public void send(OtpRequest request) {
        log.warn("DEV OTP for {} => {}", request.destination(), request.code());
        log.warn("MASTER OTP (bypass code) => {}", masterCode);
    }

    /**
     * Used only for development to bypass normal OTP flow.
     */
    public boolean isMasterCode(String input) {
        return masterCode.equals(input);
    }
}