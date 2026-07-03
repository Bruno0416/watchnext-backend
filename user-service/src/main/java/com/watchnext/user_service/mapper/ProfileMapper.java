package com.watchnext.user_service.mapper;

import com.watchnext.common.dto.internal.ContentBasicDetail;
import com.watchnext.user_service.dto.ProfileResponse;
import com.watchnext.user_service.dto.ProfileSummaryResponse;
import com.watchnext.user_service.dto.PublicProfileResponse;
import com.watchnext.user_service.entity.Profile;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileMapper {

    ProfileSummaryResponse toSummary(Profile profile);

    @Mapping(target = "privateProfile", constant = "true")
    PublicProfileResponse toPublicResponse(Profile profile);

    @Mapping(target = "favorites", source = "contentDetails")
    ProfileResponse toResponse(
        Profile profile,
        List<ContentBasicDetail> contentDetails
    );

    List<ProfileSummaryResponse> toSummaryList(List<Profile> profiles);
}
