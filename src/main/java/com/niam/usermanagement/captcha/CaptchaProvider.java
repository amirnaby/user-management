package com.niam.usermanagement.captcha;

public interface CaptchaProvider {
    /**
     * Generate a captcha challenge. The returned CaptchaResponse contains:
     *  - token: server-side id to use for validation
     *  - imageBase64: base64-encoded png for display in UI
     *  - expiresInSeconds: TTL
     */
    CaptchaResponse generate();

    /**
     * Validate a client response (userResponse) for a given token
     */
    boolean validate(String token, String userResponse);
}