package com.watchnext.auth_service.repository;

import com.watchnext.auth_service.entity.UserIdentity;
import com.watchnext.auth_service.entity.Users;
import com.watchnext.auth_service.enums.AuthProvider;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserIdentityRepository
    extends JpaRepository<UserIdentity, UUID>
{
    Optional<UserIdentity> findByProviderAndProviderUserId(
        AuthProvider provider,
        String providerUserId
    );

    // logins sociales de la cuenta, para la baja de admin
    void deleteAllByUser(Users user);
}
