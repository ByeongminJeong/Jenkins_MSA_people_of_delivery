package com.example.cloudfour.storeservice.scheduler;

import com.example.cloudfour.storeservice.domain.menu.repository.MenuOptionRepository;
import com.example.cloudfour.storeservice.domain.menu.repository.MenuRepository;
import com.example.cloudfour.storeservice.domain.review.repository.ReviewRepository;
import com.example.cloudfour.storeservice.domain.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class StoreServiceScheduler {
    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;

    @Scheduled(cron = "0 * * * * *")
    public void deleteStore(){
        LocalDateTime oneDays = LocalDateTime.now().minusDays(1);
        storeRepository.deleteAllByDeletedAtBefore(oneDays);
        log.info("Soft Deleted된 Store 삭제");
    }

    @Scheduled(cron = "0 * * * * *")
    public void deleteMenu(){
        LocalDateTime oneDays = LocalDateTime.now().minusDays(1);
        menuRepository.deleteAllByDeletedAtBefore(oneDays);
        log.info("Soft Deleted된 Store 삭제");
    }

    @Scheduled(cron = "0 * * * * *")
    public void deleteMenuOption(){
        LocalDateTime oneDays = LocalDateTime.now().minusDays(1);
        menuOptionRepository.deleteAllByDeletedAtBefore(oneDays);
        log.info("Soft Deleted된 Store 삭제");
    }
    
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteReview(){
        LocalDateTime oneDays = LocalDateTime.now().minusDays(1);
        reviewRepository.deleteAllByDeletedAtBefore(oneDays);
        log.info("Soft Deleted된 Review 삭제");
    }
}
