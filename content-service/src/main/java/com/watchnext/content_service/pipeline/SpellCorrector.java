package com.watchnext.content_service.pipeline;

import com.watchnext.content_service.config.SearchProperties;
import io.github.mightguy.spellcheck.symspell.api.DataHolder;
import io.github.mightguy.spellcheck.symspell.api.StringDistance;
import io.github.mightguy.spellcheck.symspell.common.DictionaryItem;
import io.github.mightguy.spellcheck.symspell.common.Murmur3HashFunction;
import io.github.mightguy.spellcheck.symspell.common.SpellCheckSettings;
import io.github.mightguy.spellcheck.symspell.common.SuggestionItem;
import io.github.mightguy.spellcheck.symspell.common.Verbosity;
import io.github.mightguy.spellcheck.symspell.common.WeightedDamerauLevenshteinDistance;
import io.github.mightguy.spellcheck.symspell.exception.SpellCheckException;
import io.github.mightguy.spellcheck.symspell.impl.InMemoryDataHolder;
import io.github.mightguy.spellcheck.symspell.impl.SymSpellCheck;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class SpellCorrector {

    private static final Logger log = LoggerFactory.getLogger(
        SpellCorrector.class
    );

    private static final String FALLBACK_LANG = "en";

    private final SearchProperties props;

    private final Map<String, SymSpellCheck> checkers =
        new ConcurrentHashMap<>();

    private final Map<String, Boolean> failedLangs = new ConcurrentHashMap<>();

    public SpellCorrector(SearchProperties props) {
        this.props = props;
    }

    // --- Inicializacion ---

    @PostConstruct
    void preload() {
        if (!props.getSpell().isEnabled()) {
            log.info(
                "[SpellCorrector] Corrección ortográfica deshabilitada (spell.enabled=false)."
            );
            return;
        }
        props
            .getSpell()
            .getDictionaries()
            .keySet()
            .stream()
            .map(key -> key.contains("-") ? key.split("-")[0] : key) // "en-domain" → "en"
            .distinct()
            .forEach(lang -> {
                try {
                    getOrCreate(lang);
                } catch (Exception e) {
                    log.warn(
                        "[SpellCorrector] Fallo al precargar idioma '{}': {}",
                        lang,
                        e.getMessage()
                    );
                }
            });
    }

    public String correct(String query, String language) {
        if (!props.getSpell().isEnabled() || query == null || query.isBlank()) {
            return query;
        }

        // 1. Extraer idioma y obtener o crear instancia de correccion
        String lang = extractLang(language);
        SymSpellCheck checker = getOrCreate(lang);
        if (checker == null) {
            return query;
        }

        try {
            // 2. Aplicar correccion segun si es palabra unica o compuesta
            String corrected = query.contains(" ")
                ? correctCompound(checker, query)
                : correctSingle(checker, query);

            if (!corrected.equals(query)) {
                log.debug(
                    "[SpellCorrector] '{}' → '{}' (lang={})",
                    query,
                    corrected,
                    lang
                );
            }
            return corrected;
        } catch (SpellCheckException e) {
            log.warn(
                "[SpellCorrector] Error corrigiendo '{}': {}",
                query,
                e.getMessage()
            );
            return query;
        }
    }

    public String correct(String query) {
        return correct(query, FALLBACK_LANG);
    }

    public boolean isReady(String language) {
        if (!props.getSpell().isEnabled()) return false;
        String lang = extractLang(language);
        return checkers.containsKey(lang) && !failedLangs.containsKey(lang);
    }

    public boolean isReady() {
        return isReady(FALLBACK_LANG);
    }

    // --- Correccion interna ---

    private String correctSingle(SymSpellCheck checker, String word)
        throws SpellCheckException {
        // 1. Buscar sugerencias para palabra unica
        List<SuggestionItem> suggestions = checker.lookup(
            word,
            Verbosity.TOP,
            props.getSpell().getMaxEditDistance()
        );
        if (suggestions == null || suggestions.isEmpty()) return word;
        return suggestions.get(0).getTerm();
    }

    private String correctCompound(SymSpellCheck checker, String phrase)
        throws SpellCheckException {
        // 1. Buscar sugerencias para frase compuesta
        List<SuggestionItem> suggestions = checker.lookupCompound(
            phrase,
            props.getSpell().getMaxEditDistance()
        );
        if (suggestions == null || suggestions.isEmpty()) return phrase;
        return suggestions.get(0).getTerm();
    }

    // --- Inicialización lazy del checker ---

    private SymSpellCheck getOrCreate(String lang) {
        // 1. Retornar instancia existente o nulo si fallo previamente
        if (checkers.containsKey(lang)) return checkers.get(lang);
        if (failedLangs.containsKey(lang)) return null;

        Map<String, String> dicts = props.getSpell().getDictionaries();
        String basePath = dicts.get(lang);
        String domainPath = dicts.get(lang + "-domain");

        if (basePath == null) {
            if (!lang.equals(FALLBACK_LANG)) {
                log.debug(
                    "[SpellCorrector] Sin diccionario para '{}', redirigiendo a '{}'.",
                    lang,
                    FALLBACK_LANG
                );
                SymSpellCheck fallback = getOrCreate(FALLBACK_LANG);
                if (fallback != null) checkers.put(lang, fallback);
                return fallback;
            }
            log.warn(
                "[SpellCorrector] No hay diccionario base configurado para '{}'.",
                lang
            );
            failedLangs.put(lang, true);
            return null;
        }

        // 2. Construir e inicializar nueva instancia de correccion ortografica
        return checkers.computeIfAbsent(lang, l ->
            buildChecker(l, basePath, domainPath)
        );
    }

    private SymSpellCheck buildChecker(
        String lang,
        String basePath,
        String domainPath
    ) {
        try {
            // 1. Configurar parametros de correccion ortografica
            SpellCheckSettings settings = SpellCheckSettings.builder()
                .maxEditDistance(props.getSpell().getMaxEditDistance())
                .prefixLength(props.getSpell().getPrefixLength())
                .countThreshold(1)
                .verbosity(Verbosity.TOP)
                .topK(1)
                .build();

            // 2. Inicializar estructuras de datos para los diccionarios
            DataHolder dataHolder = new InMemoryDataHolder(
                settings,
                new Murmur3HashFunction()
            );

            StringDistance distance = new WeightedDamerauLevenshteinDistance(
                settings.getDeletionWeight(),
                settings.getInsertionWeight(),
                settings.getReplaceWeight(),
                settings.getTranspositionWeight(),
                null
            );

            SymSpellCheck checker = new SymSpellCheck(
                dataHolder,
                distance,
                settings
            );

            loadDictionary(dataHolder, basePath, lang, "base");

            if (domainPath != null) {
                loadDictionary(dataHolder, domainPath, lang, "domain");
            }

            log.info(
                "[SpellCorrector] Checker '{}' listo — base='{}' | domain='{}'.",
                lang,
                basePath,
                domainPath != null ? domainPath : "—"
            );
            return checker;
        } catch (Exception e) {
            log.error(
                "[SpellCorrector] Error construyendo checker '{}': {}",
                lang,
                e.getMessage(),
                e
            );
            failedLangs.put(lang, true);
            return null;
        }
    }

    // --- Carga de diccionario ---

    private void loadDictionary(
        DataHolder dataHolder,
        String classpath,
        String lang,
        String label
    ) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpath);
        if (!resource.exists()) {
            throw new IOException(
                "Diccionario no encontrado en classpath: '" + classpath + "'. "
            );
        }

        int loaded = 0;
        int skipped = 0;

        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    resource.getInputStream(),
                    StandardCharsets.UTF_8
                )
            )
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("[\\t ]+", 2);
                if (parts.length < 2) {
                    skipped++;
                    continue;
                }

                String word = parts[0].toLowerCase(Locale.ROOT);
                long freq;
                try {
                    freq = Long.parseLong(parts[1].strip());
                } catch (NumberFormatException ex) {
                    skipped++;
                    continue;
                }

                try {
                    dataHolder.addItem(
                        new DictionaryItem(word, (double) freq, -1.0)
                    );
                    loaded++;
                } catch (SpellCheckException e) {
                    skipped++;
                }
            }
        }

        log.info(
            "[SpellCorrector] [{}/{}] {} entradas cargadas ({} omitidas) desde '{}'.",
            lang,
            label,
            loaded,
            skipped,
            classpath
        );
    }

    // --- Utilidades ---

    private String extractLang(String language) {
        if (language == null || language.isBlank()) return FALLBACK_LANG;
        return language.split("[-_]")[0].toLowerCase(Locale.ROOT);
    }
}
