package com.niam.usermanagement.service.otp.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("localOtpProvider")
public class LocalOtpProvider implements OtpProvider {

    @Override
    public void sendOtp(String destination, String code) {
        log.warn("DEV OTP for {} => {}", destination, code);
    }
}