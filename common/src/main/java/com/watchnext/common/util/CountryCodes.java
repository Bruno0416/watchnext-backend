package com.watchnext.common.util;

import java.util.Locale;
import java.util.Set;

/**
 * Utilidad compartida para normalizar y validar codigos de pais ISO 3166-1 alpha-2.
 * Usada tanto por el perfil de usuario (user-service) como por la resolucion de
 * region para proveedores externos (content-service).
 */
public final class CountryCodes {

    // 1. universo valido de codigos alpha-2 segun la JVM (respaldado por ISO 3166-1).
    private static final Set<String> VALID_CODES = Set.of(Locale.getISOCountries());

    private CountryCodes() {}

    // 1. normaliza un codigo de pais: recorta espacios y lo pasa a mayusculas.
    public static String normalize(String rawCode) {
        if (rawCode == null) return null;
        return rawCode.trim().toUpperCase(Locale.ROOT);
    }

    // 1. valida que un codigo (ya normalizado o no) pertenezca al set ISO 3166-1 alpha-2.
    public static boolean isValid(String rawCode) {
        String normalized = normalize(rawCode);
        return normalized != null && VALID_CODES.contains(normalized);
    }
}
