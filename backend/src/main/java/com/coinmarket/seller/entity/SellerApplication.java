package com.coinmarket.seller.entity;

import com.coinmarket.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "seller_applications", schema = "coin_seller")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerApplication extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "id_document_url", length = 500)
    private String idDocumentUrl;

    @Column(name = "id_document_type", length = 50)
    private String idDocumentType;

    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified;

    @Column(name = "shop_name", length = 100)
    private String shopName;

    @Column(name = "shop_description", columnDefinition = "TEXT")
    private String shopDescription;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}
