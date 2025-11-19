package com.niam.usermanagement.exception;

import com.niam.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class AuthenticationException extends BusinessException {
    public AuthenticationException(Integer responseCode, Integer reasonCode, String responseDescription) {
        super(responseCode, reasonCode, responseDescription);
    }

    public AuthenticationException(String responseDescription) {
        super(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.series().value(), responseDescription);
    }
}