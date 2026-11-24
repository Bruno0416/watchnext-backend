package com.watchnext.auth_service.repository;

import com.watchnext.auth_service.entity.Users;
import com.watchnext.auth_service.enums.Role;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // cuantas cuentas tienen un rol, para no borrar el ultimo admin
    long countByRole(Role role);
}
