package com.example.cloudfour.storeservice.domain.store.entity;

import com.example.cloudfour.storeservice.domain.common.enums.SyncStatus;
import com.example.cloudfour.storeservice.domain.store.exception.StoreCategoryErrorCode;
import com.example.cloudfour.storeservice.domain.store.exception.StoreCategoryException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "p_storecategory")
public class StoreCategory {
    @Id
    @GeneratedValue
    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true,length = 255)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "syncStatus", nullable = false)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.CREATED_PENDING;

    @OneToMany(mappedBy = "storeCategory", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Store> stores = new ArrayList<>();

    public static class StoreCategoryBuilder {
        private StoreCategoryBuilder id(UUID id){
            throw new StoreCategoryException(StoreCategoryErrorCode.CREATE_FAILED);
        }
    }
}
