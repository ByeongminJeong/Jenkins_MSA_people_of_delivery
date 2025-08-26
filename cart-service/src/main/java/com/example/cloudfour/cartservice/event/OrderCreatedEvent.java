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
    
    public static OrderCreatedEvent create(
            UUID orderId,
            UUID userId, 
            UUID storeId,
            BigDecimal totalAmount,
            String orderStatus,
            String deliveryAddress) {
        
        return OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_CREATED")
                .eventTime(LocalDateTime.now())
                .orderId(orderId)
                .userId(userId)
                .storeId(storeId)
                .totalAmount(totalAmount)
                .orderStatus(orderStatus)
                .deliveryAddress(deliveryAddress)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
