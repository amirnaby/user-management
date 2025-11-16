package com.niam.usermanagement.service;

import com.niam.usermanagement.model.payload.request.AuthenticationRequest;
import com.niam.usermanagement.model.payload.request.ChangePasswordRequest;
import com.niam.usermanagement.model.payload.request.RegisterRequest;
import com.niam.usermanagement.model.payload.request.ResetPasswordRequest;
import com.niam.usermanagement.model.payload.response.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletRequest servletRequest);

    void changePassword(ChangePasswordRequest request, HttpServletRequest servletRequest);

    void resetPassword(ResetPasswordRequest request, HttpServletRequest servletRequest);
}