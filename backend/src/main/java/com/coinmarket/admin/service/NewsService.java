package com.coinmarket.admin.service;

import com.coinmarket.admin.dto.NewsRequest;
import com.coinmarket.admin.dto.NewsResponse;
import com.coinmarket.admin.entity.News;
import com.coinmarket.admin.repository.NewsRepository;
import com.coinmarket.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    public List<NewsResponse> listAll() {
        return newsRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(NewsResponse::from).toList();
    }

    public NewsResponse getById(Long id) {
        return newsRepository.findById(id)
                .map(NewsResponse::from)
                .orElseThrow(() -> new BusinessException("新闻不存在"));
    }

    @Transactional
    public NewsResponse create(NewsRequest request) {
        News entity = new News();
        applyRequest(entity, request);
        if (Boolean.TRUE.equals(entity.getIsPublished())) {
            entity.setPublishedAt(LocalDateTime.now());
        }
        return NewsResponse.from(newsRepository.save(entity));
    }

    @Transactional
    public NewsResponse update(Long id, NewsRequest request) {
        News entity = newsRepository.findById(id)
                .orElseThrow(() -> new BusinessException("新闻不存在"));
        boolean wasPublished = Boolean.TRUE.equals(entity.getIsPublished());
        applyRequest(entity, request);
        boolean nowPublished = Boolean.TRUE.equals(entity.getIsPublished());
        if (nowPublished && !wasPublished) {
            entity.setPublishedAt(LocalDateTime.now());
        }
        return NewsResponse.from(newsRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!newsRepository.existsById(id)) {
            throw new BusinessException("新闻不存在");
        }
        newsRepository.deleteById(id);
    }

    @Transactional
    public NewsResponse publish(Long id) {
        News entity = newsRepository.findById(id)
                .orElseThrow(() -> new BusinessException("新闻不存在"));
        entity.setIsPublished(true);
        entity.setPublishedAt(LocalDateTime.now());
        return NewsResponse.from(newsRepository.save(entity));
    }

    @Transactional
    public NewsResponse unpublish(Long id) {
        News entity = newsRepository.findById(id)
                .orElseThrow(() -> new BusinessException("新闻不存在"));
        entity.setIsPublished(false);
        entity.setPublishedAt(null);
        return NewsResponse.from(newsRepository.save(entity));
    }

    /** Public: return all published news ordered by publish date */
    public List<NewsResponse> getPublishedNews() {
        return newsRepository.findByIsPublishedTrueOrderByPublishedAtDesc()
                .stream().map(NewsResponse::from).toList();
    }

    /** Public: return single published article */
    public NewsResponse getPublishedNewsById(Long id) {
        News entity = newsRepository.findById(id)
                .orElseThrow(() -> new BusinessException("新闻不存在"));
        if (!Boolean.TRUE.equals(entity.getIsPublished())) {
            throw new BusinessException("新闻不存在");
        }
        return NewsResponse.from(entity);
    }

    private void applyRequest(News entity, NewsRequest request) {
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getSummary() != null) entity.setSummary(request.getSummary());
        if (request.getContent() != null) entity.setContent(request.getContent());
        if (request.getImageUrl() != null) entity.setImageUrl(request.getImageUrl());
        if (request.getIsPublished() != null) entity.setIsPublished(request.getIsPublished());
    }
}
