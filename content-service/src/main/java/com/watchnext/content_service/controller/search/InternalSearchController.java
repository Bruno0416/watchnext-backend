package com.watchnext.content_service.controller.search;

import com.watchnext.content_service.dto.search.SearchResponse;
import com.watchnext.content_service.service.search.SearchService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/content/internal/search")
@Validated
public class InternalSearchController {

    private final SearchService searchService;

    /*
      Busqueda interna de contenido tmdb, protegida por token de servicio.

      Limitacion de paginacion: cuando types {movie, tv}, el parametro page se ignora
      y siempre se devuelve pagina 1 de cada tipo. Para paginacion profunda, usar
      types=person o types vacio (path /search/multi nativo de tmdb).
     */
    // ---------- busqueda interna ----------
    @GetMapping
    public Mono<SearchResponse> search(
        @RequestParam("q") @NotBlank(message = "La consulta de búsqueda no puede estar vacía") @Size(min = 1, max = 255, message = "La consulta debe tener entre 1 y 255 caracteres") String query,
        @RequestParam(value = "types", required = false) Set<@Pattern(regexp = "^(movie|tv|person)$", message = "El tipo debe ser movie, tv, o person") String> types,
        @RequestParam(value = "page", defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") int page,
        @RequestParam(value = "language", defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        // 1. delegar al servicio con los types normalizados
        Set<String> resolvedTypes = types == null ? Set.of() : types;
        return searchService.search(query, language, resolvedTypes);
    }
}
