package com.watchnext.news_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class NewsNotFound extends WatchNextException {

    public NewsNotFound(String ref) {
        super(HttpStatus.NOT_FOUND, "La noticia '" + ref + "' no existe");
    }
}
