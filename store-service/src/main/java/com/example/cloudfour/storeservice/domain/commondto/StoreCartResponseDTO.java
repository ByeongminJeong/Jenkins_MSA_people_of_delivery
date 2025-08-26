package com.example.cloudfour.storeservice.domain.commondto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class StoreCartResponseDTO {
    private UUID storeId;
    private UUID userId;
    private String name;
}
