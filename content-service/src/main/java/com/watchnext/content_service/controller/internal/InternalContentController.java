package com.watchnext.content_service.controller.internal;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.dto.internal.ContentBasicDetail;
import com.watchnext.content_service.service.bulk.ContentBulkService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/content/internal")
@RequiredArgsConstructor
@Validated
public class InternalContentController {

    private final ContentBulkService contentBulkService;

    // ---------- consultas masivas ----------
    // 1. obtener detalles basicos de multiples contenidos en paralelo
    @PostMapping("/bulk")
    public Mono<ResponseEntity<List<ContentBasicDetail>>> getBulkContent(
        @RequestBody @NotEmpty(message = "La lista de items no puede estar vacía") @Valid List<ContentRefRequest> items,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentBulkService
            .fetchBulkContent(items, language)
            .map(ResponseEntity::ok);
    }
}
