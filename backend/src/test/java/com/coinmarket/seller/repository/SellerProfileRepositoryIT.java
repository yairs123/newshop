package com.coinmarket.seller.repository;

import com.coinmarket.seller.entity.SellerProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("SellerProfile Repository Integration Test")
class SellerProfileRepositoryIT {

    @Autowired
    private SellerProfileRepository sellerProfileRepository;

    private SellerProfile buildProfile(Long userId) {
        return SellerProfile.builder()
                .userId(userId)
                .shopName("Coin Shop " + userId)
                .shopDescription("Collectible coins store")
                .contactPhone("+1234567890")
                .contactEmail("shop" + userId + "@test.com")
                .status("ACTIVE")
                .locked(false)
                .build();
    }

    @Test
    @DisplayName("保存卖家档案并可通过用户ID查询")
    void saveAndFindByUserId() {
        SellerProfile profile = buildProfile(10L);
        SellerProfile saved = sellerProfileRepository.save(profile);
        assertThat(saved.getId()).isNotNull();

        Optional<SellerProfile> found = sellerProfileRepository.findByUserId(10L);
        assertThat(found).isPresent();
        assertThat(found.get().getShopName()).isEqualTo("Coin Shop 10");
        assertThat(found.get().getStatus()).isEqualTo("ACTIVE");
        assertThat(found.get().isLocked()).isFalse();
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("检查用户是否已有卖家档案")
    void existsByUserId() {
        sellerProfileRepository.save(buildProfile(20L));

        assertThat(sellerProfileRepository.existsByUserId(20L)).isTrue();
        assertThat(sellerProfileRepository.existsByUserId(999L)).isFalse();
    }

    @Test
    @DisplayName("不存在的用户返回空")
    void unknownUser_returnsEmpty() {
        Optional<SellerProfile> found = sellerProfileRepository.findByUserId(999L);
        assertThat(found).isEmpty();
    }
}
