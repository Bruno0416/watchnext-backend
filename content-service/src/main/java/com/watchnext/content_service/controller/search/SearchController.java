package com.watchnext.content_service.controller.search;

import com.watchnext.content_service.dto.search.SearchResponse;
import com.watchnext.content_service.service.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import reactor.core.publisher.Mono;

@Deprecated(since = "1.1.0", forRemoval = true)
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/content/search")
@Validated
public class SearchController {

    private final SearchService searchService;

    // ---------- busqueda publica ----------
    @Deprecated(since = "1.1.0", forRemoval = true)
	@GetMapping
    public Mono<SearchResponse> search(
        @RequestParam("q") @NotBlank(message = "La consulta de búsqueda no puede estar vacía") @Size(min = 1, max = 255, message = "La consulta debe tener entre 1 y 255 caracteres") String query,
        @RequestParam(
            value = "language",
            defaultValue = "en-US"
        ) @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return searchService.search(query, language);
    }
}
