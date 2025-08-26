package com.example.cloudfour.storeservice.scheduler;

import com.example.cloudfour.storeservice.domain.collection.repository.command.MenuCommandRepository;
import com.example.cloudfour.storeservice.domain.collection.repository.command.ReviewCommandRepository;
import com.example.cloudfour.storeservice.domain.collection.repository.command.StoreCommandRepository;
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

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MongoDeletedSyncScheduler {
    private final ReviewCommandRepository reviewCommandRepository;
    private final ReviewRepository reviewRepository;
    private final StoreCommandRepository storeCommandRepository;
    private final StoreRepository storeRepository;
    private final MenuCommandRepository menuCommandRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    @Scheduled(cron = "0 * * * * *")
    public void deleteStore(){
        log.info("MongoDB에 가게 삭제 동기화 시작");
        List<Store> stores = storeRepository.findAllByIsDeleted();
        if(stores.isEmpty()){
            log.info("삭제할 가게 데이터 없음");
            return;
        }
        List<UUID> storeIds = stores.stream().map(Store::getId).toList();
        storeCommandRepository.deleteAllByStoreIdIn(storeIds);
        log.info("MongoDB에 가게 삭제 완료");
    }

    @Scheduled(cron = "0 * * * * *")
    public void deleteReview(){
        log.info("MongoDB에 리뷰 삭제 동기화 시작");
        List<Review> reviews = reviewRepository.findAllByIsDeleted();
        if(reviews.isEmpty()){
            log.info("삭제할 리뷰 데이터 없음");
            return;
        }
        List<UUID> reviewIds = reviews.stream().map(Review::getId).toList();
        reviewCommandRepository.deleteAllByReviewIdIn(reviewIds);
        log.info("MongoDB에 리뷰 삭제 완료");
    }

    @Scheduled(cron = "0 * * * * *")
    public void deleteMenu(){
        log.info("MongoDB에 메뉴 삭제 동기화 시작");
        List<Menu> menus = menuRepository.findAllByIsDeleted();
        if(menus.isEmpty()){
            log.info("삭제할 메뉴 데이터 없음");
            return;
        }
        List<UUID> menuIds = menus.stream().map(Menu::getId).toList();
        menuCommandRepository.deleteAllByMenuIdIn(menuIds);
        log.info("MongoDB에 메뉴 삭제 완료");
    }

    @Scheduled(cron = "0 * * * * *")
    public void deleteMenuOption(){
        log.info("MongoDB에 메뉴 옵션 삭제 동기화 시작");
        List<MenuOption> menuOptions = menuOptionRepository.findAllByIsDeleted();
        if(menuOptions.isEmpty()){
            log.info("삭제할 메뉴 옵션 데이터 없음");
            return;
        }
        List<UUID> menuOptionIds = menuOptions.stream().map(MenuOption::getId).toList();
        menuCommandRepository.deleteAllByMenuOptionIdIn(menuOptionIds);
        log.info("MongoDB에 메뉴 옵션 삭제 완료");
    }
}
