package com.example.cloudfour.storeservice.domain.review.service.command;

import com.example.cloudfour.modulecommon.dto.CurrentUser;
import com.example.cloudfour.storeservice.domain.collection.repository.command.ReviewCommandRepository;
import com.example.cloudfour.storeservice.domain.review.converter.ReviewConverter;
import com.example.cloudfour.storeservice.domain.review.dto.ReviewRequestDTO;
import com.example.cloudfour.storeservice.domain.review.dto.ReviewResponseDTO;
import com.example.cloudfour.storeservice.domain.review.entity.Review;
import com.example.cloudfour.storeservice.domain.review.exception.ReviewErrorCode;
import com.example.cloudfour.storeservice.domain.review.exception.ReviewException;
import com.example.cloudfour.storeservice.domain.review.repository.ReviewRepository;
import com.example.cloudfour.storeservice.domain.store.entity.Store;
import com.example.cloudfour.storeservice.domain.store.exception.StoreErrorCode;
import com.example.cloudfour.storeservice.domain.store.exception.StoreException;
import com.example.cloudfour.storeservice.domain.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewCommandService {
    private final StoreRepository storeRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewCommandRepository reviewCommandRepository;

    public ReviewResponseDTO.ReviewCreateResponseDTO createReview(ReviewRequestDTO.ReviewCreateRequestDTO reviewCreateRequestDTO,
          CurrentUser user) {
        if(user==null){
            log.warn("리뷰 생성 권한 없음");
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_ACCESS);
        }
        log.info("리뷰 생성 권한 확인 성공");
        Store findStore = storeRepository.findById(reviewCreateRequestDTO.getReviewCommonRequestDTO().getStoreId())
                .orElseThrow(()->{
                    log.warn("존재하지 않는 가게");
            return new StoreException(StoreErrorCode.NOT_FOUND);
        });
        Review review = ReviewConverter.toReview(reviewCreateRequestDTO);
        review.setUser(user.id());
        review.setStore(findStore);
        reviewRepository.save(review);
        log.info("리뷰 생성 성공");
        return ReviewConverter.toReviewCreateResponseDTO(review);
    }

    public void deleteReview(UUID reviewId, CurrentUser user) {
        Review findReview = reviewRepository.findById(reviewId).orElseThrow(()->{
            log.warn("존재하지 않는 리뷰");
            return new ReviewException(ReviewErrorCode.NOT_FOUND);
        });
        if(user == null || !reviewRepository.existsByReviewIdAndUserId(reviewId, user.id())) {
            log.warn("리뷰 삭제 권한 없음");
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_ACCESS);
        }
        log.info("리뷰 삭제 권한 확인 성공");
        findReview.softDelete();
        log.info("리뷰 삭제 성공");
    }

    public ReviewResponseDTO.ReviewUpdateResponseDTO updateReview(ReviewRequestDTO.ReviewUpdateRequestDTO reviewUpdateRequestDTO,
              UUID reviewId, CurrentUser user) {
        Store findStore = storeRepository.findById(reviewUpdateRequestDTO.getReviewCommonRequestDTO().getStoreId()).
                orElseThrow(()->{
                    log.warn("존재하지 않는 가게");
                    return new StoreException(StoreErrorCode.NOT_FOUND);
                });
        Review findReview = reviewRepository.findById(reviewId).orElseThrow(()->{
                log.warn("존재하지 않는 리뷰");
                return new ReviewException(ReviewErrorCode.NOT_FOUND);
        });
        if(user == null || !reviewRepository.existsByReviewIdAndUserId(reviewId, user.id())) {
            log.warn("리뷰 수정 권한 없음");
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_ACCESS);
        }
        log.info("리뷰 수정 권한 확인 성공");
        findReview.update(reviewUpdateRequestDTO);
        findReview.setStore(findStore);
        log.info("리뷰 수정 성공");
        return ReviewConverter.toReviewUpdateResponseDTO(findReview);
    }
}
