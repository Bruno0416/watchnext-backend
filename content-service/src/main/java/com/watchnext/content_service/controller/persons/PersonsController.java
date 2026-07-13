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
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/content/person")
public class PersonsController {

    private final ContentService contentService;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<PersonDetails>> getPersonDetails(
        @PathVariable Long id,
        @RequestParam(defaultValue = "en-US") String language
    ) {
        return contentService
            .getPersonDetails(id, language)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
