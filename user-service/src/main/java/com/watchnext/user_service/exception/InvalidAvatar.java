package com.watchnext.user_service.exception;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class InvalidAvatar extends WatchNextException {

    public InvalidAvatar(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
