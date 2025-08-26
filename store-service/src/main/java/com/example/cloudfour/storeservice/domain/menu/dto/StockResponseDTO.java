package com.example.cloudfour.storeservice.domain.menu.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Builder
@Getter
public class StockResponseDTO {
    private UUID stockId;
    private UUID menuId;
    private Long quantity;
    private Long version;
}
