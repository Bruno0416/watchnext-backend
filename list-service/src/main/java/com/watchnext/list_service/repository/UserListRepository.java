package com.watchnext.list_service.repository;

import com.watchnext.list_service.entity.UserList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserListRepository extends JpaRepository<UserList, UUID> {
    // metodo buscar por userId + nombre
    Optional<UserList> findByUserIdAndName(String userId, String name);

    // metodo buscar por userId + listId
    Optional<UserList> findByUserIdAndId(String userId, UUID listId);

    // metodo par validar lista pertenece al usuario
    boolean existsByUserIdAndId(String userId, UUID listId);

    // metodo para eliminar lista por usuario + id devuelve la cantidad de filas afectadas
    @Modifying
    @Query("DELETE FROM UserList u WHERE u.id = :id AND u.userId = :userId")
    int deleteByIdAndUserIdMatch(
        @Param("id") UUID id,
        @Param("userId") String userId
    );

    // metodo para obtener listas de un usuario
    List<UserList> findByUserId(String userId);
}
