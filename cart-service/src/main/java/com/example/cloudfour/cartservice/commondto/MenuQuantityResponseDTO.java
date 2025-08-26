package com.example.cloudfour.cartservice.commondto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuQuantityResponseDTO {
    private UUID stockId;
    private UUID menuId;
    private Long quantity;
    private Long version;
}
