package com.watchnext.list_service.service;

import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.dto.internal.ContentBasicDetail;
import com.watchnext.common.enums.MediaType;
import com.watchnext.common.model.ContentRef;
import com.watchnext.common.model.User;
import com.watchnext.common.security.CurrentUser;
import com.watchnext.list_service.client.ContentServiceClient;
import com.watchnext.list_service.dto.CreateListRequest;
import com.watchnext.list_service.dto.ItemsRequest;
import com.watchnext.list_service.dto.ListDetailResponse;
import com.watchnext.list_service.dto.MyListsResponse;
import com.watchnext.list_service.entity.ListItem;
import com.watchnext.list_service.entity.UserList;
import com.watchnext.list_service.exceptions.ItemAlreadyExists;
import com.watchnext.list_service.exceptions.ListAlreadyExists;
import com.watchnext.list_service.exceptions.ListNotFound;
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

    // ------- Clientes -------
    private final ContentServiceClient contentClient;

    // ---------- gestion de listas ----------
    @Override
    @Transactional
    public void createList(CreateListRequest request) {
        // 1. obtener user context
        User user = CurrentUser.get();

        // 2. validar existencia de lista con mismo nombre
        listRepo
            .findByUserIdAndName(user.id(), request.name())
            .ifPresent(existingList -> {
                throw new ListAlreadyExists(
                    "Ya existe una lista con el nombre: " + request.name()
                );
            });
        // 3. crear lista
        UserList list = UserList.builder()
            .userId(user.id())
            .name(request.name())
            .description(request.description())
            .build();

        listRepo.save(list);

        // 4. agregar lista de peliculas si no es null
        if (!request.contentRefs().isEmpty()) {
            contentRefMapper
                .toModelList(request.contentRefs())
                .forEach(content ->
                    list.addItem(ListItem.builder().content(content).build())
                );
        }
    }

    // ---------- gestion de items ----------
    @Override
    @Transactional
    public void addItems(UUID listId, ItemsRequest request) {
        // 1. validar que la lista existe y le pertenece al usuario
        User user = CurrentUser.get();

        UserList list = listRepo
            .findByUserIdAndId(user.id(), listId)
            .orElseThrow(() -> new ListNotFound("Lista no encontrada"));

        // 2. agregar items a la lista
        record ItemKey(Integer tmdbId, MediaType mediaType) {}

        // 3. cargamos los items que ya existen en la lista actual en un set
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

        // 4. filtramos la request para quedarnos solo con los items nuevos
        List<ContentRefRequest> itemsToAdd = request
            .items()
            .stream()
            .filter(
                ref ->
                    !existingItems.contains(
                        new ItemKey(ref.tmdbId(), ref.mediaType())
                    )
            )
            .toList();

        // 5. si la lista original tenia items pero despues de filtrar quedo vacia
        if (itemsToAdd.isEmpty() && !request.items().isEmpty()) {
            throw new ItemAlreadyExists(
                "Todos los contenidos enviados ya estan en la lista"
            );
        }

        // 6. agregamos unicamente los items que pasaron el filtro
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
        ) throw new ListNotFound();

        // 2. mapear dto a lista de objetos
        List<ContentRef> contentsToRemove = contentRefMapper.toModelList(
            request.items()
        );

        // 3. eliminar items
        listItemRepo.deleteByListIdAndContentIn(listId, contentsToRemove);
    }

    // ---------- eliminacion ----------
    @Override
    @Transactional
    public void deleteList(UUID listId) {
        // 1. obtener usuario
        User user = CurrentUser.get();
        // 2. eliminar lista
        int deletedRows = listRepo.deleteByIdAndUserIdMatch(listId, user.id());
        if (deletedRows == 0) {
            throw new ListNotFound();
        }
    }

    // ---------- consultas ----------
    @Override
    public MyListsResponse getMyLists() {
        // 1. obtener usuario
        User user = CurrentUser.get();
        // 2. obtener listas del usuario
        List<UserList> lists = listRepo.findByUserId(user.id());

        return listMapper.toMyListsResponse(lists);
    }

    @Override
    public ListDetailResponse getListDetails(UUID listId, String language) {
        // 1. obtener usuario
        User user = CurrentUser.get();
        // 2. obtener lista
        UserList list = listRepo
            .findByUserIdAndId(user.id(), listId)
            .orElseThrow(ListNotFound::new);

        // 3. obtener detalles de los items
        List<ContentRefRequest> requests = list
            .getItems()
            .stream()
            .map(item -> item.getContent())
            .map(contentRefMapper::toRequest)
            .toList();

        List<ContentBasicDetail> contents = contentClient.fetchBulkContent(
            requests,
            language
        );

        return listMapper.toDetailResponse(list, contents);
    }
}
