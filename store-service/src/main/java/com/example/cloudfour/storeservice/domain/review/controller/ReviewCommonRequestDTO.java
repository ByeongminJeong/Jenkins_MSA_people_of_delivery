package com.example.cloudfour.storeservice.domain.review.controller;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReviewCommonRequestDTO {
    @NotNull
    private UUID storeId;

    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private Float score;

    @NotNull
    @Size(max = 500)
    private String content;

    private String pictureUrl;
}
