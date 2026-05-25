package com.coinmarket.admin.service;

import com.coinmarket.admin.dto.BarcodeCodeRequest;
import com.coinmarket.admin.dto.BarcodeCodeResponse;
import com.coinmarket.admin.entity.BarcodeCode;
import com.coinmarket.admin.repository.BarcodeCodeRepository;
import com.coinmarket.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BarcodeCodeService {

    private final BarcodeCodeRepository barcodeCodeRepository;

    public List<BarcodeCodeResponse> getByType(String codeType) {
        return barcodeCodeRepository.findByCodeTypeOrderBySortOrder(codeType)
                .stream().map(BarcodeCodeResponse::from).toList();
    }

    public List<BarcodeCodeResponse> getActiveByType(String codeType) {
        return barcodeCodeRepository.findByCodeTypeAndIsActiveTrueOrderBySortOrder(codeType)
                .stream().map(BarcodeCodeResponse::from).toList();
    }

    public List<BarcodeCodeResponse> getByParent(String parentType, String parentValue) {
        return barcodeCodeRepository.findByParentTypeAndParentValueOrderBySortOrder(parentType, parentValue)
                .stream().map(BarcodeCodeResponse::from).toList();
    }

    public List<BarcodeCodeResponse> search(String keyword) {
        String pattern = "%" + keyword + "%";
        return barcodeCodeRepository
                .findByCodeTypeAndLabelEnContainingOrCodeTypeAndLabelZhContaining("COUNTRY", pattern, "COUNTRY", pattern)
                .stream().map(BarcodeCodeResponse::from).toList();
    }

    @Transactional
    public BarcodeCodeResponse create(BarcodeCodeRequest request) {
        BarcodeCode entity = new BarcodeCode();
        applyRequest(entity, request);
        entity.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return BarcodeCodeResponse.from(barcodeCodeRepository.save(entity));
    }

    @Transactional
    public BarcodeCodeResponse update(Long id, BarcodeCodeRequest request) {
        BarcodeCode entity = barcodeCodeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("编码不存在"));
        applyRequest(entity, request);
        return BarcodeCodeResponse.from(barcodeCodeRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!barcodeCodeRepository.existsById(id)) {
            throw new BusinessException("编码不存在");
        }
        barcodeCodeRepository.deleteById(id);
    }

    private void applyRequest(BarcodeCode entity, BarcodeCodeRequest request) {
        if (request.getCodeType() != null) entity.setCodeType(request.getCodeType());
        if (request.getCodeValue() != null) entity.setCodeValue(request.getCodeValue());
        if (request.getLabelEn() != null) entity.setLabelEn(request.getLabelEn());
        if (request.getLabelZh() != null) entity.setLabelZh(request.getLabelZh());
        if (request.getParentType() != null) entity.setParentType(request.getParentType());
        if (request.getParentValue() != null) entity.setParentValue(request.getParentValue());
        if (request.getSortOrder() != null) entity.setSortOrder(request.getSortOrder());
        if (request.getIsActive() != null) entity.setIsActive(request.getIsActive());
    }
}
