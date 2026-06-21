package com.watchnext.list_service.dto;

import com.watchnext.common.dto.ContentRefRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class CreateListRequest {

    @NotBlank(message = "Nombre es obligatorio")
    @Size(max = 80, message = "Nombre no puede exceder los 80 caracteres")
    private String name;

    @Size(
        min = 1,
        max = 300,
        message = "Descripción no puede exceder los 300 caracteres"
    )
    private String description;

    @Valid
    private List<ContentRefRequest> contentRefs = new ArrayList<>();
}
