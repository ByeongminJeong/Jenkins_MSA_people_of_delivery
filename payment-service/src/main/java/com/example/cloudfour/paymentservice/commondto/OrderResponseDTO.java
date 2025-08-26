package com.example.cloudfour.paymentservice.commondto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private UUID id;
    private UUID userId;
    private UUID storeId;
    private Integer totalPrice;
    private String status;
    private String orderType;
    private String receiptType;
    private String address;
}

