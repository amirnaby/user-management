package com.niam.usermanagement.model.payload.request;

public record CaptchaValidateRequest(String token, String response) {
}