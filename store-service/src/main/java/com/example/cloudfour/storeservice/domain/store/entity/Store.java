package com.example.cloudfour.storeservice.domain.store.entity;

import com.example.cloudfour.modulecommon.entity.BaseEntity;
import com.example.cloudfour.storeservice.domain.common.enums.SyncStatus;
import com.example.cloudfour.storeservice.domain.menu.entity.Menu;
import com.example.cloudfour.storeservice.domain.region.entity.Region;
import com.example.cloudfour.storeservice.domain.review.entity.Review;
import com.example.cloudfour.storeservice.domain.store.exception.StoreErrorCode;
import com.example.cloudfour.storeservice.domain.store.exception.StoreException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "p_store")
public class Store extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 255, unique = true)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    private String storePicture;

    @Column(nullable = false, length = 255)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer minPrice;

    @Column(nullable = false)
    private Integer deliveryTip;

    private Float rating;

    private Integer likeCount;

    private Integer reviewCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "syncStatus", nullable = false)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.CREATED_PENDING;

    @Column(nullable = false, length = 255)
    private String operationHours;

    @Column(nullable = false, length = 255)
    private String closedDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storeCategoryId", nullable = false)
    private StoreCategory storeCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regionId", nullable = false)
    private Region region;
    
    @Column(nullable = false, name = "owner_id")
    private UUID ownerId;

    @Column(nullable = false)
    @Builder.Default
    private boolean userIsDeleted = false;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "store", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Menu> menus = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "store", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    public static class StoreBuilder{
        private StoreBuilder id(UUID id){
            throw new StoreException(StoreErrorCode.CREATE_FAILED);
        }
    }

    public void setStoreCategory(StoreCategory storeCategory) {
        this.storeCategory = storeCategory;
        storeCategory.getStores().add(this);
    }

    public void setRegion(Region region) {
        this.region = region;
        region.getStores().add(this);
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public void update(String name, String address) {
        if (name != null) this.name = name;
        if (address != null) this.address = address;
    }

    public void createReview(Float score){
        this.reviewCount++;

        if (this.reviewCount == 1) {
            this.rating = score;
        } else {
            Float totalScore = (this.rating * (this.reviewCount - 1)) + score;
            this.rating = totalScore / this.reviewCount;
        }

        this.rating = Math.round(this.rating * 100.0f) / 100.0f;

        this.syncStatus = SyncStatus.UPDATED_PENDING;
    }

    public void deleteReview(Float score){
        if (this.reviewCount > 0) {
            this.reviewCount--;

            if (this.reviewCount == 0) {
                this.rating = 0.0f;
            } else {
                Float totalScore = (this.rating * (this.reviewCount + 1)) - score;
                this.rating = totalScore / this.reviewCount;

                this.rating = Math.round(this.rating * 100.0f) / 100.0f;
            }

            this.syncStatus = SyncStatus.UPDATED_PENDING;
        }
    }

    public void syncCreated(){
        this.syncStatus = SyncStatus.CREATED_SYNCED;
    }

    public void syncUpdated(){
        this.syncStatus = SyncStatus.UPDATED_SYNCED;
    }
}
