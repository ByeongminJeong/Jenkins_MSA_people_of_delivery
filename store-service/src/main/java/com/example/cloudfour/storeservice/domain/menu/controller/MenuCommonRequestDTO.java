package com.example.cloudfour.storeservice.domain.menu.controller;
import com.example.cloudfour.storeservice.domain.menu.enums.MenuStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

public class MenuCommonRequestDTO {
    @Getter
    @Builder
    public static class MenuCommonMainRequestDTO {
        @NotNull
        @Size(max = 50)
        private String name;

        @NotNull
        @Size(max = 255)
        private String content;

        @NotNull
        @Min(value = 0)
        private Integer price;

        private String menuPicture;

        @NotNull
        @Size(max=50)
        private MenuStatus status;

        @NotNull
        @Size(max=255)
        private String category;
    }

    @Getter
    @Builder
    public static class MenuOptionCommonRequestDTO {
        @NotNull
        @Size(max = 50)
        private String optionName;

        @NotNull(message = "추가 가격은 필수입니다.")
        @Min(value = 0)
        private Integer additionalPrice;
    }
}
