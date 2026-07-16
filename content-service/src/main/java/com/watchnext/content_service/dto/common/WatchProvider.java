package com.watchnext.content_service.dto.common;

/**
 * Plataforma de streaming disponible para un contenido en un pais dado.
 * Reemplaza al antiguo WatchProviders (TMDB/JustWatch): solo expone lo
 * necesario para el front (nombre, iconos SVG light/dark y link al contenido).
 */
public record WatchProvider(
    String name,
    String iconLight,
    String iconDark,
    String link
) {}
