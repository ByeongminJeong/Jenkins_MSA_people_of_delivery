package com.example.cloudfour.paymentservice.commondto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreResponseDTO {
    private UUID id;
    private String name;
    private UUID ownerId;
}

