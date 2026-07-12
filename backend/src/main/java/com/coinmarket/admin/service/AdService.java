package com.coinmarket.admin.service;

import com.coinmarket.admin.dto.AdRequest;
import com.coinmarket.admin.dto.AdResponse;
import com.coinmarket.admin.entity.Ad;
import com.coinmarket.admin.repository.AdRepository;
import com.coinmarket.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdService {

    private final AdRepository adRepository;

    public List<AdResponse> listAll() {
        return adRepository.findAllByOrderBySortOrderAsc()
                .stream().map(AdResponse::from).toList();
    }

    public AdResponse getById(Long id) {
        return adRepository.findById(id)
                .map(AdResponse::from)
                .orElseThrow(() -> new BusinessException("广告不存在"));
    }

    @Transactional
    public AdResponse create(AdRequest request) {
        validateDates(request);
        Ad entity = new Ad();
        applyRequest(entity, request);
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return AdResponse.from(adRepository.save(entity));
    }

    @Transactional
    public AdResponse update(Long id, AdRequest request) {
        Ad entity = adRepository.findById(id)
                .orElseThrow(() -> new BusinessException("广告不存在"));
        validateDates(request);
        applyRequest(entity, request);
        return AdResponse.from(adRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!adRepository.existsById(id)) {
            throw new BusinessException("广告不存在");
        }
        adRepository.deleteById(id);
    }

    @Transactional
    public void toggleStatus(Long id) {
        Ad entity = adRepository.findById(id)
                .orElseThrow(() -> new BusinessException("广告不存在"));
        entity.setIsActive(Boolean.FALSE.equals(entity.getIsActive()));
        adRepository.save(entity);
    }

    /** Public: return active ads within their scheduled date range (or no date restriction) */
    public List<AdResponse> getActiveAds() {
        LocalDate today = LocalDate.now();
        return adRepository.findByIsActiveTrueOrderBySortOrderAsc().stream()
                .filter(ad -> {
                    boolean startOk = ad.getStartDate() == null || !ad.getStartDate().isAfter(today);
                    boolean endOk = ad.getEndDate() == null || !ad.getEndDate().isBefore(today);
                    return startOk && endOk;
                })
                .map(AdResponse::from)
                .toList();
    }

    private void validateDates(AdRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
    }

    private void applyRequest(Ad entity, AdRequest request) {
        if (request.getTitle() != null) entity.setTitle(request.getTitle());
        if (request.getImageUrl() != null) entity.setImageUrl(request.getImageUrl());
        if (request.getLinkUrl() != null) entity.setLinkUrl(request.getLinkUrl());
        if (request.getSortOrder() != null) entity.setSortOrder(request.getSortOrder());
        if (request.getIsActive() != null) entity.setIsActive(request.getIsActive());
        if (request.getStartDate() != null) entity.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) entity.setEndDate(request.getEndDate());
    }
}
