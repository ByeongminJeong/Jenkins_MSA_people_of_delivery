package com.example.cloudfour.storeservice.domain.collection.repository.command;

import com.example.cloudfour.storeservice.domain.review.entity.Review;

import java.util.List;
import java.util.UUID;

public interface ReviewCustomCommandRepository {
    public void createReviewByStoreId(UUID storeId, List<Review> reviews);
    public void deleteAllByReviewIdIn(List<UUID> reviewIds);
    public void updateStoreReview(UUID storeId, Integer reviewCount, Float rating);
}
