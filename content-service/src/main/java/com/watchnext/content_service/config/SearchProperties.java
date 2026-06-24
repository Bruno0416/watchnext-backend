package com.watchnext.content_service.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "watchnext.search")
public class SearchProperties {

    // ---------- Cache ----------
    private Duration cacheTtlGeneric = Duration.ofHours(6);
    private Duration cacheTtlPrecise = Duration.ofHours(24);

    // ---------- Spell correction ----------
    private Spell spell = new Spell();

    // ---------- Getters - Setters ----------

    public Duration getCacheTtlGeneric() {
        return cacheTtlGeneric;
    }

    public void setCacheTtlGeneric(Duration cacheTtlGeneric) {
        this.cacheTtlGeneric = cacheTtlGeneric;
    }

    public Duration getCacheTtlPrecise() {
        return cacheTtlPrecise;
    }

    public void setCacheTtlPrecise(Duration cacheTtlPrecise) {
        this.cacheTtlPrecise = cacheTtlPrecise;
    }

    public Spell getSpell() {
        return spell;
    }

    public void setSpell(Spell spell) {
        this.spell = spell;
    }

    // ---------- Inner class ----------
    public static class Spell {

        private boolean enabled = false;

        private int maxEditDistance = 2;

        private int prefixLength = 7;

        private Map<String, String> dictionaries = new HashMap<>();

        // ---------- Getters - Setters ----------
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxEditDistance() {
            return maxEditDistance;
        }

        public void setMaxEditDistance(int maxEditDistance) {
            this.maxEditDistance = maxEditDistance;
        }

        public int getPrefixLength() {
            return prefixLength;
        }

        public void setPrefixLength(int prefixLength) {
            this.prefixLength = prefixLength;
        }

        public Map<String, String> getDictionaries() {
            return dictionaries;
        }

        public void setDictionaries(Map<String, String> dictionaries) {
            this.dictionaries = dictionaries;
        }
    }
}
