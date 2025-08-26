package com.example.cloudfour.storeservice.domain.review.dto;

import com.example.cloudfour.storeservice.domain.review.controller.ReviewCommonRequestDTO;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Getter;


public class ReviewRequestDTO {
    @Getter
    @Builder
    public static class ReviewCreateRequestDTO{
        @JsonUnwrapped
        ReviewCommonRequestDTO reviewCommonRequestDTO;
    }

    @Getter
    @Builder
    public static class ReviewUpdateRequestDTO{
        @JsonUnwrapped
        ReviewCommonRequestDTO reviewCommonRequestDTO;
    }
}
