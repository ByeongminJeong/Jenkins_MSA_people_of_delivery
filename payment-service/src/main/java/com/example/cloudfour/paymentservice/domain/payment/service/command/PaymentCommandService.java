package com.example.cloudfour.paymentservice.domain.payment.service.command;

import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentRequestDTO;
import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentResponseDTO;

import java.util.UUID;

public interface PaymentCommandService {
    PaymentResponseDTO.PaymentConfirmResponseDTO confirmPayment(PaymentRequestDTO.PaymentConfirmRequestDTO request, UUID userId);
    PaymentResponseDTO.PaymentCancelResponseDTO cancelPayment(PaymentRequestDTO.PaymentCancelRequestDTO request, UUID orderId, UUID userId);
    void updateStatusFromWebhook(String payload);
}
