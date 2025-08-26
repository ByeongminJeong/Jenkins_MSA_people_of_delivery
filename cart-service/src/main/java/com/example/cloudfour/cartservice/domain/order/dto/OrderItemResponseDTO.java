package com.example.cloudfour.cartservice.domain.order.dto;


import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public class OrderItemResponseDTO {
    @Getter
    @Builder
    public static class OrderItemListResponseDTO{
        Integer quantity;
        Integer price;
        UUID menuId;
        List<OrderItemOptionDTO> options;
    }

    @Getter
    @Builder
    public static class OrderItemOptionDTO {
        private UUID menuOptionId;
        private Integer additionalPrice;
        private String optionName;
    }
}