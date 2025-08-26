package com.example.cloudfour.cartservice.domain.cartitem.dto;

import com.example.cloudfour.cartservice.domain.cartitem.controller.CartItemCommonResponseDTO;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

public class CartItemResponseDTO {
    @Getter
    @Builder
    public static class CartItemAddResponseDTO {
        @JsonUnwrapped
        CartItemCommonResponseDTO cartItemCommonResponseDTO;
        UUID menuId;
    }

    @Getter
    @Builder
    public static class CartItemListResponseDTO {
        @JsonUnwrapped
        CartItemCommonResponseDTO cartItemCommonResponseDTO;
        UUID menuId;
    }

    @Getter
    @Builder
    public static class CartItemUpdateResponseDTO {
        @JsonUnwrapped
        CartItemCommonResponseDTO cartItemCommonResponseDTO;
    }
}
