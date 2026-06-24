package com.watchnext.content_service.pipeline;

import com.watchnext.content_service.dto.search.SearchResult;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ResultRanker {

    public List<SearchResult> rank(
        List<SearchResult> results,
        String cleanQuery
    ) {
        // 1. retornar lista vacia si no hay resultados
        if (results == null || results.isEmpty()) return List.of();
        String q = cleanQuery == null ? "" : cleanQuery.toLowerCase().trim();

        // 2. ordenar resultados de mayor a menor puntuacion
        return results
            .stream()
            .sorted(
                Comparator.comparingDouble((SearchResult r) ->
                    score(r, q)
                ).reversed()
            )
            .toList();
    }

    private double score(SearchResult r, String query) {
        double score = 0;
        String title = r.title() == null ? "" : r.title().toLowerCase();

        // 1. Relevancia textual (Lo más importante)
        if (title.equals(query)) score += 100;
        else if (title.startsWith(query)) score += 50;
        else if (title.contains(query)) score += 25;

        // 2. Bonus por data de TMDB (Acotados para no sobrepasar el match de texto)
        score += Math.min(r.popularity() / 100.0, 10);
        score += Math.min(r.voteAverage() / 2.0, 5);

        // 3. Penalizar contenido "basura" (Sin póster o sin sinopsis)
        if (r.posterPath() == null) score -= 5;
        if (r.overview() == null || r.overview().isBlank()) score -= 3;

        return score;
    }
}
