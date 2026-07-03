package com.watchnext.user_service.exception;

import com.watchnext.common.exceptions.WatchNextException;
import java.io.IOException;
import org.springframework.http.HttpStatus;

public class AvatarStorage extends WatchNextException {

    public AvatarStorage(String message, IOException e) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message + " " + e.getMessage());
    }
}
