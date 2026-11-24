package com.watchnext.user_service.exception;

import com.watchnext.common.exceptions.WatchNextException;
import org.springframework.http.HttpStatus;

public class OnboardingAlreadyCompleted extends WatchNextException {

    public OnboardingAlreadyCompleted() {
        super(HttpStatus.CONFLICT, "El onboarding ya fue completado");
    }
}
