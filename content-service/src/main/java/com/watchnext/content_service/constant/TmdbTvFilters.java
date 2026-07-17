package com.watchnext.content_service.constant;

// 1. Constantes TMDB para filtrar listas curadas de TV a solo contenido guionizado relevante.
// 2. TMDB separa valores multiples con "|" (OR) o "," (AND); with_type requiere OR: "," da 0 resultados
//    (Scripted AND Miniseries simultaneamente es imposible), verificado empiricamente contra /discover/tv.
public final class TmdbTvFilters {

    private TmdbTvFilters() {
    }

    // ---------- with_type (TMDB TV type codes) ----------
    public static final String TYPE_SCRIPTED = "4";
    public static final String TYPE_MINISERIES = "2";
    public static final String ALLOWED_TYPES = TYPE_SCRIPTED + "|" + TYPE_MINISERIES;

    // ---------- without_genres (taxonomia de generos de TV, distinta de movies) ----------
    public static final String GENRE_NEWS = "10763";
    public static final String GENRE_REALITY = "10764";
    public static final String GENRE_SOAP = "10766";
    public static final String GENRE_TALK = "10767";
    public static final String GENRE_KIDS = "10762";
    public static final String GENRE_FAMILY = "10751";
    public static final String GENRE_DOCUMENTARY = "99";

    public static final String BLACKLISTED_GENRES = String.join(
        "|", GENRE_SOAP, GENRE_REALITY, GENRE_TALK, GENRE_NEWS, GENRE_KIDS, GENRE_FAMILY, GENRE_DOCUMENTARY
    );
}
