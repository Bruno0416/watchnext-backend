package com.watchnext.list_service.mapper;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.model.ContentRef;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContentRefMapper {
    ContentRef toModel(ContentRefRequest request);
    List<ContentRef> toModelList(List<ContentRefRequest> requests);

    ContentRefRequest toRequest(ContentRef contentRef);
    List<ContentRefRequest> toRequestList(List<ContentRef> contentRefs);
}
