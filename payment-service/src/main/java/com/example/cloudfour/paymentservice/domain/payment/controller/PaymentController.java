package com.example.cloudfour.paymentservice.domain.payment.controller;

import com.example.cloudfour.modulecommon.apiPayLoad.CustomResponse;
import com.example.cloudfour.modulecommon.dto.CurrentUser;
import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentRequestDTO;
import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentResponseDTO;
import com.example.cloudfour.paymentservice.domain.payment.service.command.PaymentCommandService;
import com.example.cloudfour.paymentservice.domain.payment.service.query.PaymentQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Tag(name="Payment", description = "토스페이먼츠 결제 관리 API")
@Validated
public class PaymentController {
    
    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    @PostMapping("/confirm")
    @Operation(summary = "결제 승인", description = "프론트엔드에서 받은 결제 정보를 승인합니다.")
    @PreAuthorize("hasRole('USER') and authentication.principal.id == #user.id()")
    public CustomResponse<PaymentResponseDTO.PaymentConfirmResponseDTO> confirmPayment(
            @Valid @RequestBody PaymentRequestDTO.PaymentConfirmRequestDTO request,
            @AuthenticationPrincipal CurrentUser user
    ){
        log.info("결제 승인 요청: paymentKey={}, orderId={}, userId={}", request.getPaymentKey(), request.getOrderId(), user.id());
        PaymentResponseDTO.PaymentConfirmResponseDTO response = paymentCommandService.confirmPayment(request, user.id());
        return CustomResponse.onSuccess(HttpStatus.OK, response);
    }

    @PostMapping("/webhook")
    @Operation(summary = "토스페이먼츠 웹훅", description = "토스페이먼츠 결제 상태 변경 웹훅을 처리합니다.")
    public CustomResponse<Void> receiveWebhook(@RequestBody String payload) {
        log.info("웹훅 수신: payload={}", payload);
        paymentCommandService.updateStatusFromWebhook(payload);
        return CustomResponse.onSuccess(HttpStatus.OK, null);
    }

    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "결제 취소", description = "결제를 취소합니다.")
    public CustomResponse<PaymentResponseDTO.PaymentCancelResponseDTO> cancelPayment(
            @Valid @RequestBody PaymentRequestDTO.PaymentCancelRequestDTO request,
            @PathVariable("orderId") UUID orderId,
            @AuthenticationPrincipal CurrentUser user
    ){
        log.info("결제 취소 요청: orderId={}, userId={}", orderId, user.id());
        PaymentResponseDTO.PaymentCancelResponseDTO response = paymentCommandService.cancelPayment(request, orderId, user.id());
        return CustomResponse.onSuccess(HttpStatus.OK, response);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "결제 상세 조회", description = "결제 상세 정보를 조회합니다.")
    @PreAuthorize("hasRole('USER') and authentication.principal.id == #user.id()")
    public CustomResponse<PaymentResponseDTO.PaymentDetailResponseDTO> getPayment(
            @PathVariable("orderId") UUID orderId,
            @AuthenticationPrincipal CurrentUser user
    ){
        log.info("결제 상세 조회 요청: orderId={}, userId={}", orderId, user.id());
        PaymentResponseDTO.PaymentDetailResponseDTO response = paymentQueryService.getDetailPayment(orderId, user.id());
        return CustomResponse.onSuccess(HttpStatus.OK, response);
    }

    @GetMapping("/me")
    @Operation(summary = "내 결제 이력", description = "내 결제 이력을 조회합니다.")
    @PreAuthorize("hasRole('USER') and authentication.principal.id == #user.id()")
    public CustomResponse<PaymentResponseDTO.PaymentUserListResponseDTO> getUserPayments(
            @AuthenticationPrincipal CurrentUser user
    ){
        log.info("내 결제 이력 조회 요청: userId={}", user.id());
        PaymentResponseDTO.PaymentUserListResponseDTO response = paymentQueryService.getUserListPayment(user.id());
        return CustomResponse.onSuccess(HttpStatus.OK, response);
    }
}
