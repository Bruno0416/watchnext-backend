package com.watchnext.list_service.controller;

import com.watchnext.list_service.dto.CreateListRequest;
import com.watchnext.list_service.dto.ItemsRequest;
import com.watchnext.list_service.dto.ListDetailResponse;
import com.watchnext.list_service.dto.MyListsResponse;
import com.watchnext.list_service.service.ListService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/list")
public class ListController {

    private final ListService service;

    // ----- ENDPOINTS -----
    // 1. crear lista
    @PostMapping("/create")
    public ResponseEntity<Void> createList(
        @Valid @RequestBody CreateListRequest request
    ) {
        service.createList(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 2. agregar lista de items
    @PostMapping("/add-items/{listId}")
    public ResponseEntity<Void> addItems(
        @PathVariable UUID listId,
        @Valid @RequestBody ItemsRequest request
    ) {
        service.addItems(listId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 3. eliminar lista de items
    @DeleteMapping("/remove-items/{listId}")
    public ResponseEntity<Void> removeItems(
        @PathVariable UUID listId,
        @Valid @RequestBody ItemsRequest request
    ) {
        service.removeItems(listId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 4. eliminar lista
    @DeleteMapping("/delete/{listId}")
    public ResponseEntity<Void> deleteList(@PathVariable UUID listId) {
        service.deleteList(listId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 5. ver mis listas
    @GetMapping("/my-lists")
    public ResponseEntity<MyListsResponse> getMyLists() {
        return ResponseEntity.ok(service.getMyLists());
    }

    // 6. detalle lista
    @GetMapping("/{listId}")
    public ResponseEntity<ListDetailResponse> getListDetails(
        @PathVariable UUID listId
    ) {
        return ResponseEntity.ok(service.getListDetails(listId));
    }
}
