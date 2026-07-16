package com.watchnext.user_service.exception;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class InvalidCountry extends WatchNextException {

    public InvalidCountry(String country) {
        super(HttpStatus.BAD_REQUEST, "Country code '" + country + "' is not a valid ISO 3166-1 alpha-2 code");
    }
}
