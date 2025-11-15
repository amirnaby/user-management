package com.niam.usermanagement.otp.provider;

public interface OtpProvider {
    void sendOtp(String destination, String code);
}