package com.watchnext.content_service.client;

import com.watchnext.content_service.dto.common.ShowStreamingRaw;
import com.watchnext.content_service.dto.common.StreamingOptionSummary;
import com.watchnext.content_service.exceptions.ErrorFetchingWatchProviders;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/*
  Cliente reactivo para la Streaming Availability API (RapidAPI), que reemplaza
  a TMDB/JustWatch como fuente de watch providers. Replica el patron de TmdbClient
  (WebClient + timeout + auth por headers), pero autentica via headers de RapidAPI
  en lugar de un Bearer token.
 */
@Component
public class StreamingAvailabilityClient {

    private final WebClient webClient;

    // ---------- configuracion ----------
    public StreamingAvailabilityClient(
        WebClient.Builder builder,
        @Value("${streaming-availability.api.base-url}") String baseUrl,
        @Value("${streaming-availability.api.host}") String apiHost,
        @Value("${streaming-availability.api.key}") String apiKey
    ) {
        HttpClient httpClient = HttpClient.create().responseTimeout(
            Duration.ofSeconds(10)
        );

        this.webClient = builder
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader("X-RapidAPI-Key", apiKey)
            .defaultHeader("X-RapidAPI-Host", apiHost)
            .build();
    }

    // ---------- disponibilidad ----------
    public Mono<List<StreamingOptionSummary>> getShowStreamingOptions(
        String showId,
        String country
    ) {
        // 1. consultar api de streaming usando el id local o tmdb
        // 2. procesar mapeo de paises y retornar lista de opciones filtradas
        return webClient
            .get()
            .uri(uriBuilder ->
                uriBuilder
                    .path("/shows/{id}")
                    .queryParam("country", country)
                    .queryParam("series_granularity", "show")
                    .build(showId)
            )
            .retrieve()
            .onStatus(HttpStatusCode::isError, response ->
                Mono.error(
                    new ErrorFetchingWatchProviders(
                        "Error al obtener streaming options para " + showId
                    )
                )
            )
            .bodyToMono(ShowStreamingRaw.class)
            .map(raw -> extractOptions(raw, country));
    }

    // --- helper privado ---
    private List<StreamingOptionSummary> extractOptions(
        ShowStreamingRaw raw,
        String country
    ) {
        // 1. extraer opciones del pais solicitado
        if (raw == null || raw.streamingOptions() == null) return List.of();

        List<ShowStreamingRaw.StreamingOptionRaw> options = raw
            .streamingOptions()
            .get(country.toLowerCase(Locale.ROOT));
        if (options == null) return List.of();

        return options
            .stream()
            .filter(o -> o.service() != null && o.service().imageSet() != null)
            .map(o -> new StreamingOptionSummary(
                o.service().id(),
                o.service().name(),
                o.service().imageSet().lightThemeImage(),
                o.service().imageSet().whiteImage(),
                o.link(),
                o.type()
            ))
            .toList();
    }
}
