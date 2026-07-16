package com.watchnext.content_service.dto.common;

import java.util.List;
import java.util.Map;

/*
  Forma cruda de la respuesta de GET /shows/{id} de la Streaming Availability API.
  Solo modela los campos que usamos; se descarta el resto (cast, imagenes de
  show, precios, etc.) al mapear a WatchProvider.
 */
public record ShowStreamingRaw(
    String id,
    Map<String, List<StreamingOptionRaw>> streamingOptions
) {
    public record StreamingOptionRaw(
        ServiceInfoRaw service,
        String type,
        String link
    ) {}

    public record ServiceInfoRaw(
        String id,
        String name,
        ServiceImageSetRaw imageSet
    ) {}

    public record ServiceImageSetRaw(
        String lightThemeImage,
        String darkThemeImage,
        String whiteImage
    ) {}
}
