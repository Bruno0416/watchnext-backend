package com.watchnext.content_service.controller.internal;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.dto.internal.ContentBasicDetail;
import com.watchnext.content_service.service.bulk.ContentBulkService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/content/internal")
@RequiredArgsConstructor
public class InternalContentController {

    private final ContentBulkService contentBulkService;

    // 1. obtener detalles basicos de multiples contenidos en paralelo
    @PostMapping("/bulk")
    public Mono<ResponseEntity<List<ContentBasicDetail>>> getBulkContent(
        @RequestBody List<ContentRefRequest> items,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentBulkService
            .fetchBulkContent(items, language)
            .map(ResponseEntity::ok);
    }
}
