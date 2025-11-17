package com.niam.usermanagement.model.dto;

public record CaptchaValidateRequest(String token, String response) {
}
