package com.niam.usermanagement.service;

import com.niam.usermanagement.payload.request.AuthenticationRequest;
import com.niam.usermanagement.payload.request.RegisterRequest;
import com.niam.usermanagement.payload.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse authenticate(AuthenticationRequest request);
}