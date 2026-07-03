package com.watchnext.common.dto.internal;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ConfirmationEmailRequest(
    @Email @NotBlank String to,
    @NotBlank String code
) {}
