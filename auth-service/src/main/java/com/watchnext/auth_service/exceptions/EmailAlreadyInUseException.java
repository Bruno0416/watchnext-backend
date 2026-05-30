package com.watchnext.auth_service.exceptions;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(String message) {
        super(message);
    }
}
