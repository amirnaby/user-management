package com.niam.usermanagement.model.payload.request;

/**
 * Data object passed to OTP providers.
 */
public record OtpRequest(String destination, String code) {
}