package com.example.cloudfour.storeservice.domain.store.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StoreCommonRequestDTO {
    @NotNull
    @Size(max = 255)
    private String name;

    @NotNull
    @Size(max=500)
    private String address;

    @NotNull
    @Size(max=255)
    private String category;
}
