package com.example.cloudfour.paymentservice.domain.payment.service.query;

import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentResponseDTO;

import java.util.UUID;

public interface PaymentQueryService {
    PaymentResponseDTO.PaymentDetailResponseDTO getDetailPayment(UUID orderId, UUID userId);
    PaymentResponseDTO.PaymentUserListResponseDTO getUserListPayment(UUID userId);
}
