package com.watchnext.user_service.exception;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class ProfileNotFound extends WatchNextException {

    public ProfileNotFound() {
        super(HttpStatus.NOT_FOUND, "Perfil no encontrado");
    }
}
