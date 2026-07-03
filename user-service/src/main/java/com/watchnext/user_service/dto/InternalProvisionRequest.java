package com.watchnext.user_service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InternalProvisionRequest(
    @NotNull UUID authUserId
) {}
