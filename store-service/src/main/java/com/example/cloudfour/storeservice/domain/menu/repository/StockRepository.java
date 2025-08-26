package com.example.cloudfour.storeservice.domain.menu.repository;

import com.example.cloudfour.storeservice.domain.common.enums.SyncStatus;
import com.example.cloudfour.storeservice.domain.menu.entity.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockRepository extends JpaRepository<Stock, UUID> {
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select s from Stock s where s.id = :id")
    Optional<Stock> findByIdWithOptimisticLock(UUID id);

    List<Stock> findAllBySyncStatus(SyncStatus syncStatus);
}
