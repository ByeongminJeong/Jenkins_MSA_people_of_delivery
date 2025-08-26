package com.example.cloudfour.storeservice.domain.menu.dto;

import com.example.cloudfour.storeservice.domain.menu.controller.MenuCommonResponseDTO;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MenuResponseDTO {

    @Getter
    @Builder
    public static class MenuDetailResponseDTO {
        @JsonUnwrapped
        MenuCommonResponseDTO menuCommonResponseDTO;
        private Long quantity;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<MenuOptionDTO> menuOptions;
    }

    @Getter
    @Builder
    public static class MenuOptionDTO {
        private UUID menuOptionId;
        private String optionName;
        private Integer additionalPrice;
    }

    @Getter
    @Builder
    public static class MenuListResponseDTO {
        @JsonUnwrapped
        MenuCommonResponseDTO menuCommonResponseDTO;
        private java.time.LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class MenuTopResponseDTO {
        @JsonUnwrapped
        MenuCommonResponseDTO menuCommonResponseDTO;
        private String storeName;
    }

    @Getter
    @Builder
    public static class MenuTimeTopResponseDTO {
        @JsonUnwrapped
        MenuCommonResponseDTO menuCommonResponseDTO;
        private String storeName;
        private Integer orderCount;
    }

    @Getter
    @Builder
    public static class MenuRegionTopResponseDTO {
        @JsonUnwrapped
        MenuCommonResponseDTO menuCommonResponseDTO;
        private String storeName;
        private String region;
    }

    @Getter
    @Builder
    public static class MenuStoreListResponseDTO {
        private List<MenuListResponseDTO> menus;
    }
}