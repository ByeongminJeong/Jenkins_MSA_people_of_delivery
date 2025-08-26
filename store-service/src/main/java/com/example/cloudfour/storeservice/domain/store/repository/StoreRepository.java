package com.example.cloudfour.storeservice.domain.store.repository;

import com.example.cloudfour.storeservice.domain.common.enums.SyncStatus;
import com.example.cloudfour.storeservice.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID>{
    void deleteAllByDeletedAtBefore(LocalDateTime deletedAtBefore);
    Optional<Store> findByIdAndIsDeletedFalse(UUID storeId);
    Boolean existsByNameAndIsDeletedFalse(String name);

    @Query("select s from Store s where s.isDeleted = true")
    List<Store> findAllByIsDeleted();

    List<Store> findAllBySyncStatus(SyncStatus syncStatus);

    List<Store> findAllByIsDeletedIsFalse();

    boolean existsByIdAndIsDeletedFalse(UUID storeId);
}
