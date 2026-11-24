package com.watchnext.list_service.controller;

import com.watchnext.list_service.repository.UserListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// rutas internas, solo con token de servicio
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/list/internal")
public class InternalListController {

    private final UserListRepository listRepo;

    // ---------- baja de usuario (la llama auth-service desde el panel admin) ----------
    @DeleteMapping("/users/{userId}")
    @Transactional
    public ResponseEntity<Void> deleteUserLists(@PathVariable String userId) {
        // 1. borrar todas sus listas, los items se van en cascada
        listRepo.deleteAll(listRepo.findByUserId(userId));
        return ResponseEntity.noContent().build();
    }
}
