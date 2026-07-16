package com.watchnext.common.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class RateLimitExceeded extends WatchNextException {

    private final int retryAfterSeconds;

    public RateLimitExceeded(String message, int retryAfterSeconds) {
        super(HttpStatus.TOO_MANY_REQUESTS, message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
