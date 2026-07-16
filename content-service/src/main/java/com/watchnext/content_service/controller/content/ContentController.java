package com.watchnext.content_service.controller.content;

import com.watchnext.common.enums.MediaType;
import com.watchnext.common.enums.TimeWindow;
import com.watchnext.content_service.dto.common.TrendingResponse;
import com.watchnext.content_service.dto.movies.CollectionDetails;
import com.watchnext.content_service.service.content.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/content")
@Validated
public class ContentController {

    private final ContentService contentService;

    // ---------- metadatos globales ----------
    @GetMapping("/trending/{mediaType}/{timeWindow}")
    public Mono<ResponseEntity<TrendingResponse>> getTrending(
        @PathVariable MediaType mediaType,
        @PathVariable TimeWindow timeWindow,
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page
    ) {
        return contentService
            .getTrending(mediaType, timeWindow, page)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/collections/{id}")
    public Mono<ResponseEntity<CollectionDetails>> getCollection(
        @PathVariable @Positive(message = "El id debe ser un número positivo") Integer id,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getCollection(id, language)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
