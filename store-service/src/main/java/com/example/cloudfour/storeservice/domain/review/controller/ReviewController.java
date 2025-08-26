package com.example.cloudfour.storeservice.domain.review.controller;

import com.example.cloudfour.modulecommon.apiPayLoad.CustomResponse;
import com.example.cloudfour.modulecommon.dto.CurrentUser;
import com.example.cloudfour.storeservice.domain.review.dto.ReviewRequestDTO;
import com.example.cloudfour.storeservice.domain.review.dto.ReviewResponseDTO;
import com.example.cloudfour.storeservice.domain.review.service.command.ReviewCommandService;
import com.example.cloudfour.storeservice.domain.review.service.query.ReviewQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
@Tag(name = "Review", description = "리뷰 API by 김준형")
public class ReviewController {
    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "리뷰 생성", description = "리뷰를 생성합니다. 리뷰 생성에 사용되는 API입니다.")
    public CustomResponse<ReviewResponseDTO.ReviewCreateResponseDTO> createReview(
            @Valid @RequestBody ReviewRequestDTO.ReviewCreateRequestDTO reviewCreateRequestDTO,
        @AuthenticationPrincipal CurrentUser user
    ){
        ReviewResponseDTO.ReviewCreateResponseDTO review = reviewCommandService.createReview(reviewCreateRequestDTO,user);
        return CustomResponse.onSuccess(HttpStatus.CREATED, review);
    }

    @PatchMapping("/{reviewId}")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "리뷰 수정", description = "리뷰를 수정합니다. 리뷰 수정에 사용되는 API입니다.")
    public CustomResponse<ReviewResponseDTO.ReviewUpdateResponseDTO> updateReview(
            @Valid @RequestBody ReviewRequestDTO.ReviewUpdateRequestDTO reviewUpdateRequestDTO,
            @PathVariable("reviewId") UUID reviewId,
            @AuthenticationPrincipal CurrentUser user
    ){
        ReviewResponseDTO.ReviewUpdateResponseDTO review = reviewCommandService.updateReview(reviewUpdateRequestDTO,reviewId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, review);
    }

    @PatchMapping("/{reviewId}/canceled")
    @PreAuthorize("(hasRole('ROLE_USER') and authentication.principal.id == #user.id()) or hasRole('ROLE_MASTER')")
    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다. 리뷰 삭제에 사용되는 API입니다.")
    public CustomResponse<String> deleteReview(
            @PathVariable("reviewId") UUID reviewId,
            @AuthenticationPrincipal CurrentUser user
    ){
        reviewCommandService.deleteReview(reviewId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, "리뷰 삭제 완료");
    }

    @GetMapping("/{reviewId}/detail")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "리뷰 상세 조회", description = "리뷰를 상세 조회합니다. 리뷰 상세 조회에 사용되는 API입니다.")
    public CustomResponse<ReviewResponseDTO.ReviewDetailResponseDTO> getReview(
            @PathVariable("reviewId") UUID reviewId,
            @AuthenticationPrincipal CurrentUser user
    ){
        ReviewResponseDTO.ReviewDetailResponseDTO review = reviewQueryService.getReviewById(reviewId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, review);
    }

    @GetMapping("")
    @PreAuthorize("(hasRole('ROLE_USER') and authentication.principal.id == #user.id())")
    @Operation(summary = "유저 리뷰 조회", description = "사용자가 작성한 리뷰를 조회합니다. 사용자 리뷰 조회에 사용되는 API입니다.")
    @Parameter(name = "cursor", description = "데이터가 시작하는 부분을 표시합니다")
    @Parameter(name = "size", description = "size만큼 데이터를 가져옵니다.")
    public CustomResponse<ReviewResponseDTO.ReviewUserListResponseDTO> getUserReviews(
            @AuthenticationPrincipal CurrentUser user,
            @RequestParam(name = "cursor", required = false) LocalDateTime cursor,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ){
        ReviewResponseDTO.ReviewUserListResponseDTO review = reviewQueryService.getReviewListByUser(cursor,size,user);
        return CustomResponse.onSuccess(HttpStatus.OK, review);
    }

    @GetMapping("{storeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "가게 리뷰 조회", description = "가게에 있는 리뷰를 조회합니다. 가게 리뷰 조회에 사용되는 API입니다.")
    @Parameter(name = "cursor", description = "데이터가 시작하는 부분을 표시합니다")
    @Parameter(name = "size", description = "size만큼 데이터를 가져옵니다.")
    public CustomResponse<ReviewResponseDTO.ReviewStoreListResponseDTO> getStoreReviews(
            @PathVariable("storeId") UUID storeId,
            @AuthenticationPrincipal CurrentUser user,
            @RequestParam(name = "cursor", required = false) LocalDateTime cursor,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ){
        ReviewResponseDTO.ReviewStoreListResponseDTO review = reviewQueryService.getReviewListByStore(storeId,cursor,size,user);
        return CustomResponse.onSuccess(HttpStatus.OK, review);
    }
}
