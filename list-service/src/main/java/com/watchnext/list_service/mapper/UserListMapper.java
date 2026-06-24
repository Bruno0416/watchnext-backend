package com.watchnext.list_service.mapper;

import com.watchnext.common.dto.ContentRefResponse;
import com.watchnext.common.dto.internal.ContentBasicDetail;
import com.watchnext.list_service.dto.ListDetailResponse;
import com.watchnext.list_service.dto.MyListsResponse;
import com.watchnext.list_service.dto.UserListResponse;
import com.watchnext.list_service.entity.ListItem;
import com.watchnext.list_service.entity.UserList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserListMapper {
    // 1. Mapeos para vistas resumidas (UserListResponse)
    @Mapping(
        target = "itemCount",
        expression = "java(entity.getItems() != null ? entity.getItems().size() : 0)"
    )
    UserListResponse toResponse(UserList entity);

    List<UserListResponse> toResponseList(List<UserList> entities);

    default MyListsResponse toMyListsResponse(List<UserList> entities) {
        if (entities == null) {
            return null;
        }
        return new MyListsResponse(toResponseList(entities));
    }

    // 2. Mapeos para vistas detalladas (ListDetailResponse)
    @Mapping(target = "items", ignore = true)
    ListDetailResponse toDetailResponse(UserList entity);

    @Mapping(target = "tmdbId", source = "content.tmdbId")
    @Mapping(target = "mediaType", source = "content.mediaType")
    ContentRefResponse listItemToContentRefResponse(ListItem item);

    @Mapping(target = "items", source = "contents")
    ListDetailResponse toDetailResponse(
        UserList entity,
        List<ContentBasicDetail> contents
    );
}
