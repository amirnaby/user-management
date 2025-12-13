package com.niam.usermanagement.exception;

import com.niam.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(Integer responseCode, Integer reasonCode, String responseDescription) {
        super(responseCode, reasonCode, responseDescription);
    }

    public UserNotFoundException(String responseDescription) {
        super(401, 401, responseDescription);
    }
}