package com.watchnext.user_service.mapper;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.user_service.dto.FavoriteItemRequest;
import com.watchnext.common.model.ContentRef;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ContentRefMapper {
    // ------- DTO -> entidad -------
    ContentRef toModel(ContentRefRequest request);

    List<ContentRef> toModelList(List<ContentRefRequest> requests);

    // ------- DTO especifico -> entidad -------
    ContentRef toModelFromFavorite(FavoriteItemRequest request);

    List<ContentRef> toModelListFromFavorites(List<FavoriteItemRequest> requests);

    // ------- entidad -> DTO -------
    ContentRefRequest toRequest(ContentRef contentRef);

    List<ContentRefRequest> toRequestList(List<ContentRef> contentRefs);
}
