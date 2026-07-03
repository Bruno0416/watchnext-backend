package com.watchnext.user_service.exception;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class FollowRequestNotFound extends WatchNextException {

    public FollowRequestNotFound() {
        super(HttpStatus.NOT_FOUND, "Solicitud de seguimiento no encontrada");
    }
}
