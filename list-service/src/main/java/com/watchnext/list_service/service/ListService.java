package com.watchnext.list_service.service;

import com.watchnext.list_service.dto.CreateListRequest;
import com.watchnext.list_service.dto.ItemsRequest;
import com.watchnext.list_service.dto.ListDetailResponse;
import com.watchnext.list_service.dto.MyListsResponse;
import java.util.UUID;

public interface ListService {
    // 1. crear lista (nombre, descripcion, lista{opt})
    void createList(CreateListRequest request);
    // 2. agregar lista de items(UUID listId, ItemsRequest request)
    void addItems(UUID listId, ItemsRequest request);
    // 3. eliminar lista de items(UUID listId, ItemsRequest request)
    void removeItems(UUID listId, ItemsRequest request);
    // 4. eliminar lista
    void deleteList(UUID listId);
    // 5. ver mis listas
    MyListsResponse getMyLists();
    // 6. detalle lista
    ListDetailResponse getListDetails(UUID listId);
}
