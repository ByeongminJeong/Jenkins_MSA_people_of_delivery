package com.example.cloudfour.storeservice.domain.commondto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class MenuCartResponseDTO {
    private UUID menuId;
    private Integer price;
}