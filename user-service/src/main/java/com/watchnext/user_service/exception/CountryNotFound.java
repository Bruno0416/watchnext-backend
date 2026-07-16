package com.watchnext.user_service.exception;

import org.springframework.http.HttpStatus;

import com.watchnext.common.exceptions.WatchNextException;

public class CountryNotFound extends WatchNextException{

	public CountryNotFound() {
		super(HttpStatus.NOT_FOUND, "Pais no encontrado");

	}


}
