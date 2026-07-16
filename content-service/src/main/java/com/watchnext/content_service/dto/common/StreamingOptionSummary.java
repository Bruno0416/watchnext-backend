package com.watchnext.content_service.dto.common;

/*
  Representacion intermedia de una opcion de streaming ya extraida para el
  pais solicitado, previa a la deduplicacion por servicio en ContentServiceImpl.
  El campo 'type' (subscription|rent|buy|free|addon) se usa solo para decidir
  que opcion priorizar cuando un mismo servicio aparece mas de una vez.
 */
public record StreamingOptionSummary(
    String serviceId,
    String name,
    String iconLight,
    String iconDark,
    String link,
    String type
) {}
