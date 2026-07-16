package com.watchnext.content_service.controller.tv;

import com.watchnext.content_service.dto.common.GenreListResponse;
import com.watchnext.content_service.dto.common.ReviewResponse;
import com.watchnext.content_service.dto.common.WatchProvider;
import com.watchnext.content_service.dto.tv.TvDetails;
import com.watchnext.content_service.dto.tv.TvEpisode;
import com.watchnext.content_service.dto.tv.TvListResponse;
import com.watchnext.content_service.dto.tv.TvSeasonDetail;
import com.watchnext.content_service.service.content.ContentService;
import lombok.RequiredArgsConstructor;
import com.watchnext.common.security.GatewayHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/content/tv")
@Validated
public class SeriesController {

    private final ContentService contentService;

    // ---------- detalles y metadatos ----------
    @GetMapping("/{id}")
    public Mono<ResponseEntity<TvDetails>> getTvDetails(
        @PathVariable @Positive(message = "El id debe ser un número positivo") Integer id,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getTvDetails(id, language)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/watch-providers")
    public Mono<ResponseEntity<List<WatchProvider>>> getTvWatchProviders(
        @PathVariable @Positive(message = "El id debe ser un número positivo") Integer id,
@RequestHeader(GatewayHeaders.COUNTRY) String country,
@RequestHeader(GatewayHeaders.REGION) String region
    ) {
        return contentService
            .getTvWatchProviders(id, country, region)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/{id}/season/{seasonNumber}")
    public Mono<ResponseEntity<TvSeasonDetail>> getTvSeasonDetail(
        @PathVariable @Positive(message = "El id debe ser un número positivo") Integer id,
        @PathVariable @PositiveOrZero(message = "El número de temporada debe ser 0 o mayor") Integer seasonNumber,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getTvSeasonDetail(id, seasonNumber, language)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/season/{seasonNumber}/episode/{episodeNumber}")
    public Mono<ResponseEntity<TvEpisode>> getTvEpisodeDetail(
        @PathVariable @Positive(message = "El id debe ser un número positivo") Integer id,
        @PathVariable @PositiveOrZero(message = "El número de temporada debe ser 0 o mayor") Integer seasonNumber,
        @PathVariable @Positive(message = "El número de episodio debe ser un número positivo") Integer episodeNumber,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getTvEpisodeDetail(id, seasonNumber, episodeNumber, language)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/reviews")
    public Mono<ResponseEntity<ReviewResponse>> getTvReviews(
        @PathVariable @Positive(message = "El id debe ser un número positivo") Integer id,
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page
    ) {
        return contentService
            .getTvReviews(id, page)
            .map(ResponseEntity::ok);
    }

    // ---------- listados de series ----------
    @GetMapping("/on-the-air")
    public Mono<ResponseEntity<TvListResponse>> getOnTheAir(
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getOnTheAir(page, language)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/popular")
    public Mono<ResponseEntity<TvListResponse>> getPopularTv(
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getPopularTv(page, language)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/top-rated")
    public Mono<ResponseEntity<TvListResponse>> getTopRatedTv(
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getTopRatedTv(page, language)
            .map(ResponseEntity::ok);
    }

    // ---------- busqueda y filtrado ----------
    @GetMapping("/discover")
    public Mono<ResponseEntity<TvListResponse>> discoverTv(
        @RequestParam(required = false) @Pattern(regexp = "^\\d+(,\\d+)*$", message = "Los géneros deben ser una lista de IDs separados por comas") String genres,
        @RequestParam(name = "sort_by", defaultValue = "popularity.desc")
        @Pattern(
            regexp = "^(popularity|first_air_date|vote_average|vote_count|name)\\.(asc|desc)$",
            message = "sort_by debe coincidir con las opciones de ordenamiento permitidas por TMDB para TV"
        ) String sortBy,
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language,
        @RequestHeader(value = "Region", required = false) @Size(max = 20, message = "La región no puede exceder los 20 caracteres") String region
    ) {
        return contentService
            .discoverTv(genres, sortBy, page, language, region)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/by-genre")
    public Mono<ResponseEntity<TvListResponse>> getSeriesByGenre(
        @RequestParam @NotEmpty(message = "El conjunto de géneros no puede estar vacío") Set<Integer> genre,
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "La página mínima es 1") @Max(value = 500, message = "La página máxima es 500") Integer page,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language,
        @RequestHeader(value = "Region", required = false) @Size(max = 20, message = "La región no puede exceder los 20 caracteres") String region
    ) {
        return contentService
            .getSeriesByGenre(genre, page, language, region)
            .map(ResponseEntity::ok);
    }

    @GetMapping("/genres")
    public Mono<ResponseEntity<GenreListResponse>> getTvGenres(
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService.getTvGenres(language).map(ResponseEntity::ok);
    }

}
