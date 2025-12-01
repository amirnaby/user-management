package com.niam.usermanagement.model.payload.request;

public record OtpRequest(String destination, String code) {
}