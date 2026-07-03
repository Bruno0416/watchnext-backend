package com.watchnext.common.enums;

public enum Language {
    ES,
    EN;

    public static Language fromString(String value) {
        if (value == null) return ES;
        for (Language lang : Language.values()) {
            if (lang.name().equalsIgnoreCase(value)) {
                return lang;
            }
        }
        return ES;
    }
}
