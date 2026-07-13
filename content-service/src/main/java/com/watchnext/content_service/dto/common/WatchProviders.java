package com.watchnext.content_service.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record WatchProviders(List<Provider> flatrate, List<Provider> rent, List<Provider> buy) {

    public static final String ATTRIBUTION = "Watch data powered by JustWatch";

    // atribucion obligatoria a JustWatch para los datos de watch providers
    @JsonProperty("attribution")
    public String attribution() {
        return ATTRIBUTION;
    }

    public record Provider(
        @JsonProperty("provider_id") Integer id,
        @JsonProperty("provider_name") String name,
        @JsonProperty("logo_path") String logoPath
    ) {}
}
