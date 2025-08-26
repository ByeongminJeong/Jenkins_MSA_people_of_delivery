package com.example.cloudfour.paymentservice.domain.payment.service.query;

import com.example.cloudfour.paymentservice.domain.payment.converter.PaymentConverter;
import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentResponseDTO;
import com.example.cloudfour.paymentservice.domain.payment.entity.Payment;
import com.example.cloudfour.paymentservice.domain.payment.enums.PaymentStatus;
import com.example.cloudfour.paymentservice.domain.payment.exception.PaymentErrorCode;
import com.example.cloudfour.paymentservice.domain.payment.exception.PaymentException;
import com.example.cloudfour.paymentservice.domain.payment.repository.PaymentRepository;
import com.example.cloudfour.paymentservice.domain.payment.apiclient.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentRepository paymentRepository;
    private final PaymentConverter paymentConverter;
    private final UserClient userClient;

    @Override
    public PaymentResponseDTO.PaymentDetailResponseDTO getDetailPayment(UUID orderId, UUID userId) {
        log.info("결제 상세 조회: orderId={}, userId={}", orderId, userId);
        
        if (orderId == null || userId == null) {
            throw new PaymentException(PaymentErrorCode.INVALID_INPUT);
        }

        if (!userClient.existsUser(userId)) {
            log.error("존재하지 않는 사용자: userId={}", userId);
            throw new PaymentException(PaymentErrorCode.USER_NOT_FOUND);
        }
        
        Payment payment = paymentRepository.findByOrderIdAndUserId(orderId, userId)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        
        return paymentConverter.toDetailResponse(payment);
    }

    @Override
    public PaymentResponseDTO.PaymentUserListResponseDTO getUserListPayment(UUID userId) {
        log.info("사용자 결제 목록 조회: userId={}", userId);
        
        if (userId == null) {
            throw new PaymentException(PaymentErrorCode.INVALID_INPUT);
        }

        if (!userClient.existsUser(userId)) {
            log.error("존재하지 않는 사용자: userId={}", userId);
            throw new PaymentException(PaymentErrorCode.USER_NOT_FOUND);
        }
        
        List<Payment> payments = paymentRepository.findAllByUserId(userId);
        
        if (payments == null) {
            throw new PaymentException(PaymentErrorCode.INTERNAL_SERVER_ERROR);
        }
        
        List<PaymentResponseDTO.PaymentDetailResponseDTO> paymentList = payments.stream()
                .map(paymentConverter::toDetailResponse)
                .collect(Collectors.toList());
        
        return PaymentResponseDTO.PaymentUserListResponseDTO.builder()
                .paymentList(paymentList)
                .totalCount(paymentList.size())
                .build();
    }
}
