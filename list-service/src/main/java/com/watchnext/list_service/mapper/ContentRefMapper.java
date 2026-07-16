package com.watchnext.list_service.mapper;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.model.ContentRef;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface ContentRefMapper {
    @Mapping(target = "tmdbId", source = "tmdbId")
    @Mapping(target = "mediaType", source = "mediaType")
    ContentRef toModel(ContentRefRequest request);
    List<ContentRef> toModelList(List<ContentRefRequest> requests);

    @Mapping(target = "tmdbId", source = "tmdbId")
    @Mapping(target = "mediaType", source = "mediaType")
    ContentRefRequest toRequest(ContentRef contentRef);
    List<ContentRefRequest> toRequestList(List<ContentRef> contentRefs);
}
