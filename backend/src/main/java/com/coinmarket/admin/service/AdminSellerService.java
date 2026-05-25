package com.coinmarket.admin.service;

import com.coinmarket.seller.entity.SellerApplication;
import com.coinmarket.seller.entity.SellerProfile;
import com.coinmarket.seller.repository.SellerApplicationRepository;
import com.coinmarket.seller.repository.SellerProfileRepository;
import com.coinmarket.seller.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSellerService {

    private final SellerService sellerService;
    private final SellerApplicationRepository applicationRepository;
    private final SellerProfileRepository profileRepository;

    public List<SellerApplication> listApplications(String status) {
        if (status != null) {
            return applicationRepository.findByStatus(status);
        }
        return applicationRepository.findAll();
    }

    public List<SellerProfile> listProfiles() {
        return profileRepository.findAll();
    }

    @Transactional
    public void approveApplication(Long applicationId, Long adminId) {
        sellerService.approveApplication(applicationId, adminId);
    }

    @Transactional
    public void rejectApplication(Long applicationId, Long adminId, String reason) {
        sellerService.rejectApplication(applicationId, adminId, reason);
    }
}
