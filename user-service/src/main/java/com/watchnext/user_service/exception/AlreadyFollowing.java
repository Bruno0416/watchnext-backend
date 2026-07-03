package com.watchnext.user_service.exception;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class AlreadyFollowing extends WatchNextException {

    public AlreadyFollowing() {
        super(HttpStatus.CONFLICT, "Ya sigues o tienes una solicitud pendiente a este usuario");
    }
}
