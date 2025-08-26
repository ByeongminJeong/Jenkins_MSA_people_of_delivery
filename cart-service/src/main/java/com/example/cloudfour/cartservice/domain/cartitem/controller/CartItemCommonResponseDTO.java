package com.example.cloudfour.cartservice.domain.cartitem.controller;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
public class CartItemCommonResponseDTO {
    UUID cartItemId;
    UUID cartId;
    List<MenuOptionDto> menuOptions;
    Integer quantity;
    Integer price;
    
    @Getter
    @Builder
    public static class MenuOptionDto {
        private UUID id;
        private Integer additionalPrice;
        private String optionName;
    }
}
