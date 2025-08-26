package com.example.cloudfour.storeservice.domain.menu.converter;

import com.example.cloudfour.storeservice.domain.collection.document.StoreDocument;
import com.example.cloudfour.storeservice.domain.commondto.MenuOptionCartResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.controller.MenuOptionCommonResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.dto.MenuOptionResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.entity.MenuOption;

public class MenuOptionConverter {
    public static MenuOptionResponseDTO.MenuOptionSimpleResponseDTO toMenuOptionSimpleResponseDTO(MenuOption option) {
        return MenuOptionResponseDTO.MenuOptionSimpleResponseDTO.builder()
                .menuOptionId(option.getId())
                .additionalPrice(option.getAdditionalPrice())
                .optionName(option.getOptionName())
                .build();
    }

    public static MenuOptionResponseDTO.MenuOptionSimpleResponseDTO documentToMenuOptionSimpleResponseDTO(StoreDocument.MenuOption option) {
        return MenuOptionResponseDTO.MenuOptionSimpleResponseDTO.builder()
                .menuOptionId(option.getId())
                .additionalPrice(option.getAdditionalPrice())
                .optionName(option.getOptionName())
                .build();
    }

    public static MenuOptionResponseDTO.MenuOptionDetailResponseDTO toMenuOptionDetailResponseDTO(MenuOption option) {
        return MenuOptionResponseDTO.MenuOptionDetailResponseDTO.builder()
                .menuOptionCommonResponseDTO(toMenuOptionCommonResponseDTO(option))
                .build();
    }

    public static MenuOptionCommonResponseDTO toMenuOptionCommonResponseDTO(MenuOption option) {
        return MenuOptionCommonResponseDTO.builder()
                .additionalPrice(option.getAdditionalPrice())
                .optionName(option.getOptionName())
                .menuId(option.getMenu().getId())
                .menuName(option.getMenu().getName())
                .menuOptionId(option.getId())
                .build();
    }

    public static MenuOptionCartResponseDTO toFindMenuOptionDTO(MenuOption option){
        return MenuOptionCartResponseDTO.builder()
                .menuOptionId(option.getId())
                .menuId(option.getMenu().getId())
                .menuName(option.getMenu().getName())
                .additionalPrice(option.getAdditionalPrice())
                .optionName(option.getOptionName())
                .build();
    }
}