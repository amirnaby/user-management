package com.niam.usermanagement.service.otp.provider;

import com.niam.usermanagement.model.payload.request.OtpRequest;

/**
 * Generic OTP provider interface used for sending OTP codes.
 */
public interface OtpProvider {

    /**
     * Sends an OTP code to a destination.
     *
     * @param request encapsulates destination and OTP values.
     */
    void send(OtpRequest request);
}