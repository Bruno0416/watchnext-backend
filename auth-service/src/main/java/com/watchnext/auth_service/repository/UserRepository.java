package com.watchnext.auth_service.repository;

import com.watchnext.auth_service.entity.Users;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
