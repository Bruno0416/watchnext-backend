package com.watchnext.list_service.dto;

import com.watchnext.common.dto.ContentRefRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateListRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80, message = "El nombre no puede exceder los 80 caracteres")
    String name,

    @Size(min = 1, max = 300, message = "La descripción no puede exceder los 300 caracteres")
    String description,

    List<@Valid ContentRefRequest> contentRefs
) {
    public CreateListRequest {
        if (contentRefs == null) contentRefs = List.of();
    }
}
