package com.watchnext.auth_service.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AuthResponse {

    String token;
    String refreshToken;
}
