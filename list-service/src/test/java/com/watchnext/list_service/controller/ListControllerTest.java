package com.watchnext.list_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.watchnext.common.dto.ContentRefRequest;
import com.watchnext.common.exceptions.GlobalExceptionHandler;
import com.watchnext.list_service.dto.CreateListRequest;
import com.watchnext.list_service.dto.ItemsRequest;
import com.watchnext.list_service.dto.ListDetailResponse;
import com.watchnext.list_service.dto.MyListsResponse;
import com.watchnext.list_service.entity.ListVisibility;
import com.watchnext.list_service.exceptions.ItemAlreadyExists;
import com.watchnext.list_service.exceptions.ListAlreadyExists;
import com.watchnext.list_service.exceptions.ListNotFound;
import com.watchnext.list_service.service.ListService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ListController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ListService service;

    // -------------- 1. CREATE LIST --------------

    @Test
    void testCreateList_success() throws Exception {
        // 1. preparar request
        var request = new CreateListRequest(
            "Mi lista",
            "Descripción",
            List.of()
        );
        doNothing().when(service).createList(any());

        // 2. ejecutar y verificar
        // 201
        mockMvc
            .perform(
                post("/api/v1/list/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void testCreateList_nombreEnBlanco_400() throws Exception {
        // 1. preparar request (nombre vacío)
        var request = new CreateListRequest("", "Descripción", List.of());

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/list/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateList_listaYaExiste_409() throws Exception {
        // 1. preparar request
        var request = new CreateListRequest(
            "Mi lista",
            "Descripción",
            List.of()
        );
        doThrow(new ListAlreadyExists("La lista ya existe"))
            .when(service)
            .createList(any());

        // 2. ejecutar y verificar
        // 409
        mockMvc
            .perform(
                post("/api/v1/list/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict());
    }

    // -------------- 2. ADD ITEMS --------------

    @Test
    void testAddItems_success() throws Exception {
        // 1. preparar request
        var listId = UUID.randomUUID();
        var items = List.of(
            new ContentRefRequest(1, com.watchnext.common.enums.MediaType.MOVIE)
        );
        var request = new ItemsRequest(items);
        doNothing().when(service).addItems(any(), any());

        // 2. ejecutar y verificar
        // 201
        mockMvc
            .perform(
                post("/api/v1/list/add-items/" + listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isCreated());
    }

    @Test
    void testAddItems_itemsVacios_400() throws Exception {
        // 1. preparar request (lista de items vacía)
        var listId = UUID.randomUUID();
        var request = new ItemsRequest(List.of());

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                post("/api/v1/list/add-items/" + listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testAddItems_listaNoEncontrada_404() throws Exception {
        // 1. preparar request
        var listId = UUID.randomUUID();
        var items = List.of(
            new ContentRefRequest(1, com.watchnext.common.enums.MediaType.MOVIE)
        );
        var request = new ItemsRequest(items);
        doThrow(new ListNotFound()).when(service).addItems(any(), any());

        // 2. ejecutar y verificar
        // 404
        mockMvc
            .perform(
                post("/api/v1/list/add-items/" + listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void testAddItems_itemYaExiste_409() throws Exception {
        // 1. preparar request
        var listId = UUID.randomUUID();
        var items = List.of(
            new ContentRefRequest(1, com.watchnext.common.enums.MediaType.MOVIE)
        );
        var request = new ItemsRequest(items);
        doThrow(new ItemAlreadyExists("El item ya existe"))
            .when(service)
            .addItems(any(), any());

        // 2. ejecutar y verificar
        // 409
        mockMvc
            .perform(
                post("/api/v1/list/add-items/" + listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isConflict());
    }

    // -------------- 3. REMOVE ITEMS --------------

    @Test
    void testRemoveItems_success() throws Exception {
        // 1. preparar request
        var listId = UUID.randomUUID();
        var items = List.of(
            new ContentRefRequest(1, com.watchnext.common.enums.MediaType.MOVIE)
        );
        var request = new ItemsRequest(items);
        doNothing().when(service).removeItems(any(), any());

        // 2. ejecutar y verificar
        // 204
        mockMvc
            .perform(
                delete("/api/v1/list/remove-items/" + listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNoContent());
    }

    @Test
    void testRemoveItems_itemsVacios_400() throws Exception {
        // 1. preparar request (lista de items vacía)
        var listId = UUID.randomUUID();
        var request = new ItemsRequest(List.of());

        // 2. ejecutar y verificar
        // 400
        mockMvc
            .perform(
                delete("/api/v1/list/remove-items/" + listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void testRemoveItems_listaNoEncontrada_404() throws Exception {
        // 1. preparar request
        var listId = UUID.randomUUID();
        var items = List.of(
            new ContentRefRequest(1, com.watchnext.common.enums.MediaType.MOVIE)
        );
        var request = new ItemsRequest(items);
        doThrow(new ListNotFound()).when(service).removeItems(any(), any());

        // 2. ejecutar y verificar
        // 404
        mockMvc
            .perform(
                delete("/api/v1/list/remove-items/" + listId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isNotFound());
    }

    // -------------- 4. DELETE LIST --------------

    @Test
    void testDeleteList_success() throws Exception {
        // 1. preparar
        var listId = UUID.randomUUID();
        doNothing().when(service).deleteList(any());

        // 2. ejecutar y verificar
        // 204
        mockMvc
            .perform(delete("/api/v1/list/delete/" + listId))
            .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteList_listaNoEncontrada_404() throws Exception {
        // 1. preparar
        var listId = UUID.randomUUID();
        doThrow(new ListNotFound()).when(service).deleteList(any());

        // 2. ejecutar y verificar
        // 404
        mockMvc
            .perform(delete("/api/v1/list/delete/" + listId))
            .andExpect(status().isNotFound());
    }

    // -------------- 5. MY LISTS --------------

    @Test
    void testGetMyLists_success() throws Exception {
        // 1. preparar respuesta
        var response = new MyListsResponse(List.of());
        when(service.getMyLists()).thenReturn(response);

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(get("/api/v1/list/my-lists"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lists").isArray());
    }

    // -------------- 6. LIST DETAILS --------------

    @Test
    void testGetListDetails_success() throws Exception {
        // 1. preparar respuesta
        var listId = UUID.randomUUID();
        var response = new ListDetailResponse(
            listId,
            "Mi lista",
            "Descripción",
            ListVisibility.PUBLIC,
            List.of(),
            Instant.now(),
            Instant.now()
        );
        when(service.getListDetails(any(), any())).thenReturn(response);

        // 2. ejecutar y verificar
        // 200
        mockMvc
            .perform(get("/api/v1/list/" + listId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Mi lista"));
    }

    @Test
    void testGetListDetails_listaNoEncontrada_404() throws Exception {
        // 1. preparar
        var listId = UUID.randomUUID();
        when(service.getListDetails(any(), any())).thenThrow(
            new ListNotFound()
        );

        // 2. ejecutar y verificar
        // 404
        mockMvc
            .perform(get("/api/v1/list/" + listId))
            .andExpect(status().isNotFound());
    }
}
