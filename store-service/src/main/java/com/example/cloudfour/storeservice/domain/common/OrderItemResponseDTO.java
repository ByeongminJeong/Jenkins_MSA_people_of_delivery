package com.example.cloudfour.storeservice.domain.common;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class OrderItemResponseDTO {
    private UUID menuOptionId;
    private UUID menuId;
}
