package com.watchnext.news_service.repository;

import com.watchnext.news_service.entity.News;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News, UUID> {
    List<News> findAllByOrderByPublishedAtDesc();

    Optional<News> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
