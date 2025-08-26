package com.example.cloudfour.storeservice.domain.store.converter;

import com.example.cloudfour.storeservice.domain.collection.document.StoreDocument;
import com.example.cloudfour.storeservice.domain.commondto.MenuCartResponseDTO;
import com.example.cloudfour.storeservice.domain.commondto.StoreCartResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.entity.Menu;
import com.example.cloudfour.storeservice.domain.store.controller.StoreCommonResponseDTO;
import com.example.cloudfour.storeservice.domain.store.dto.StoreRequestDTO;
import com.example.cloudfour.storeservice.domain.store.dto.StoreResponseDTO;
import com.example.cloudfour.storeservice.domain.store.entity.Store;
import java.time.LocalDateTime;
import java.util.List;

public class StoreConverter {

    public static Store toStore(StoreRequestDTO.StoreCreateRequestDTO dto) {
        return Store.builder()
                .name(dto.getStoreCommonRequestDTO().getName())
                .address(dto.getStoreCommonRequestDTO().getAddress())
                .storePicture(dto.getStorePicture())
                .phone(dto.getPhone())
                .content(dto.getContent())
                .minPrice(dto.getMinPrice())
                .deliveryTip(dto.getDeliveryTip())
                .operationHours(dto.getOperationHours())
                .closedDays(dto.getClosedDays())
                .rating(0f)
                .likeCount(0)
                .reviewCount(0)
                .build();
    }

    public static StoreResponseDTO.StoreCreateResponseDTO toStoreCreateResponseDTO(Store store) {
        return StoreResponseDTO.StoreCreateResponseDTO.builder()
                .storeCommonMainResponseDTO(toStoreCommonMainResponseDTO(store))
                .storeCommonsBaseResponseDTO(toStoreCommonBaseResponseDTO(store))
                .createdAt(store.getCreatedAt())
                .createdBy(store.getOwnerId())
                .build();
    }

    public static StoreResponseDTO.StoreUpdateResponseDTO toStoreUpdateResponseDTO(Store store){
        return StoreResponseDTO.StoreUpdateResponseDTO.builder()
                .storeCommonsBaseResponseDTO(toStoreCommonBaseResponseDTO(store))
                .category(store.getStoreCategory().getCategory())
                .updatedAt(store.getUpdatedAt())
                .build();
    }

    public static StoreResponseDTO.StoreListResponseDTO toStoreListResponseDTO(StoreDocument storeDocument) {
        return StoreResponseDTO.StoreListResponseDTO.builder()
                .storeCommonOptionResponseDTO(documentToStoreCommonOptionResponseDTO(storeDocument))
                .storeCommonsBaseResponseDTO(documentToStoreCommonBaseResponseDTO(storeDocument))
                .createdAt(storeDocument.getCreatedAt())
                .build();
    }

    public static StoreResponseDTO.StoreCursorListResponseDTO toStoreCursorListResponseDTO(
            List<StoreResponseDTO.StoreListResponseDTO> storeList,
            LocalDateTime nextCursor
    ) {
        return StoreResponseDTO.StoreCursorListResponseDTO.builder()
                .storeList(storeList)
                .nextCursor(nextCursor)
                .build();
    }

    public static StoreResponseDTO.StoreDetailResponseDTO toStoreDetailResponseDTO(Store store) {
        return StoreResponseDTO.StoreDetailResponseDTO.builder()
                .userId(store.getOwnerId())
                .storeCommonMainResponseDTO(toStoreCommonMainResponseDTO(store))
                .storeCommonOptionResponseDTO(toStoreCommonOptionResponseDTO(store))
                .storeCommonsBaseResponseDTO(toStoreCommonBaseResponseDTO(store))
                .build();
    }

    public static StoreResponseDTO.StoreDetailResponseDTO documentToStoreDetailResponseDTO(StoreDocument storeDocument) {
        return StoreResponseDTO.StoreDetailResponseDTO.builder()
                .userId(storeDocument.getUserId())
                .storeCommonMainResponseDTO(documentToStoreCommonMainResponseDTO(storeDocument))
                .storeCommonOptionResponseDTO(documentToStoreCommonOptionResponseDTO(storeDocument))
                .storeCommonsBaseResponseDTO(documentToStoreCommonBaseResponseDTO(storeDocument))
                .build();
    }

    public static StoreCommonResponseDTO.StoreCommonMainResponseDTO toStoreCommonMainResponseDTO (Store store) {
        return StoreCommonResponseDTO.StoreCommonMainResponseDTO.builder()
                .phone(store.getPhone())
                .content(store.getContent())
                .minPrice(store.getMinPrice())
                .deliveryTip(store.getDeliveryTip())
                .operationHours(store.getOperationHours())
                .closedDays(store.getClosedDays())
                .category(store.getStoreCategory().getCategory())
                .build();
    }

    public static StoreCommonResponseDTO.StoreCommonsBaseResponseDTO toStoreCommonBaseResponseDTO(Store store){
        return StoreCommonResponseDTO.StoreCommonsBaseResponseDTO.builder()
                .storeId(store.getId())
                .name(store.getName())
                .address(store.getAddress())
                .storePicture(store.getStorePicture())
                .build();
    }

    public static StoreCommonResponseDTO.StoreCommonOptionResponseDTO toStoreCommonOptionResponseDTO(Store store){
        return StoreCommonResponseDTO.StoreCommonOptionResponseDTO.builder()
                .rating(store.getRating())
                .reviewCount(store.getReviewCount())
                .build();
    }

    public static StoreCommonResponseDTO.StoreCommonOptionResponseDTO documentToStoreCommonOptionResponseDTO(StoreDocument storeDocument) {
        return StoreCommonResponseDTO.StoreCommonOptionResponseDTO.builder()
                .rating(storeDocument.getRating())
                .reviewCount(storeDocument.getReviewCount())
                .build();
    }


    public static StoreCommonResponseDTO.StoreCommonMainResponseDTO documentToStoreCommonMainResponseDTO (StoreDocument storeDocument) {
        return StoreCommonResponseDTO.StoreCommonMainResponseDTO.builder()
                .phone(storeDocument.getPhone())
                .content(storeDocument.getContent())
                .minPrice(storeDocument.getMinPrice())
                .deliveryTip(storeDocument.getDeliveryTip())
                .operationHours(storeDocument.getOperationHours())
                .closedDays(storeDocument.getClosedDays())
                .category(storeDocument.getStoreCategory().getStoreCategoryName())
                .build();
    }

    public static StoreCommonResponseDTO.StoreCommonsBaseResponseDTO documentToStoreCommonBaseResponseDTO(StoreDocument storeDocument){
        return StoreCommonResponseDTO.StoreCommonsBaseResponseDTO.builder()
                .storeId(storeDocument.getStoreId())
                .name(storeDocument.getName())
                .address(storeDocument.getAddress())
                .storePicture(storeDocument.getPictureURL())
                .build();
    }

    public static StoreCartResponseDTO toFindStoreDTO(Store store){
        return StoreCartResponseDTO.builder()
                .storeId(store.getId())
                .userId(store.getOwnerId())
                .name(store.getName())
                .build();
    }
}
