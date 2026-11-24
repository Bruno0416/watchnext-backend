package com.watchnext.news_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record NewsRequest(
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede exceder los 150 caracteres")
    String title,

    @NotBlank(message = "El slug es obligatorio")
    @Size(max = 160, message = "El slug no puede exceder los 160 caracteres")
    @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "El slug solo admite minúsculas, números y guiones")
    String slug,

    @NotBlank(message = "El resumen es obligatorio")
    @Size(max = 300, message = "El resumen no puede exceder los 300 caracteres")
    String summary,

    @NotBlank(message = "El contenido es obligatorio")
    String content,

    @Size(max = 512, message = "La url de la imagen no puede exceder los 512 caracteres")
    String imageUrl,

    // si no viene se publica ahora
    Instant publishedAt
) {}
