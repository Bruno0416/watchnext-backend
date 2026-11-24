package com.watchnext.auth_service.controller;

import com.watchnext.auth_service.dto.admin.AdminCreateUserRequest;
import com.watchnext.auth_service.dto.admin.AdminIdentityRequest;
import com.watchnext.auth_service.dto.admin.AdminUserResponse;
import com.watchnext.auth_service.service.admin.AdminUserService;
import com.watchnext.common.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// gestion de cuentas para el panel admin, SecurityConfig exige rol ADMIN
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/auth/admin/users")
public class AdminUserController {

    private final AdminUserService service;

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> search(@RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(service.search(q));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponse> getById(@PathVariable UUID userId) {
        return ResponseEntity.ok(service.getById(userId));
    }

    @PostMapping
    public ResponseEntity<AdminUserResponse> create(@Valid @RequestBody AdminCreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{userId}/identity")
    public ResponseEntity<AdminUserResponse> updateIdentity(
        @PathVariable UUID userId,
        @Valid @RequestBody AdminIdentityRequest request
    ) {
        return ResponseEntity.ok(service.updateIdentity(userId, request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        service.delete(userId, CurrentUser.id());
        return ResponseEntity.noContent().build();
    }
}
