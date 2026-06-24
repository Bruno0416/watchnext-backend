package com.watchnext.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class ContentRef {

    // ID para el contenido (Pelicula o Serie)
    @Column(name = "tmdb_id", nullable = false)
    private Integer tmdbId;

    // Enum del tipo de contenido
    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private MediaType mediaType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentRef that)) return false;
        return (
            Objects.equals(tmdbId, that.tmdbId) && mediaType == that.mediaType
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(tmdbId, mediaType);
    }
}
