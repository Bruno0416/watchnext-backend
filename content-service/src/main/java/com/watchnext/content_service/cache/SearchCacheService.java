package com.watchnext.content_service.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchnext.content_service.config.SearchProperties;
import com.watchnext.content_service.dto.search.ParsedQuery;
import com.watchnext.content_service.dto.search.SearchResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import reactor.core.publisher.Mono;

@Service
public class SearchCacheService {

    private static final Logger log = LoggerFactory.getLogger(
        SearchCacheService.class
    );
    private static final String KEY_PREFIX = "search:v1:";

    private final ReactiveStringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final SearchProperties props;

    public SearchCacheService(
        ReactiveStringRedisTemplate redis,
        ObjectMapper mapper,
        SearchProperties props
    ) {
        this.redis = redis;
        this.mapper = mapper;
        this.props = props;
    }

    public Mono<SearchResponse> get(ParsedQuery query, String language) {
        // 1. buscar json en redis usando la llave
        return redis
            .opsForValue()
            .get(buildKey(query, language))
            .flatMap(json -> {
                try {
                    // 2. parsear json a objeto de respuesta
                    SearchResponse response = mapper.readValue(
                        json,
                        SearchResponse.class
                    );
                    return Mono.just(response);
                } catch (JsonProcessingException e) {
                    log.warn(
                        "Error deserializando caché de búsqueda. Ignorando caché.",
                        e
                    );
                    return Mono.empty();
                }
            });
    }

    public Mono<SearchResponse> put(
        ParsedQuery query,
        String language,
        SearchResponse response
    ) {
        // 1. determinar tiempo de expiracion basado en precision de busqueda
        Duration ttl = query.hasYear()
            ? props.getCacheTtlPrecise()
            : props.getCacheTtlGeneric();

        try {
            // 2. transformar respuesta a json y guardar en redis
            String json = mapper.writeValueAsString(response);

            return redis
                .opsForValue()
                .set(buildKey(query, language), json, ttl)
                .thenReturn(response);
        } catch (JsonProcessingException e) {
            log.warn(
                "Error serializando respuesta para caché. No se guardará.",
                e
            );
            return Mono.just(response);
        }
    }

    private String buildKey(ParsedQuery query, String language) {
        // 1. construir string base combinando parametros principales
        String raw = String.join(
            "|",
            query.cleanQuery(),
            String.valueOf(query.year()),
            String.valueOf(query.mediaType()),
            language
        );
        // 2. generar hash md5 para usar como sufijo en la llave
        String hash = DigestUtils.md5DigestAsHex(
            raw.getBytes(StandardCharsets.UTF_8)
        );
        return KEY_PREFIX + hash;
    }
}
