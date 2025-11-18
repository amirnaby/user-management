package com.niam.usermanagement.model.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaResponse {
    private String token;
    private String imageBase64;
    private int expiresInSeconds;
}