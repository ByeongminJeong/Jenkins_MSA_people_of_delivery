package com.example.cloudfour.paymentservice.domain.payment.entity;

import com.example.cloudfour.paymentservice.domain.payment.enums.PaymentStatus;
import com.example.cloudfour.modulecommon.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "p_payment_history")
public class PaymentHistory extends BaseEntity {
    
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus currentStatus;

    @Column(length = 500)
    private String changeReason;

    @Column(length = 1000)
    private String rawResponse;

    @Column(length = 100)
    private String idempotencyKey;

    @Column
    private LocalDateTime processedAt;

    @Column
    private LocalDateTime canceledAt;

    public void markAsProcessed(String idempotencyKey) {
        this.processedAt = LocalDateTime.now();
        this.idempotencyKey = idempotencyKey;
    }

    public boolean isProcessed() {
        return this.processedAt != null;
    }

    // 테스트 및 내부 사용
    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public static class PaymentHistoryBuilder {
        private PaymentHistoryBuilder id(UUID id) {
            throw new UnsupportedOperationException("id 수동 생성 불가");
        }
    }
}
