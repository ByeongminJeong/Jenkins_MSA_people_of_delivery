package com.example.cloudfour.storeservice.domain.menu.service.command;

import com.example.cloudfour.storeservice.domain.menu.entity.Stock;
import com.example.cloudfour.storeservice.domain.menu.exception.StockErrorCode;
import com.example.cloudfour.storeservice.domain.menu.exception.StockException;
import com.example.cloudfour.storeservice.domain.menu.repository.StockRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
@Retryable(
        value = OptimisticLockException.class,
        maxAttempts = 5,
        backoff = @Backoff(delay = 100)
)
public class StockCommandService {
    private final StockRepository stockRepository;

    public void decreaseStock(UUID stockId, Long quantity){
        Stock stock = stockRepository.findByIdWithOptimisticLock(stockId).orElseThrow(()-> new StockException(StockErrorCode.NOT_FOUND));
        stock.decrease(quantity);
    }

    public void increaseStock(UUID stockId, Long quantity){
        Stock stock = stockRepository.findByIdWithOptimisticLock(stockId).orElseThrow(()-> new StockException(StockErrorCode.NOT_FOUND));
        stock.increase(quantity);
    }

    @Recover
    public void recover(OptimisticLockException e, UUID stockId, Long quantity) {
        log.error("재고 차감 재시도 중 충돌 무한 반복 - stockId={}, quantity={}", stockId, quantity, e);
        throw e;
    }
}
