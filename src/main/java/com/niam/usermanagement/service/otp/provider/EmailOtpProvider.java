package com.niam.usermanagement.service.otp.provider;

import com.niam.usermanagement.model.dto.OtpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service("emailOtpProvider")
public class EmailOtpProvider implements OtpProvider {
    @Override
    public void send(OtpRequest request) {
        log.info("Sending EMAIL OTP to {} => {}", request.destination(), request.code());
        // call real email gateway
    }
}