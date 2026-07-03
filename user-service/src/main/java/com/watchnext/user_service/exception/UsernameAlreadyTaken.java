package com.watchnext.user_service.exception;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class UsernameAlreadyTaken extends WatchNextException {

    public UsernameAlreadyTaken(String username) {
        super(HttpStatus.CONFLICT, "El username '" + username + "' ya está en uso");
    }
}
