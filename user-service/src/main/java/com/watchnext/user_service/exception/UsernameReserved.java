package com.watchnext.user_service.exception;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class UsernameReserved extends WatchNextException {

    public UsernameReserved(String username) {
        super(HttpStatus.CONFLICT, "El username '" + username + "' está reservado");
    }
}
