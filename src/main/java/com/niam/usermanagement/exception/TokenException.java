package com.niam.usermanagement.exception;

import com.niam.common.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenException extends BusinessException {
    public TokenException(Integer responseCode, Integer reasonCode, String responseDescription) {
        super(responseCode, reasonCode, responseDescription);
    }

    public TokenException(String responseDescription) {
        super(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.series().value(), responseDescription);
    }
}