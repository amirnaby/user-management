package com.niam.usermanagement.model.enums;

/**
 * Available OTP delivery channels.
 */
public enum OtpProviderType {
    SMS("smsOtpProvider"),
    EMAIL("emailOtpProvider"),
    DEV("devOtpProvider");

    private final String beanName;

    OtpProviderType(String beanName) {
        this.beanName = beanName;
    }

    public String beanName() {
        return beanName;
    }
}