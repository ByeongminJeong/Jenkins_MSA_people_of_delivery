package com.example.cloudfour.storeservice.scheduler;

import com.example.cloudfour.storeservice.domain.collection.converter.DocumentConverter;
import com.example.cloudfour.storeservice.domain.collection.document.ReviewDocument;
import com.example.cloudfour.storeservice.domain.collection.document.StoreDocument;
import com.example.cloudfour.storeservice.domain.collection.repository.command.MenuCommandRepository;
import com.example.cloudfour.storeservice.domain.collection.repository.command.ReviewCommandRepository;
import com.example.cloudfour.storeservice.domain.collection.repository.command.StoreCommandRepository;
import com.example.cloudfour.storeservice.domain.common.enums.SyncStatus;
import com.example.cloudfour.storeservice.domain.menu.entity.Menu;
import com.example.cloudfour.storeservice.domain.menu.entity.MenuOption;
import com.example.cloudfour.storeservice.domain.menu.repository.MenuOptionRepository;
import com.example.cloudfour.storeservice.domain.menu.repository.MenuRepository;
import com.example.cloudfour.storeservice.domain.review.entity.Review;
import com.example.cloudfour.storeservice.domain.review.repository.ReviewRepository;
import com.example.cloudfour.storeservice.domain.store.entity.Store;
import com.example.cloudfour.storeservice.domain.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MongoCreatedSyncScheduler {
    private final ReviewCommandRepository reviewCommandRepository;
    private final ReviewRepository reviewRepository;
    private final StoreCommandRepository storeCommandRepository;
    private final StoreRepository storeRepository;
    private final MenuCommandRepository menuCommandRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    //@Scheduled(cron = "0 0 0 * * *")
    @Scheduled(cron = "0 * * * * *")
    public void createStore(){
        log.info("MongoDB에 가게 동기화 시작");
        List<Store> stores = storeRepository.findAllBySyncStatus(SyncStatus.CREATED_PENDING);
        if(stores.isEmpty()){
            log.info("MongoDB에 생성할 가게가 존재하지 않음");
            return;
        }
        List<StoreDocument> storeDocuments = stores.stream().map(DocumentConverter::toStoreDocument).toList();
        storeCommandRepository.saveAll(storeDocuments);
        stores.forEach(Store::syncCreated);
        storeRepository.saveAll(stores);
        log.info("MongoDB에 가게 동기화 완료");
    }

    @Scheduled(cron = "0 * * * * *")
    public void createReview(){
        log.info("MongoDB에 리뷰 동기화 시작");
        List<Review> reviews = reviewRepository.findAllBySyncStatus(SyncStatus.CREATED_PENDING);
        if(reviews.isEmpty()){
            log.info("MongoDB에 생성할 리뷰가 존재하지 않음");
            return;
        }
        List<ReviewDocument> reviewDocuments = reviews.stream().map(DocumentConverter::toReviewDocument).toList();
        reviewCommandRepository.saveAll(reviewDocuments);
        reviews.forEach(Review::syncCreated);
        reviewRepository.saveAll(reviews);
        log.info("MongoDB에 리뷰 동기화 완료");
    }

    @Scheduled(cron = "0 * * * * *")
    public void createMenu(){
        log.info("MongoDB에 메뉴 동기화 시작");
        List<Menu> unsyncedMenus = menuRepository.findAllBySyncStatus(SyncStatus.CREATED_PENDING);
        if (unsyncedMenus.isEmpty()) {
            log.info("MongoDB에 생성할 메뉴가 존재하지 않음");
            return;
        }
        Map<UUID, List<Menu>> menusByStoreId = unsyncedMenus.stream()
                .collect(Collectors.groupingBy(menu->menu.getStore().getId()));

        List<UUID> storeIds = new ArrayList<>(menusByStoreId.keySet());
        menuCommandRepository.createMenuByStoreId(storeIds, menusByStoreId);
        unsyncedMenus.forEach(Menu::syncCreated);
        menuRepository.saveAll(unsyncedMenus);
        log.info("MongoDB에 메뉴 동기화 완료");
    }

    @Scheduled(cron = "0 * * * * *")
    public void createMenuOption(){
        log.info("MongoDB에 메뉴옵션 동기화 시작");
        List<MenuOption> unsyncedMenuOptions = menuOptionRepository.findAllBySyncStatus(SyncStatus.CREATED_PENDING);
        if(unsyncedMenuOptions.isEmpty()){
            log.info("MongoDB에 생성할 메뉴옵션이 존재하지 않음");
            return;
        }
        Map<UUID, List<MenuOption>> menuOptionsByMenuId = unsyncedMenuOptions.stream()
                .collect(Collectors.groupingBy(menuOption->menuOption.getMenu().getId()));

        List<UUID> menuIds = new ArrayList<>(menuOptionsByMenuId.keySet());
        menuCommandRepository.createMenuOptionByMenuId(menuIds, menuOptionsByMenuId);
        unsyncedMenuOptions.forEach(MenuOption::syncCreated);
        menuOptionRepository.saveAll(unsyncedMenuOptions);
        log.info("MongoDB에 메뉴 옵션 동기화 완료");
    }
}
