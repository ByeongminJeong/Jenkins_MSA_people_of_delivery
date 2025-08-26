package com.example.cloudfour.storeservice.domain.review.repository;

import com.example.cloudfour.storeservice.domain.common.enums.SyncStatus;
import com.example.cloudfour.storeservice.domain.review.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    void deleteAllByDeletedAtBefore(LocalDateTime deletedAtBefore);

    @Query("select count(r) > 0 from Review r where r.id =:ReviewId and r.user =:UserId and r.userIsDeleted = false")
    boolean existsByReviewIdAndUserId(@Param("ReviewId") UUID reviewId, @Param("UserId") UUID userId);

    @Query("select r from Review r where r.isDeleted = true")
    List<Review> findAllByIsDeleted();

    List<Review> findAllBySyncStatus(SyncStatus syncStatus);

    @Query("select r from Review r where r.store.id =:StoreId order by r.score desc, r.createdAt desc ")
    Slice<Review> findAllTopThreeReview(@Param("StoreId") UUID storeId, Pageable pageable);
}
