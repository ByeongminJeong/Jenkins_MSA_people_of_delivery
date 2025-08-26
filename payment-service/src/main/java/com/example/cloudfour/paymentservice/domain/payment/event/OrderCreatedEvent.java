package com.example.cloudfour.paymentservice.domain.payment.event;

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
public class OrderCreatedEvent {
    
    private String eventId;
    private String eventType;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime eventTime;
    
    private UUID orderId;
    private UUID userId;
    private UUID storeId;
    private BigDecimal totalAmount;
    private String orderStatus;
    private String deliveryAddress;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdAt;
}
