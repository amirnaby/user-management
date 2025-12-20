package com.niam.usermanagement.service;

import com.niam.usermanagement.model.payload.request.AuthenticationRequest;
import com.niam.usermanagement.model.payload.request.ChangePasswordRequest;
import com.niam.usermanagement.model.payload.request.ResetPasswordRequest;
import com.niam.usermanagement.model.payload.request.UserDTO;
import com.niam.usermanagement.model.payload.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse register(UserDTO request);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    void changePassword(ChangePasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}