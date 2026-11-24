package com.watchnext.news_service.exceptions;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class SlugAlreadyExists extends WatchNextException {

    public SlugAlreadyExists(String slug) {
        super(HttpStatus.CONFLICT, "Ya existe una noticia con el slug '" + slug + "'");
    }
}
