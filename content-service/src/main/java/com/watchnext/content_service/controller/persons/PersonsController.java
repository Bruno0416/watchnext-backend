package com.watchnext.content_service.controller.persons;

import com.watchnext.content_service.dto.persons.PersonDetails;
import com.watchnext.content_service.service.content.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/content/person")
@Validated
public class PersonsController {

    private final ContentService contentService;

    // ---------- informacion de personas ----------
    @GetMapping("/{id}")
    public Mono<ResponseEntity<PersonDetails>> getPersonDetails(
        @PathVariable @Positive(message = "El id debe ser un número positivo") Long id,
        @RequestParam(defaultValue = "en-US") @Size(max = 20, message = "El idioma no puede exceder los 20 caracteres") String language
    ) {
        return contentService
            .getPersonDetails(id, language)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
