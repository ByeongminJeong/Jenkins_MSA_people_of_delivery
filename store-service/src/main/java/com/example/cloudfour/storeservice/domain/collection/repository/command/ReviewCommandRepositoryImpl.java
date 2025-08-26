package com.example.cloudfour.storeservice.domain.collection.repository.command;

import com.example.cloudfour.storeservice.domain.collection.converter.DocumentConverter;
import com.example.cloudfour.storeservice.domain.collection.document.ReviewDocument;
import com.example.cloudfour.storeservice.domain.collection.document.StoreDocument;
import com.example.cloudfour.storeservice.domain.review.entity.Review;
import com.mongodb.client.result.UpdateResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Transactional
public class ReviewCommandRepositoryImpl implements ReviewCustomCommandRepository{

    private final MongoTemplate mongoTemplate;

    @Override
    public void createReviewByStoreId(UUID storeId, List<Review> reviews) {
        Query query = Query.query(Criteria.where("storeId").in(storeId));
        StoreDocument storeDocument = mongoTemplate.findOne(query, StoreDocument.class);
        List<StoreDocument.Review> reviewsToUpdate = reviews.stream().map(
                DocumentConverter::toStoreDocumentReview).toList();
        Query updateQuery = Query.query(Criteria.where("storeId").is(storeId));
        Update update = Update.update("reviews", reviewsToUpdate);

        UpdateResult result = mongoTemplate.updateFirst(updateQuery, update, StoreDocument.class);
    }

    @Override
    public void deleteAllByReviewIdIn(List<UUID> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return;
        }

        Query reviewQuery = new Query(Criteria.where("reviewId").in(reviewIds));
        mongoTemplate.remove(reviewQuery, ReviewDocument.class);

        Query storeQuery = new Query(Criteria.where("reviews.id").in(reviewIds));
        Update update = new Update().pull("reviews", Query.query(Criteria.where("_id").in(reviewIds)));

        mongoTemplate.updateMulti(storeQuery, update, StoreDocument.class);
    }

    @Override
    public void updateStoreReview(UUID storeId, Integer reviewCount, Float rating) {
        Query query = new Query(Criteria.where("storeId").is(storeId));
        Update update = new Update()
                .set("reviewCount", reviewCount)
                .set("rating",rating);

        mongoTemplate.updateFirst(query,update,StoreDocument.class);
    }

}
