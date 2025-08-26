package com.example.cloudfour.cartservice.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent {
    
    private String eventId;
    private String eventType;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime eventTime;
    
    private UUID orderId;
    private UUID userId;
    private UUID storeId;
    private String paymentKey;
    private BigDecimal amount;
    private String paymentMethod;
    private String failureReason;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime failedAt;

    public static PaymentFailedEvent create(
            UUID orderId,
            UUID userId, 
            UUID storeId,
            String paymentKey,
            BigDecimal amount,
            String paymentMethod,
            String failureReason,
            LocalDateTime failedAt) {
        
        return PaymentFailedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PAYMENT_FAILED")
                .eventTime(LocalDateTime.now())
                .orderId(orderId)
                .userId(userId)
                .storeId(storeId)
                .paymentKey(paymentKey)
                .amount(amount)
                .paymentMethod(paymentMethod)
                .failureReason(failureReason)
                .failedAt(failedAt)
                .build();
    }
}
