package com.example.cloudfour.paymentservice.domain.payment.service;

import com.example.cloudfour.paymentservice.domain.payment.entity.Payment;
import com.example.cloudfour.paymentservice.domain.payment.entity.PaymentHistory;
import com.example.cloudfour.paymentservice.domain.payment.repository.PaymentHistoryRepository;
import com.example.cloudfour.paymentservice.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    @Transactional(readOnly = true)
    public Optional<Payment> checkPaymentApprovalIdempotency(String paymentKey, String orderId) {
        String idempotencyKey = generateIdempotencyKey("approve", paymentKey, orderId);
        return paymentRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentHistory> checkPaymentCancelIdempotency(UUID paymentId, String reason) {
        String idempotencyKey = generateIdempotencyKey("cancel", paymentId.toString(), reason);
        return paymentHistoryRepository.findByIdempotencyKey(idempotencyKey);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentHistory> checkWebhookIdempotency(String paymentKey, String status) {
        String idempotencyKey = generateIdempotencyKey("webhook", paymentKey, status);
        return paymentHistoryRepository.findByIdempotencyKey(idempotencyKey);
    }

    private String generateIdempotencyKey(String operation, String... params) {
        StringBuilder sb = new StringBuilder(operation);
        for (String param : params) {
            sb.append(":").append(param);
        }
        return UUID.nameUUIDFromBytes(sb.toString().getBytes()).toString();
    }

    public String setPaymentApprovalIdempotency(Payment payment) {
        String idempotencyKey = generateIdempotencyKey("approve", payment.getPaymentKey(), payment.getOrderId().toString());
        payment.setIdempotencyKey(idempotencyKey);
        return idempotencyKey;
    }

    public String setPaymentCancelIdempotency(PaymentHistory history) {
        String idempotencyKey = generateIdempotencyKey("cancel", history.getPayment().getId().toString(), history.getChangeReason());
        history.setIdempotencyKey(idempotencyKey);
        return idempotencyKey;
    }

    public String setWebhookIdempotency(PaymentHistory history) {
        String idempotencyKey = generateIdempotencyKey("webhook", history.getPayment().getPaymentKey(), history.getCurrentStatus().toString());
        history.setIdempotencyKey(idempotencyKey);
        return idempotencyKey;
    }
}
