package com.niam.usermanagement.model.enums;

/**
 * Available Captcha Provide channels.
 */
public enum CaptchaProviderType {
    LOCAL("localCaptchaProvider"),
    DEV("devCaptchaProvider");

    private final String beanName;

    CaptchaProviderType(String beanName) {
        this.beanName = beanName;
    }

    public String beanName() {
        return beanName;
    }
}