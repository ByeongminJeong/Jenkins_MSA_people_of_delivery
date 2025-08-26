package com.example.cloudfour.storeservice.domain.menu.repository;

import com.example.cloudfour.storeservice.domain.common.enums.SyncStatus;
import com.example.cloudfour.storeservice.domain.menu.entity.MenuOption;
import com.example.cloudfour.storeservice.domain.menu.repository.querydsl.MenuOptionQueryDslRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MenuOptionRepository extends JpaRepository<MenuOption, UUID> , MenuOptionQueryDslRepository {
    void deleteAllByDeletedAtBefore(LocalDateTime deletedAtBefore);

    @Query("select mo from MenuOption mo where mo.isDeleted = true")
    List<MenuOption> findAllByIsDeleted();

    List<MenuOption> findAllBySyncStatus(SyncStatus syncStatus);
}