package com.niam.usermanagement.model.dto;

/**
 * Data object passed to OTP providers.
 */
public record OtpRequest(String destination, String code) {
}