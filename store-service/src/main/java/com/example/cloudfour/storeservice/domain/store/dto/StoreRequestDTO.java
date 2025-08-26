package com.example.cloudfour.storeservice.domain.store.dto;

import com.example.cloudfour.storeservice.domain.store.controller.StoreCommonRequestDTO;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;


public class StoreRequestDTO {

    @Getter
    @Builder
    public static class StoreCreateRequestDTO {
        @JsonUnwrapped
        StoreCommonRequestDTO storeCommonRequestDTO;
        private String storePicture;

        @NotNull
        @Size(max=50)
        private String phone;

        @NotNull
        @Size(max=500)
        private String content;

        @NotNull
        @Min(value = 0)
        private Integer minPrice;

        @NotNull
        @Min(value=0)
        private Integer deliveryTip;

        @NotNull
        @Size(max=255)
        private String operationHours;

        @NotNull
        @Size(max=255)
        private String closedDays;
    }

    @Getter
    @Builder
    public static class StoreUpdateRequestDTO {
        @JsonUnwrapped
        StoreCommonRequestDTO storeCommonRequestDTO;
    }
}
