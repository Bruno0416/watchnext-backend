package com.watchnext.content_service.dto.persons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchnext.content_service.dto.common.ExternalIds;
import java.util.List;

public record PersonDetails(
    Long id,
    String name,
    String biography,
    String birthday,
    String deathday,
    @JsonProperty("place_of_birth") String placeOfBirth,
    @JsonProperty("profile_path") String profilePath,
    @JsonProperty("known_for_department") String knownForDepartment,
    ExternalIds externalIds,
    List<PersonImage> images,
    @JsonProperty("combined_credits") List<PersonCredit> combinedCredits
) {}
