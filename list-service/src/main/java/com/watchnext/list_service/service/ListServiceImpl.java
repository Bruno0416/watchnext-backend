package com.watchnext.list_service.service;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.model.ContentRef;
import com.watchnext.common.model.MediaType;
import com.watchnext.common.model.User;
import com.watchnext.common.security.CurrentUser;
import com.watchnext.list_service.dto.CreateListRequest;
import com.watchnext.list_service.dto.ItemsRequest;
import com.watchnext.list_service.dto.ListDetailResponse;
import com.watchnext.list_service.dto.MyListsResponse;
import com.watchnext.list_service.entity.ListItem;
import com.watchnext.list_service.entity.UserList;
import com.watchnext.list_service.exceptions.ItemAlreadyExistsException;
import com.watchnext.list_service.mapper.ContentRefMapper;
import com.watchnext.list_service.mapper.UserListMapper;
import com.watchnext.list_service.repository.ListItemRepository;
import com.watchnext.list_service.repository.UserListRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListServiceImpl implements ListService {

    // ------- Repositorios -------
    private final ListItemRepository listItemRepo;
    private final UserListRepository listRepo;

    // ------- Mappers -------
    private final ContentRefMapper contentRefMapper;
    private final UserListMapper listMapper;

    @Override
    @Transactional
    public void createList(CreateListRequest request) {
        // 1. obtener user context
        User user = CurrentUser.get();

        // 2. validar existencia de lista con mismo nombre
        listRepo
            .findByUserIdAndName(user.id(), request.getName())
            .ifPresent(existingList -> {
                throw new IllegalArgumentException(
                    // TODO: crear excepcion
                    "Ya existe una lista con el nombre: " + request.getName()
                );
            });
        // 3. crear lista
        UserList list = UserList.builder()
            .userId(user.id())
            .name(request.getName())
            .description(request.getDescription())
            .build();

        listRepo.save(list);

        // 4. agregar lista de peliculas si no es null
        if (!request.getContentRefs().isEmpty()) {
            contentRefMapper
                .toModelList(request.getContentRefs())
                .forEach(content ->
                    list.addItem(ListItem.builder().content(content).build())
                );
        }
    }

    @Override
    @Transactional
    public void addItems(UUID listId, ItemsRequest request) {
        // 1. validar que la lista existe y le pertenece al usuario
        User user = CurrentUser.get();

        UserList list = listRepo
            .findByUserIdAndId(user.id(), listId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    // TODO: crear excepcion
                    "Lista no encontrada"
                )
            );

        // 2. agregar items a la lista
        record ItemKey(Long tmdbId, MediaType mediaType) {}

        // 3. Cargamos los items que YA existen en la lista actual en un Set.
        Set<ItemKey> existingItems = list
            .getItems()
            .stream()
            .map(item ->
                new ItemKey(
                    item.getContent().getTmdbId(),
                    item.getContent().getMediaType()
                )
            )
            .collect(Collectors.toSet());

        // 4. Filtramos la request para quedarnos SOLO con los items nuevos.
        List<ContentRefRequest> itemsToAdd = request
            .getItems()
            .stream()
            .filter(
                ref ->
                    !existingItems.contains(
                        new ItemKey(ref.getTmdbId(), ref.getMediaType())
                    )
            )
            .toList();

        // 5. Si la lista original tenia items, pero despues de filtrar quedo vacia
        if (itemsToAdd.isEmpty() && !request.getItems().isEmpty()) {
            throw new ItemAlreadyExistsException(
                "Todos los contenidos enviados ya están en la lista"
            );
        }

        // 6. Agregamos únicamente los items que pasaron el filtro
        itemsToAdd.forEach(ref ->
            list.addItem(
                ListItem.builder()
                    .content(contentRefMapper.toModel(ref))
                    .build()
            )
        );

        // 7. guardar lista
        listRepo.save(list);
    }

    @Override
    @Transactional
    public void removeItems(UUID listId, ItemsRequest request) {
        // 1. validar que la lista existe y le pertenece al usuario
        User user = CurrentUser.get();

        if (
            !listRepo.existsByUserIdAndId(user.id(), listId)
        ) throw new IllegalArgumentException(
            "Lista no encontrada o no pertenece al usuario"
        );

        // 2. mapear dto a lista de objetos
        List<ContentRef> contentsToRemove = contentRefMapper.toModelList(
            request.getItems()
        );

        // 3. eliminar items
        listItemRepo.deleteByListIdAndContentIn(listId, contentsToRemove);
    }

    @Override
    @Transactional
    public void deleteList(UUID listId) {
        // 1. obtener usuario
        User user = CurrentUser.get();
        // 2. eliminar lista
        int deletedRows = listRepo.deleteByIdAndUserIdMatch(listId, user.id());
        if (deletedRows == 0) {
            throw new IllegalArgumentException(
                // TODO: crear excepcion
                "Lista no encontrada o no pertenece al usuario"
            );
        }
    }

    @Override
    public MyListsResponse getMyLists() {
        // 1. obtener usuario
        User user = CurrentUser.get();
        // 2. obtener listas del usuario
        List<UserList> lists = listRepo.findByUserId(user.id());

        return listMapper.toMyListsResponse(lists);
    }

    @Override
    public ListDetailResponse getListDetails(UUID listId) {
        // 1. obtener usuario
        User user = CurrentUser.get();
        // 2. obtener lista
        UserList list = listRepo
            .findByUserIdAndId(user.id(), listId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Lista no encontrada o no pertenece al usuario"
                )
            );

        return listMapper.toDetailResponse(list);
    }
}
