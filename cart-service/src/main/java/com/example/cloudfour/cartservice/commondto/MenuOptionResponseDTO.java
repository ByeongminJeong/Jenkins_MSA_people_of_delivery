package com.example.cloudfour.cartservice.commondto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class MenuOptionResponseDTO {
    private UUID menuOptionId;
    private UUID menuId;
    private String menuName;
    private String optionName;
    private Integer additionalPrice;
}
