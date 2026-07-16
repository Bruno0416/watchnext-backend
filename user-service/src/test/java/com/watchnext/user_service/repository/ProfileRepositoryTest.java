package com.watchnext.user_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.watchnext.user_service.entity.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;

// Regresion del bug: findCountryByUserId era una query derivada sin @Query,
// Spring Data la resolvia como findByUserId y devolvia Profile en vez de String,
// causando ClassCastException. Ver ProfileRepository.java.
@DataJpaTest
public class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepo;

    // Codigo: retorna el pais como String, sin ClassCastException
    @Test
    public void testFindCountryByUserId() {
        // 1. preparar perfil persistido con pais y region
        Profile profile = Profile.builder()
            .userId("user-cl-1")
            .username("clientecl")
            .country("CL")
            .region("LATAM")
            .build();
        profileRepo.saveAndFlush(profile);

        // 2. ejecutar test
        var result = profileRepo.findCountryByUserId("user-cl-1");

        // 3. verificar
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("CL");
    }

    // Codigo: usuario inexistente -> Optional vacio
    @Test
    public void testFindCountryByUserIdNotFound() {
        var result = profileRepo.findCountryByUserId("no-such-user");

        assertThat(result).isEmpty();
    }
}
