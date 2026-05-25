package com.coinmarket.admin.repository;

import com.coinmarket.admin.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findAllByOrderByCreatedAtDesc();
    List<News> findByIsPublishedTrueOrderByPublishedAtDesc();
}
