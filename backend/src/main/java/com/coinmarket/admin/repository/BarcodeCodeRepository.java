package com.coinmarket.admin.repository;

import com.coinmarket.admin.entity.BarcodeCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarcodeCodeRepository extends JpaRepository<BarcodeCode, Long> {
    List<BarcodeCode> findByCodeTypeOrderBySortOrder(String codeType);
    List<BarcodeCode> findByCodeTypeAndIsActiveTrueOrderBySortOrder(String codeType);
    List<BarcodeCode> findByParentTypeAndParentValueOrderBySortOrder(String parentType, String parentValue);
    List<BarcodeCode> findByCodeTypeAndLabelEnContainingOrCodeTypeAndLabelZhContaining(
            String type1, String keyword1, String type2, String keyword2);
}
