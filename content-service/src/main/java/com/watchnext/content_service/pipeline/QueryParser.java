package com.watchnext.content_service.pipeline;

import com.watchnext.content_service.dto.search.ParsedQuery;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class QueryParser {

    private static final Pattern YEAR_PATTERN = Pattern.compile(
        "\\b(18[89]\\d|19\\d{2}|20[0-3]\\d)\\b"
    );

    private static final Map<String, String> TYPE_KEYWORDS =
        new LinkedHashMap<>();

    static {
        TYPE_KEYWORDS.put("serie", "tv");
        TYPE_KEYWORDS.put("series", "tv");
        TYPE_KEYWORDS.put("anime", "tv");
        TYPE_KEYWORDS.put("pelicula", "movie");
        TYPE_KEYWORDS.put("peliculas", "movie");
        TYPE_KEYWORDS.put("movie", "movie");
    }

    public ParsedQuery parse(String normalizedQuery) {
        // 1. retornar query vacio si el texto original es nulo o blanco
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return new ParsedQuery("", null, null);
        }

        String working = normalizedQuery;
        int maxYear = Year.now().getValue() + 2;

        // 2. extraer el año de la busqueda usando expresion regular
        Integer year = null;
        Matcher m = YEAR_PATTERN.matcher(working);
        while (m.find()) {
            int candidate = Integer.parseInt(m.group());
            if (candidate <= maxYear) {
                year = candidate;
                working = working.replaceFirst("\\b" + candidate + "\\b", " ");
                break;
            }
        }

        // 3. extraer el tipo de contenido (pelicula o serie) basado en palabras clave
        String mediaType = null;
        for (Map.Entry<String, String> e : TYPE_KEYWORDS.entrySet()) {
            String kw = e.getKey();
            if (working.matches(".*\\b" + kw + "\\b.*")) {
                mediaType = e.getValue();
                working = working.replaceAll("\\b" + kw + "\\b", " ");
                break;
            }
        }

        // 4. limpiar espacios adicionales y retornar objeto parseado
        String cleanQuery = working.replaceAll("\\s+", " ").trim();
        return new ParsedQuery(cleanQuery, year, mediaType);
    }
}
