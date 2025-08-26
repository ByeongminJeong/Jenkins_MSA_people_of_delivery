package com.example.cloudfour.cartservice.domain.cart.dto;

import com.example.cloudfour.cartservice.domain.cart.controller.CartCommonResponseDTO;
import com.example.cloudfour.cartservice.domain.cartitem.dto.CartItemResponseDTO;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class CartResponseDTO {
    @Getter
    @Builder
    public static class CartCreateResponseDTO{
        @JsonUnwrapped
        CartCommonResponseDTO cartCommonResponseDTO;
        UUID cartItemId;
        LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class CartDetailResponseDTO {
        @JsonUnwrapped
        CartCommonResponseDTO cartCommonResponseDTO;
        List<CartItemResponseDTO.CartItemListResponseDTO> cartItems;
    }
}