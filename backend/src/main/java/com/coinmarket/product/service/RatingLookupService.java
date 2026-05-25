package com.coinmarket.product.service;

import com.coinmarket.product.dto.RatingInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RatingLookupService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.rating-api.ngc.base-url:https://api.ngccoin.com}")
    private String ngcBaseUrl;

    @Value("${app.rating-api.pcgs.base-url:https://api.pcgs.com}")
    private String pcgsBaseUrl;

    @Value("${app.rating-api.pmg.base-url:https://api.pmgnotes.com}")
    private String pmgBaseUrl;

    public RatingInfoResponse lookup(String company, String certNumber) {
        try {
            return switch (company.toUpperCase()) {
                case "NGC" -> lookupNgc(certNumber);
                case "PCGS" -> lookupPcgs(certNumber);
                case "PMG" -> lookupPmg(certNumber);
                default -> null;
            };
        } catch (Exception e) {
            log.warn("Rating lookup failed for {}: {}", company, certNumber, e);
            return null;
        }
    }

    private RatingInfoResponse lookupNgc(String certNumber) {
        try {
            String url = ngcBaseUrl + "/api/certificates/" + certNumber;
            var response = restTemplate.getForEntity(url, NgcResponse.class);
            if (response.getBody() != null) {
                return RatingInfoResponse.builder()
                        .grade(response.getBody().getGrade())
                        .certNumber(certNumber)
                        .verified(true)
                        .build();
            }
        } catch (Exception e) {
            log.warn("NGC lookup failed for {}", certNumber, e);
        }
        return null;
    }

    private RatingInfoResponse lookupPcgs(String certNumber) {
        log.debug("PCGS lookup placeholder for {}", certNumber);
        return null;
    }

    private RatingInfoResponse lookupPmg(String certNumber) {
        log.debug("PMG lookup placeholder for {}", certNumber);
        return null;
    }

    @lombok.Data
    static class NgcResponse {
        private String grade;
        private String certNumber;
        private String designation;
        private String label;
    }
}
