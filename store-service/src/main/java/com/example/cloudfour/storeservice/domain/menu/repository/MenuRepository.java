package com.example.cloudfour.storeservice.domain.menu.repository;

import com.example.cloudfour.storeservice.domain.common.enums.SyncStatus;
import com.example.cloudfour.storeservice.domain.menu.entity.Menu;
import com.example.cloudfour.storeservice.domain.menu.repository.querydsl.MenuQueryDslRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MenuRepository extends JpaRepository<Menu, UUID>, MenuQueryDslRepository {
    @Query("select m from Menu m where m.isDeleted = true")
    List<Menu> findAllByIsDeleted();

    void deleteAllByDeletedAtBefore(LocalDateTime deletedAtBefore);

    List<Menu> findAllBySyncStatus(SyncStatus syncStatus);
}