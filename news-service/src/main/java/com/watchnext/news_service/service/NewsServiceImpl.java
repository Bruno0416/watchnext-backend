package com.watchnext.news_service.service;

import com.watchnext.news_service.dto.NewsRequest;
import com.watchnext.news_service.dto.NewsResponse;
import com.watchnext.news_service.entity.News;
import com.watchnext.news_service.exceptions.NewsNotFound;
import com.watchnext.news_service.exceptions.SlugAlreadyExists;
import com.watchnext.news_service.repository.NewsRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepo;

    // ---------- consultas ----------
    @Override
    @Transactional(readOnly = true)
    public List<NewsResponse> getAll() {
        // 1. mas recientes primero
        return newsRepo.findAllByOrderByPublishedAtDesc().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NewsResponse getById(UUID id) {
        return toResponse(find(id));
    }

    @Override
    @Transactional(readOnly = true)
    public NewsResponse getBySlug(String slug) {
        return newsRepo.findBySlug(slug).map(this::toResponse).orElseThrow(() -> new NewsNotFound(slug));
    }

    // ---------- administracion ----------
    @Override
    @Transactional
    public NewsResponse create(NewsRequest request) {
        // 1. el slug es la url de la noticia, no se puede repetir
        if (newsRepo.existsBySlug(request.slug())) {
            throw new SlugAlreadyExists(request.slug());
        }

        // 2. crear noticia
        News news = News.builder()
            .slug(request.slug())
            .title(request.title().trim())
            .summary(request.summary().trim())
            .content(request.content().trim())
            .imageUrl(blankToNull(request.imageUrl()))
            .publishedAt(request.publishedAt() != null ? request.publishedAt() : Instant.now())
            .build();

        return toResponse(newsRepo.save(news));
    }

    @Override
    @Transactional
    public NewsResponse update(UUID id, NewsRequest request) {
        // 1. buscar noticia
        News news = find(id);

        // 2. validar slug contra las demas noticias
        if (newsRepo.existsBySlugAndIdNot(request.slug(), id)) {
            throw new SlugAlreadyExists(request.slug());
        }

        // 3. actualizar campos
        news.setSlug(request.slug());
        news.setTitle(request.title().trim());
        news.setSummary(request.summary().trim());
        news.setContent(request.content().trim());
        news.setImageUrl(blankToNull(request.imageUrl()));
        if (request.publishedAt() != null) news.setPublishedAt(request.publishedAt());

        return toResponse(newsRepo.save(news));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        newsRepo.delete(find(id));
    }

    // ---------- helpers ----------
    private News find(UUID id) {
        return newsRepo.findById(id).orElseThrow(() -> new NewsNotFound(id.toString()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private NewsResponse toResponse(News news) {
        return new NewsResponse(
            news.getId(),
            news.getSlug(),
            news.getTitle(),
            news.getSummary(),
            news.getContent(),
            news.getImageUrl(),
            news.getPublishedAt()
        );
    }
}
