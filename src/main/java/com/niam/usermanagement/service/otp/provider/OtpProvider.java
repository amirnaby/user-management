package com.niam.usermanagement.service.otp.provider;

public interface OtpProvider {
    void sendOtp(String destination, String code);
}