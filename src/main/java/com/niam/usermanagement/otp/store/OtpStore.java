package com.niam.usermanagement.otp.store;

public interface OtpStore {
    void saveOtp(String username, String otp, long ttlSeconds);

    String getOtp(String username);

    void removeOtp(String username);
}