package com.example.cloudfour.storeservice.scheduler;

import com.example.cloudfour.storeservice.domain.collection.repository.command.ReviewCommandRepository;
import com.example.cloudfour.storeservice.domain.collection.repository.command.StockCommandRepository;
import com.example.cloudfour.storeservice.domain.common.enums.SyncStatus;
import com.example.cloudfour.storeservice.domain.menu.entity.Stock;
import com.example.cloudfour.storeservice.domain.menu.repository.StockRepository;
import com.example.cloudfour.storeservice.domain.review.entity.Review;
import com.example.cloudfour.storeservice.domain.review.repository.ReviewRepository;
import com.example.cloudfour.storeservice.domain.store.entity.Store;
import com.example.cloudfour.storeservice.domain.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MongoUpdatedSyncScheduler {
    private final ReviewCommandRepository reviewCommandRepository;
    private final StockCommandRepository stockCommandRepository;
    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final StockRepository stockRepository;

    @Scheduled(cron = "0 * * * * *")
    public void refreshReviews(){
        log.info("MongoDB에 리뷰 최신화 시작");
        Pageable pageable = PageRequest.of(0,3);
        List<Store> stores = storeRepository.findAllByIsDeletedIsFalse();
        for(Store store:stores){
            Slice<Review> top3Review = reviewRepository.findAllTopThreeReview(store.getId(),pageable);
            List<Review> reviews = top3Review.toList();
            reviewCommandRepository.createReviewByStoreId(store.getId(), reviews);
            reviewCommandRepository.updateStoreReview(store.getId(),store.getReviewCount(),store.getRating());
            log.info("MongoDB에 리뷰 최신화 완료");
        }
    }

    @Scheduled(cron = "0 * * * * *")
    public void refreshQuantity(){
        log.info("MongoDB에 수량 최신화 시작");
        List<Stock> pendingStocks = stockRepository.findAllBySyncStatus(SyncStatus.UPDATED_PENDING);
        if(pendingStocks.isEmpty()){
            log.info("MongoDB에 최신화할 수량아 존재하지 않음");
        }

        for(Stock stock: pendingStocks){
            stockCommandRepository.updateStockByMenuId(stock.getMenu().getId(), stock.getQuantity());
            stock.setSyncStatus(SyncStatus.UPDATED_SYNCED);
            stockRepository.save(stock);
        }
        log.info("MongoDB에 수량 최신화 완료");
    }
}
