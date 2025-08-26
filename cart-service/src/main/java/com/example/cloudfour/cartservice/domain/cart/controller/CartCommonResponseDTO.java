package com.example.cloudfour.cartservice.domain.cart.controller;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CartCommonResponseDTO {
    UUID cartId;
    UUID userId;
    UUID storeId;
}
