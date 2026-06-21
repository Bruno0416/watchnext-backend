package com.watchnext.list_service.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MyListsResponse {

    List<UserListResponse> lists;
}
