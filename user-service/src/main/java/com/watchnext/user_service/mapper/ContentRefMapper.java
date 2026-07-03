package com.watchnext.user_service.mapper;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.model.ContentRef;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContentRefMapper {
    // ------- DTO -> entidad -------
    ContentRef toModel(ContentRefRequest request);

    List<ContentRef> toModelList(List<ContentRefRequest> requests);

    // ------- entidad -> DTO -------
    ContentRefRequest toRequest(ContentRef contentRef);

    List<ContentRefRequest> toRequestList(List<ContentRef> contentRefs);
}
