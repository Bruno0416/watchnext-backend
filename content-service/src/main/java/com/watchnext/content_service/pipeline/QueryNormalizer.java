package com.watchnext.content_service.pipeline;

import java.text.Normalizer;
import org.springframework.stereotype.Component;

@Component
public class QueryNormalizer {

    public String normalize(String raw) {
        // 1. retornar vacio si el texto es nulo o blanco
        if (raw == null || raw.isBlank()) return "";

        // 2. separar caracteres diacriticos de las letras
        String decomposed = Normalizer.normalize(raw, Normalizer.Form.NFD);

        // 3. eliminar todos los acentos y marcas
        String withoutAccents = decomposed.replaceAll(
            "\\p{InCombiningDiacriticalMarks}+",
            ""
        );

        // 4. convertir a minusculas, dejar solo alfanumericos y limpiar espacios extra
        return withoutAccents
            .toLowerCase()
            .replaceAll("[^a-z0-9\\s]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }
}
