package com.watchnext.auth_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class InvalidSocialToken extends WatchNextException {

    public InvalidSocialToken(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
