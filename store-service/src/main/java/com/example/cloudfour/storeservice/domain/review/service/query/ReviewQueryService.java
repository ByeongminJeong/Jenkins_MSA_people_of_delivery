package com.example.cloudfour.storeservice.domain.review.service.query;

import com.example.cloudfour.modulecommon.dto.CurrentUser;
import com.example.cloudfour.storeservice.domain.collection.document.ReviewDocument;
import com.example.cloudfour.storeservice.domain.collection.repository.query.ReviewSearchRepository;
import com.example.cloudfour.storeservice.domain.common.UserResponseDTO;
import com.example.cloudfour.storeservice.domain.review.converter.ReviewConverter;
import com.example.cloudfour.storeservice.domain.review.dto.ReviewResponseDTO;
import com.example.cloudfour.storeservice.domain.review.exception.ReviewErrorCode;
import com.example.cloudfour.storeservice.domain.review.exception.ReviewException;
import com.example.cloudfour.storeservice.domain.store.exception.StoreErrorCode;
import com.example.cloudfour.storeservice.domain.store.exception.StoreException;
import com.example.cloudfour.storeservice.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewQueryService {
    private final ReviewSearchRepository reviewRepository;
    private final StoreRepository storeRepository;
    private final RestTemplate rt;
    private static final LocalDateTime first_cursor = LocalDateTime.now().plusDays(1);
    private static final String BASE = "http://user-service/internal/users";

    public ReviewResponseDTO.ReviewDetailResponseDTO getReviewById(UUID reviewId, CurrentUser user) {
        if(user==null){
            log.warn("상세 리뷰 조회 접근 권한 없음");
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_ACCESS);
        }

        UserResponseDTO findUser =  rt.getForObject(BASE+"/{id}",UserResponseDTO.class,user.id());

        if(findUser == null){
            log.warn("상세 리뷰 조회 접근 권한 없음");
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_ACCESS);
        }

        log.info("상세 리뷰 조회 권한 확인 성공");

        ReviewDocument findReview = reviewRepository.findById(reviewId).orElseThrow(()->{
            log.warn("존재하지 않는 리뷰");
            return new ReviewException(ReviewErrorCode.NOT_FOUND);
        });
        log.info("상세 리뷰 조회 성공");
        return ReviewConverter.toReviewDetailResponseDTO(findReview,findReview.getUserName());
    }

    public ReviewResponseDTO.ReviewStoreListResponseDTO getReviewListByStore(UUID storeId, LocalDateTime cursor, Integer size, CurrentUser user) {
        storeRepository.findById(storeId).orElseThrow(()->{
            log.warn("존재하지 않는 가게");
            return new StoreException(StoreErrorCode.NOT_FOUND);
        });
        if(user==null){
            log.warn("가게 리뷰 목록 조회 접근 권한 없음");
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_ACCESS);
        }
        if(cursor==null){
            cursor = first_cursor;
        }
        log.info("가게 리뷰 목록 조회 권한 확인 성공");
        Pageable pageable = PageRequest.of(0,size);

        Slice<ReviewDocument> findReviews = reviewRepository.findAllByStoreId(storeId,cursor,pageable);
        if(findReviews.isEmpty()){
            log.info("가게 리뷰 데이터 없음");
            throw new ReviewException(ReviewErrorCode.NOT_FOUND);
        }
        List<ReviewDocument> reviews = findReviews.toList();
        List<ReviewResponseDTO.ReviewStoreResponseDTO> reviewStoreListResponseDTOS = reviews.stream().map(ReviewConverter::toReviewStoreResponseDTO).toList();
        LocalDateTime next_cursor = null;
        if(!findReviews.isEmpty() && findReviews.hasNext()) {
            next_cursor = reviews.getLast().getCreatedAt();
        }
        log.info("가게 리뷰 목록 조회 성공");
        return ReviewConverter.toReviewStoreListResponseDTO(reviewStoreListResponseDTOS,findReviews.hasNext(),next_cursor);
    }

    public ReviewResponseDTO.ReviewUserListResponseDTO getReviewListByUser(LocalDateTime cursor, Integer size, CurrentUser user) {
        if(user==null){
            log.warn("가게 리뷰 목록 조회 접근 권한 없음");
            throw new ReviewException(ReviewErrorCode.UNAUTHORIZED_ACCESS);
        }
        if(cursor==null){
            cursor = first_cursor;
        }
        log.info("사용자 리뷰 목록 조회 권한 확인 성공");
        Pageable pageable = PageRequest.of(0,size);
        Slice<ReviewDocument> findReviews = reviewRepository.findAllByUserId(user.id(),cursor,pageable);
        if(findReviews.isEmpty()){
            log.info("사용자 리뷰 데이터 없음");
            throw new ReviewException(ReviewErrorCode.NOT_FOUND);
        }
        List<ReviewDocument> reviews = findReviews.toList();
        List<ReviewResponseDTO.ReviewUserResponseDTO> reviewUserListResponseDTOS = reviews.stream().map(ReviewConverter::toReviewUserResponseDTO).toList();
        LocalDateTime next_cursor = null;
        if(!findReviews.isEmpty() && findReviews.hasNext()) {
            next_cursor = reviews.getLast().getCreatedAt();
        }
        return ReviewConverter.toReviewUserListResponseDTO(reviewUserListResponseDTOS,findReviews.hasNext(),next_cursor);
    }
}
