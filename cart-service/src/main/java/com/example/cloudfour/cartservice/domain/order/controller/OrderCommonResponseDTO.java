package com.example.cloudfour.cartservice.domain.order.controller;

import com.example.cloudfour.cartservice.domain.order.enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
public class OrderCommonResponseDTO {
    UUID orderId;
    OrderStatus orderStatus;
    Integer totalPrice;
    LocalDateTime createdAt;
}
