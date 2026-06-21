package com.watchnext.list_service.dto;

import com.watchnext.common.dto.ContentRefRequest;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Value;

@Value
public class ItemsRequest {

    @NotEmpty(message = "Los items no pueden estar vacíos")
    private List<ContentRefRequest> items;
}
