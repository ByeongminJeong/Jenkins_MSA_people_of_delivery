package com.example.cloudfour.storeservice.domain.review.entity;

import com.example.cloudfour.modulecommon.entity.BaseEntity;
import com.example.cloudfour.storeservice.domain.common.enums.SyncStatus;
import com.example.cloudfour.storeservice.domain.review.dto.ReviewRequestDTO;
import com.example.cloudfour.storeservice.domain.review.exception.ReviewErrorCode;
import com.example.cloudfour.storeservice.domain.review.exception.ReviewException;
import com.example.cloudfour.storeservice.domain.store.entity.Store;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "p_review")
public class Review extends BaseEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private Float score;

    @Column(nullable = false)
    private String content;

    @Lob
    private String pictureUrl;

    private String userName;

    @Column(name = "userId" ,nullable = false)
    private UUID user;

    @Enumerated(EnumType.STRING)
    @Column(name = "syncStatus", nullable = false)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.CREATED_PENDING;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storeId", nullable = false)
    private Store store;

    @Column(nullable = false)
    @Builder.Default
    private boolean userIsDeleted = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean storeIsDeleted = false;

    public static class ReviewBuilder{
        private ReviewBuilder id(UUID id){
            throw new ReviewException(ReviewErrorCode.CREATE_FAILED);
        }
    }

    public void setUser(UUID user){
        this.user = user;
    }

    public void setStore(Store store){
        this.store = store;
        store.getReviews().add(this);
        store.createReview(this.getScore());
        this.syncStatus = SyncStatus.CREATED_PENDING;
    }

    public void update(ReviewRequestDTO.ReviewUpdateRequestDTO reviewUpdateRequestDTO){
        this.score = reviewUpdateRequestDTO.getReviewCommonRequestDTO().getScore();
        this.content = reviewUpdateRequestDTO.getReviewCommonRequestDTO().getContent();
        this.pictureUrl = reviewUpdateRequestDTO.getReviewCommonRequestDTO().getPictureUrl();
    }

    public void softDelete() {
        this.isDeleted = true;
        store.deleteReview(this.getScore());
    }

    public void syncCreated(){
        this.syncStatus = SyncStatus.CREATED_SYNCED;
    }

    public void syncUpdated(){
        this.syncStatus = SyncStatus.UPDATED_SYNCED;
    }
}
