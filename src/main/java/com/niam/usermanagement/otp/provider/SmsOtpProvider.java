package com.niam.usermanagement.otp.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("smsOtpProvider")
public class SmsOtpProvider implements OtpProvider {

    @Override
    public void sendOtp(String destination, String code) {
        log.info("Sending SMS OTP to {} => {}", destination, code);
    }
}