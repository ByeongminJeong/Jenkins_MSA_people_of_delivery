package com.example.cloudfour.storeservice.domain.collection.converter;

import com.example.cloudfour.storeservice.domain.collection.document.ReviewDocument;
import com.example.cloudfour.storeservice.domain.collection.document.StoreDocument;
import com.example.cloudfour.storeservice.domain.menu.entity.Menu;
import com.example.cloudfour.storeservice.domain.menu.entity.MenuOption;
import com.example.cloudfour.storeservice.domain.review.entity.Review;
import com.example.cloudfour.storeservice.domain.store.entity.Store;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class DocumentConverter {
    public static StoreDocument toStoreDocument(Store store){
        return StoreDocument.builder()
                .storeId(store.getId())
                .userId(store.getOwnerId())
                .name(store.getName())
                .address(store.getAddress())
                .phone(store.getPhone())
                .content(store.getContent())
                .minPrice(store.getMinPrice())
                .deliveryTip(store.getDeliveryTip())
                .rating(store.getRating())
                .likeCount(store.getLikeCount())
                .reviewCount(store.getReviewCount())
                .OperationHours(store.getOperationHours())
                .closedDays(store.getClosedDays())
                .siDo(store.getRegion().getSiDo())
                .siGunGu(store.getRegion().getSiGunGu())
                .eupMyeonDong(store.getRegion().getEupMyeonDong())
                .pictureURL(store.getStorePicture())
                .createdAt(store.getCreatedAt())
                .storeCategory(store.getStoreCategory() == null ? null :
                        StoreDocument.StoreCategory.builder()
                                .id(store.getStoreCategory().getId())
                                .storeCategoryName(store.getStoreCategory().getCategory())
                                .build())
                .menus(store.getMenus() == null ? null :
                        store.getMenus().stream().map(m ->
                                StoreDocument.Menu.builder()
                                        .id(m.getId())
                                        .name(m.getName())
                                        .content(m.getContent())
                                        .price(m.getPrice())
                                        .menuPicture(m.getMenuPicture())
                                        .menuStatus(m.getStatus())
                                        .stock(m.getStock()==null ? null:
                                                StoreDocument.Stock.builder()
                                                        .id(m.getStock().getId())
                                                        .quantity(m.getStock().getQuantity())
                                                        .build())
                                        .menuCategory(m.getMenuCategory() == null ? null :
                                                StoreDocument.MenuCategory.builder()
                                                        .id(m.getMenuCategory().getId())
                                                        .menuCategoryName(m.getMenuCategory().getCategory())
                                                        .build())
                                        .menuOptions(m.getMenuOptions() == null ? null :
                                                m.getMenuOptions().stream().map(opt ->
                                                                StoreDocument.MenuOption.builder()
                                                                        .id(opt.getId())
                                                                        .additionalPrice(opt.getAdditionalPrice())
                                                                        .optionName(opt.getOptionName())
                                                                        .build())
                                                        .toList())
                                        .createdAt(m.getCreatedAt())
                                        .build()
                        ).toList())
                .reviews(store.getReviews() == null ? null :
                        store.getReviews().stream().map(r ->
                                StoreDocument.Review.builder()
                                        .id(r.getId())
                                        .score(r.getScore())
                                        .content(r.getContent())
                                        .build()
                        ).toList())
                .build();
    }

    public static ReviewDocument toReviewDocument(Review review){
        return ReviewDocument.builder()
                .reviewId(review.getId())
                .userId(review.getUser())
                .storeId(review.getStore().getId())
                .userName(review.getUserName())
                .score(review.getScore())
                .content(review.getContent())
                .pictureUrl(review.getPictureUrl())
                .createdAt(review.getCreatedAt())
                .build();
    }

    public static StoreDocument.Menu toStoreDocumentMenu(Menu menu){
        return StoreDocument.Menu.builder()
                .id(menu.getId())
                .name(menu.getName())
                .content(menu.getContent())
                .price(menu.getPrice())
                .menuPicture(menu.getMenuPicture())
                .menuStatus(menu.getStatus())
                .stock(menu.getStock()==null ? null:
                        StoreDocument.Stock.builder()
                                .id(menu.getStock().getId())
                                .quantity(menu.getStock().getQuantity())
                                .build())
                .menuCategory(menu.getMenuCategory() != null ?
                        StoreDocument.MenuCategory.builder()
                                .id(menu.getMenuCategory().getId())
                                .menuCategoryName(menu.getMenuCategory().getCategory())
                                .build() : null)
                .menuOptions(menu.getMenuOptions() != null ?
                        menu.getMenuOptions().stream()
                                .map(option -> StoreDocument.MenuOption.builder()
                                        .id(option.getId())
                                        .additionalPrice(option.getAdditionalPrice())
                                        .optionName(option.getOptionName())
                                        .build())
                                .collect(Collectors.toList()) : new ArrayList<>())
                .createdAt(menu.getCreatedAt())
                .build();
    }

    public static StoreDocument.MenuOption toStoreDocumentMenuOption(MenuOption menuOption) {
        return StoreDocument.MenuOption.builder()
                .id(menuOption.getId())
                .additionalPrice(menuOption.getAdditionalPrice())
                .optionName(menuOption.getOptionName())
                .build();
    }

    public static StoreDocument.Review toStoreDocumentReview(Review review) {
        return StoreDocument.Review.builder()
                .id(review.getId())
                .score(review.getScore())
                .content(review.getContent())
                .build();
    }
}
