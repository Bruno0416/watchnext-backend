package com.watchnext.auth_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class InvalidConfirmationCode extends WatchNextException {

    public InvalidConfirmationCode(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
