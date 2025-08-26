package com.example.cloudfour.paymentservice.domain.payment.service.command;

import com.example.cloudfour.paymentservice.domain.payment.converter.PaymentConverter;
import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentRequestDTO;
import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentResponseDTO;
import com.example.cloudfour.paymentservice.domain.payment.entity.Payment;
import com.example.cloudfour.paymentservice.domain.payment.entity.PaymentHistory;
import com.example.cloudfour.paymentservice.domain.payment.enums.PaymentStatus;
import com.example.cloudfour.paymentservice.domain.payment.exception.PaymentErrorCode;
import com.example.cloudfour.paymentservice.domain.payment.exception.PaymentException;
import com.example.cloudfour.paymentservice.domain.payment.repository.PaymentHistoryRepository;
import com.example.cloudfour.paymentservice.domain.payment.repository.PaymentRepository;
import com.example.cloudfour.paymentservice.domain.payment.service.IdempotencyService;
import com.example.cloudfour.paymentservice.domain.payment.service.WebhookSignatureService;
import com.example.cloudfour.paymentservice.domain.payment.service.EventPublishService;
import com.example.cloudfour.paymentservice.domain.payment.event.PaymentApprovedEvent;
import com.example.cloudfour.paymentservice.domain.payment.event.PaymentFailedEvent;

import com.example.cloudfour.paymentservice.domain.payment.apiclient.OrderClient;
import com.example.cloudfour.paymentservice.domain.payment.apiclient.TossApiClient;
import com.example.cloudfour.paymentservice.domain.payment.apiclient.UserClient;
import com.example.cloudfour.paymentservice.domain.payment.apiclient.StoreClient;
import com.example.cloudfour.paymentservice.domain.payment.dto.TossWebhookPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentCommandServiceImpl implements PaymentCommandService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final TossApiClient tossApiClient;
    private final IdempotencyService idempotencyService;
    private final WebhookSignatureService webhookSignatureService;
    private final PaymentConverter paymentConverter;
    private final ObjectMapper objectMapper;
    private final OrderClient orderClient;
    private final UserClient userClient;
    private final StoreClient storeClient;
    private final EventPublishService eventPublishService;

    @Override
    public PaymentResponseDTO.PaymentConfirmResponseDTO confirmPayment(PaymentRequestDTO.PaymentConfirmRequestDTO request, UUID userId) {
        log.info("결제 승인 시작: paymentKey={}, orderId={}, userId={}", request.getPaymentKey(), request.getOrderId(), userId);

        if (!userClient.existsUser(userId)) {
            log.error("존재하지 않는 사용자: userId={}", userId);
            throw new PaymentException(PaymentErrorCode.USER_NOT_FOUND);
        }

        com.example.cloudfour.paymentservice.commondto.OrderResponseDTO order = 
            orderClient.getOrderById(request.getOrderId(), userId);
        
        if (!storeClient.existsStore(order.getStoreId())) {
            log.error("존재하지 않는 가게: storeId={}", order.getStoreId());
            throw new PaymentException(PaymentErrorCode.STORE_NOT_FOUND);
        }

        validatePaymentOrderConsistency(request, order, userId);

        var existingPayment = idempotencyService.checkPaymentApprovalIdempotency(request.getPaymentKey(), request.getOrderId());
        if (existingPayment.isPresent()) {
            log.info("중복 결제 승인 요청 무시: paymentKey={}, orderId={}", request.getPaymentKey(), request.getOrderId());
            Payment payment = existingPayment.get();
            return paymentConverter.toConfirmResponse(payment);
        }

        String idempotencyKey = UUID.nameUUIDFromBytes(
            (request.getPaymentKey() + request.getOrderId()).getBytes()
        ).toString();

        try {
            TossApiClient.TossApproveResponse tossResponse = tossApiClient.approvePayment(
                request.getPaymentKey(),
                request.getOrderId(),
                request.getAmount(),
                idempotencyKey
            );

            Payment payment = Payment.builder()
                    .paymentKey(tossResponse.paymentKey)
                    .orderId(UUID.fromString(tossResponse.orderId))
                    .userId(userId)
                    .amount(tossResponse.totalAmount)
                    .paymentMethod(tossResponse.method)
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .rawResponse(objectMapper.writeValueAsString(tossResponse))
                    .build();

            idempotencyService.setPaymentApprovalIdempotency(payment);
            payment = paymentRepository.save(payment);

            PaymentHistory history = PaymentHistory.builder()
                    .payment(payment)
                    .previousStatus(null)
                    .currentStatus(PaymentStatus.APPROVED)
                    .changeReason("토스페이먼츠 결제 승인")
                    .rawResponse(objectMapper.writeValueAsString(tossResponse))
                    .build();

            idempotencyService.setPaymentCancelIdempotency(history);
            paymentHistoryRepository.save(history);

            try {
                orderClient.updateOrderStatus(request.getOrderId(), "주문완료");
                log.info("주문 상태 업데이트 완료: orderId={}, status=주문완료", request.getOrderId());
            } catch (Exception e) {
                log.error("주문 상태 업데이트 실패하지만 결제는 성공: orderId={}, error={}", request.getOrderId(), e.getMessage());
            }

            try {
                PaymentApprovedEvent paymentApprovedEvent = PaymentApprovedEvent.create(
                    payment.getOrderId(),
                    payment.getUserId(),
                    order.getStoreId(),
                    payment.getPaymentKey(),
                    BigDecimal.valueOf(payment.getAmount()),
                    payment.getPaymentMethod(),
                    payment.getApprovedAt()
                );
                
                eventPublishService.publishPaymentApprovedEvent(paymentApprovedEvent);
                log.info("PaymentApproved 이벤트 발행 완료: orderId={}, eventId={}", 
                    payment.getOrderId(), paymentApprovedEvent.getEventId());
                    
            } catch (Exception eventException) {
                log.error("PaymentApproved 이벤트 발행 실패하지만 결제는 성공: orderId={}, error={}", 
                    request.getOrderId(), eventException.getMessage());
            }

            log.info("결제 승인 완료: paymentId={}, paymentKey={}", payment.getId(), payment.getPaymentKey());
            return paymentConverter.toConfirmResponse(payment);

        } catch (Exception e) {
            log.error("결제 승인 실패: paymentKey={}, error={}", request.getPaymentKey(), e.getMessage());

            Payment failedPayment = Payment.builder()
                    .paymentKey(request.getPaymentKey())
                    .orderId(UUID.fromString(request.getOrderId()))
                    .userId(userId)
                    .amount(request.getAmount())
                    .paymentMethod("UNKNOWN")
                    .paymentStatus(PaymentStatus.FAILED)
                    .failedReason(e.getMessage())
                    .rawResponse("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();

            failedPayment = paymentRepository.save(failedPayment);

            PaymentHistory failedHistory = PaymentHistory.builder()
                    .payment(failedPayment)
                    .previousStatus(null)
                    .currentStatus(PaymentStatus.FAILED)
                    .changeReason("토스페이먼츠 결제 승인 실패: " + e.getMessage())
                    .rawResponse("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();

            paymentHistoryRepository.save(failedHistory);

            try {
                PaymentFailedEvent paymentFailedEvent = PaymentFailedEvent.create(
                    failedPayment.getOrderId(),
                    failedPayment.getUserId(),
                    order.getStoreId(), // 실제 주문의 storeId 사용
                    failedPayment.getPaymentKey(),
                    BigDecimal.valueOf(failedPayment.getAmount()),
                    failedPayment.getPaymentMethod(),
                    failedPayment.getFailedReason(),
                    LocalDateTime.now()
                );
                
                eventPublishService.publishPaymentFailedEvent(paymentFailedEvent);
                log.info("PaymentFailed 이벤트 발행 완료: orderId={}, eventId={}", 
                    failedPayment.getOrderId(), paymentFailedEvent.getEventId());
                    
            } catch (Exception eventException) {
                log.error("PaymentFailed 이벤트 발행 실패: orderId={}, error={}", 
                    failedPayment.getOrderId(), eventException.getMessage());
            }

            throw new PaymentException(PaymentErrorCode.PAYMENT_APPROVAL_FAILED);
        }
    }

    @Override
    public PaymentResponseDTO.PaymentCancelResponseDTO cancelPayment(PaymentRequestDTO.PaymentCancelRequestDTO request, UUID orderId, UUID userId) {
        log.info("결제 취소 시작: orderId={}, userId={}, reason={}", orderId, userId, request.getCancelReason());

        if (!userClient.existsUser(userId)) {
            log.error("존재하지 않는 사용자: userId={}", userId);
            throw new PaymentException(PaymentErrorCode.USER_NOT_FOUND);
        }

        Payment payment = paymentRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.canCancel()) {
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        var existingCancel = idempotencyService.checkPaymentCancelIdempotency(payment.getId(), request.getCancelReason());
        if (existingCancel.isPresent()) {
            log.info("중복 결제 취소 요청 무시: paymentId={}, reason={}", payment.getId(), request.getCancelReason());
            PaymentHistory history = existingCancel.get();
            return paymentConverter.toCancelResponse(payment, history);
        }

        try {
            tossApiClient.cancelPayment(payment.getPaymentKey(), request.getCancelReason());

            payment.cancel(request.getCancelReason(), LocalDateTime.now(), "{\"cancelReason\":\"" + request.getCancelReason() + "\"}");
            payment = paymentRepository.save(payment);

            PaymentHistory history = PaymentHistory.builder()
                    .payment(payment)
                    .previousStatus(PaymentStatus.APPROVED)
                    .currentStatus(PaymentStatus.CANCELED)
                    .changeReason(request.getCancelReason())
                    .rawResponse("{\"cancelReason\":\"" + request.getCancelReason() + "\"}")
                    .build();

            idempotencyService.setPaymentCancelIdempotency(history);
            history = paymentHistoryRepository.save(history);

            try {
                orderClient.updateOrderStatus(orderId.toString(), "주문취소");
                log.info("주문 상태 업데이트 완료: orderId={}, status=주문취소", orderId);
            } catch (Exception orderUpdateException) {
                log.error("주문 상태 업데이트 실패하지만 결제 취소는 성공: orderId={}, error={}", orderId, orderUpdateException.getMessage());
            }

            log.info("결제 취소 완료: paymentId={}, paymentKey={}", payment.getId(), payment.getPaymentKey());
            return paymentConverter.toCancelResponse(payment, history);

        } catch (Exception e) {
            log.error("결제 취소 실패: paymentId={}, error={}", payment.getId(), e.getMessage());
            throw new PaymentException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
        }
    }

    @Override
    public void updateStatusFromWebhook(String payload) {
        log.info("웹훅 수신: payload={}", payload);
        
        try {
            if (!webhookSignatureService.verifySignature(payload)) {
                log.error("웹훅 서명 검증 실패");
                throw new PaymentException(PaymentErrorCode.INVALID_WEBHOOK_SIGNATURE);
            }

            TossWebhookPayload webhookPayload = objectMapper.readValue(payload, TossWebhookPayload.class);

            var existingWebhook = idempotencyService.checkWebhookIdempotency(
                webhookPayload.getPaymentKey(), webhookPayload.getStatus());
            if (existingWebhook.isPresent()) {
                log.info("중복 웹훅 무시: paymentKey={}, status={}", 
                    webhookPayload.getPaymentKey(), webhookPayload.getStatus());
                return;
            }

            Payment payment = paymentRepository.findByPaymentKey(webhookPayload.getPaymentKey())
                    .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

            PaymentStatus newStatus = convertTossStatus(webhookPayload.getStatus());
            if (payment.getPaymentStatus() == newStatus) {
                log.info("웹훅 상태가 현재 상태와 동일: paymentKey={}, status={}", 
                    webhookPayload.getPaymentKey(), newStatus);
                return;
            }

            PaymentStatus previousStatus = payment.getPaymentStatus();

            switch (newStatus) {
                case APPROVED:
                    payment.approve(LocalDateTime.now(), payload);
                    break;
                case FAILED:
                    payment.fail(webhookPayload.getFailure() != null ? webhookPayload.getFailure().getMessage() : "웹훅으로 인한 실패", payload);
                    break;
                case CANCELED:
                    payment.cancel("웹훅으로 인한 취소", LocalDateTime.now(), payload);
                    break;
                default:
                    log.warn("알 수 없는 웹훅 상태: {}", newStatus);
                    return;
            }

            payment = paymentRepository.save(payment);

            PaymentHistory history = PaymentHistory.builder()
                    .payment(payment)
                    .previousStatus(previousStatus)
                    .currentStatus(newStatus)
                    .changeReason("토스페이먼츠 웹훅 상태 변경: " + webhookPayload.getStatus())
                    .rawResponse(payload)
                    .build();

            idempotencyService.setWebhookIdempotency(history);
            paymentHistoryRepository.save(history);

            updateOrderStatusBasedOnPayment(payment.getOrderId().toString(), newStatus, previousStatus);

            log.info("웹훅 상태 업데이트 완료: paymentKey={}, {} → {}", 
                webhookPayload.getPaymentKey(), previousStatus, newStatus);

        } catch (Exception e) {
            log.error("웹훅 처리 실패: error={}", e.getMessage());
            throw new PaymentException(PaymentErrorCode.WEBHOOK_PROCESSING_FAILED);
        }
    }


    private PaymentStatus convertTossStatus(String tossStatus) {
        return switch (tossStatus.toUpperCase()) {
            case "DONE", "APPROVED" -> PaymentStatus.APPROVED;
            case "CANCELED" -> PaymentStatus.CANCELED;
            case "FAILED" -> PaymentStatus.FAILED;
            default -> throw new PaymentException(PaymentErrorCode.UNKNOWN_TOSS_STATUS);
        };
    }

    private void updateOrderStatusBasedOnPayment(String orderId, PaymentStatus newPaymentStatus, PaymentStatus previousPaymentStatus) {
        try {
            String newOrderStatus = null;

            if (newPaymentStatus == previousPaymentStatus) {
                return;
            }
            
            switch (newPaymentStatus) {
                case APPROVED:
                    newOrderStatus = "주문완료";
                    break;
                case CANCELED:
                    newOrderStatus = "주문취소";
                    break;
                case FAILED:
                    newOrderStatus = "결제전";
                    break;
                default:
                    log.warn("알 수 없는 결제 상태로 주문 상태 업데이트 생략: paymentStatus={}", newPaymentStatus);
                    return;
            }
            
            orderClient.updateOrderStatus(orderId, newOrderStatus);
            log.info("결제 상태 변경에 따른 주문 상태 업데이트 완료: orderId={}, paymentStatus={} → {}, orderStatus={}", 
                orderId, previousPaymentStatus, newPaymentStatus, newOrderStatus);
                
        } catch (Exception e) {
            log.error("결제 상태 변경에 따른 주문 상태 업데이트 실패: orderId={}, paymentStatus={} → {}, error={}", 
                orderId, previousPaymentStatus, newPaymentStatus, e.getMessage());
        }
    }

    private void validatePaymentOrderConsistency(
            PaymentRequestDTO.PaymentConfirmRequestDTO request,
            com.example.cloudfour.paymentservice.commondto.OrderResponseDTO order,
            UUID userId) {

        if (!order.getUserId().equals(userId)) {
            log.error("주문 소유자 불일치: orderId={}, requestUserId={}, orderUserId={}", 
                request.getOrderId(), userId, order.getUserId());
            throw new PaymentException(PaymentErrorCode.UNAUTHORIZED_PAYMENT_ACCESS);
        }

        if (!order.getTotalPrice().equals(request.getAmount())) {
            log.error("주문 금액과 결제 금액 불일치: orderId={}, orderAmount={}, paymentAmount={}", 
                request.getOrderId(), order.getTotalPrice(), request.getAmount());
            throw new PaymentException(PaymentErrorCode.INVALID_INPUT);
        }

        if (!"결제전".equals(order.getStatus())) {
            log.error("결제 불가능한 주문 상태: orderId={}, status={}", 
                request.getOrderId(), order.getStatus());
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }
        
        log.info("결제-주문 정보 일치 검증 완료: orderId={}, amount={}, userId={}", 
            request.getOrderId(), request.getAmount(), userId);
    }
}
