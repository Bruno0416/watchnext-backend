package com.watchnext.auth_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class UserNotFound extends WatchNextException {

    public UserNotFound(String email) {
        super(HttpStatus.NOT_FOUND, "User with email " + email + " not found");
    }

    public UserNotFound() {
        super(HttpStatus.NOT_FOUND, "User not found");
    }
}
