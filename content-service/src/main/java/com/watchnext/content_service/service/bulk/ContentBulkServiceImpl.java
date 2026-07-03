package com.watchnext.content_service.service.bulk;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.dto.internal.ContentBasicDetail;
import com.watchnext.common.enums.MediaType;
import com.watchnext.content_service.dto.movies.MovieDetails;
import com.watchnext.content_service.dto.tv.TvDetails;
import com.watchnext.content_service.service.content.ContentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ContentBulkServiceImpl implements ContentBulkService {

    private static final int CONCURRENCY = 8; // tope para no pasar el rate limit de TMDB

    private final ContentService contentService;

    @Override
    public Mono<List<ContentBasicDetail>> fetchBulkContent(
        List<ContentRefRequest> requests,
        String language
    ) {
        // 1. recorrer lista de peticiones eliminando duplicados
        return Flux.fromIterable(requests)
            .distinct()
            // 2. procesar en paralelo respetando el limite de concurrencia
            .flatMapSequential(ref -> fetchOne(ref, language), CONCURRENCY)
            .collectList();
    }

    private Mono<ContentBasicDetail> fetchOne(
        ContentRefRequest ref,
        String language
    ) {
        Integer id = ref.tmdbId().intValue();

        if (ref.mediaType() == MediaType.MOVIE) {
            return contentService
                .getMovieDetails(id, language)
                .map(this::toBasic)
                .onErrorResume(e -> Mono.empty());
        } else {
            return contentService
                .getTvDetails(id, language)
                .map(this::toBasic)
                .onErrorResume(e -> Mono.empty());
        }
    }

    private ContentBasicDetail toBasic(MovieDetails m) {
        return new ContentBasicDetail(
            m.id(),
            MediaType.MOVIE,
            m.title(),
            m.posterPath(),
            m.voteAverage(),
            m.releaseDate(),
            m.runtime(),
            null
        );
    }

    private ContentBasicDetail toBasic(TvDetails t) {
        return new ContentBasicDetail(
            t.id(),
            MediaType.TV,
            t.name(),
            t.posterPath(),
            t.voteAverage(),
            t.firstAirDate(),
            null,
            t.numberOfSeasons()
        );
    }
}
