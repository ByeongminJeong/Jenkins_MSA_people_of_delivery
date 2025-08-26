package com.example.cloudfour.paymentservice.domain.payment.service.query;

import com.example.cloudfour.paymentservice.domain.payment.converter.PaymentConverter;
import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentResponseDTO;
import com.example.cloudfour.paymentservice.domain.payment.entity.Payment;
import com.example.cloudfour.paymentservice.domain.payment.enums.PaymentStatus;
import com.example.cloudfour.paymentservice.domain.payment.exception.PaymentErrorCode;
import com.example.cloudfour.paymentservice.domain.payment.exception.PaymentException;
import com.example.cloudfour.paymentservice.domain.payment.repository.PaymentRepository;
import com.example.cloudfour.paymentservice.domain.payment.apiclient.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentQueryServiceImpl 단위테스트")
class PaymentQueryServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentConverter paymentConverter;
    
    @Mock
    private UserClient userClient;

    @InjectMocks
    private PaymentQueryServiceImpl paymentQueryService;

    private UUID userId;
    private UUID paymentId;
    private String orderId;
    private String paymentKey;
    private Integer amount;
    private Payment samplePayment;
    private List<Payment> paymentList;
    private PaymentResponseDTO.PaymentDetailResponseDTO detailResponse;
    private PaymentResponseDTO.PaymentUserListResponseDTO listResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        orderId = UUID.randomUUID().toString();
        paymentKey = "toss_payment_key_" + UUID.randomUUID();
        amount = 15000;

        samplePayment = Payment.builder()
                .paymentKey(paymentKey)
                .orderId(UUID.fromString(orderId))
                .userId(userId)
                .amount(amount)
                .paymentMethod("CARD")
                .paymentStatus(PaymentStatus.APPROVED)
                .approvedAt(LocalDateTime.now())
                .build();

        paymentList = List.of(samplePayment);

        detailResponse = PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                .paymentId(paymentId)
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .paymentMethod("CARD")
                .paymentStatus(PaymentStatus.APPROVED)
                .approvedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        listResponse = PaymentResponseDTO.PaymentUserListResponseDTO.builder()
                .paymentList(List.of(detailResponse))
                .totalCount(1)
                .build();
                
        // 기본 Mock 설정 (lenient 모드로 설정)
        lenient().when(userClient.existsUser(any(UUID.class))).thenReturn(true);
    }

    @Nested
    @DisplayName("결제 상세 조회 (getDetailPayment)")
    class GetDetailPaymentTests {

        @Test
        @DisplayName("정상적인 결제 상세 조회 성공")
        void getDetailPayment_Success() {
            // Given
            when(paymentRepository.findByOrderIdAndUserId(UUID.fromString(orderId), userId))
                    .thenReturn(Optional.of(samplePayment));
            when(paymentConverter.toDetailResponse(samplePayment)).thenReturn(detailResponse);

            // When
            PaymentResponseDTO.PaymentDetailResponseDTO response = paymentQueryService.getDetailPayment(UUID.fromString(orderId), userId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPaymentId()).isNotNull();
            assertThat(response.getPaymentKey()).isEqualTo(paymentKey);
            assertThat(response.getOrderId()).isEqualTo(orderId);
            assertThat(response.getAmount()).isEqualTo(amount);
            assertThat(response.getPaymentMethod()).isEqualTo("CARD");
            assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(response.getApprovedAt()).isNotNull();

            verify(paymentRepository).findByOrderIdAndUserId(UUID.fromString(orderId), userId);
            verify(paymentConverter).toDetailResponse(samplePayment);
        }

        @Test
        @DisplayName("다양한 결제 상태에 대한 상세 조회")
        void getDetailPayment_VariousPaymentStatuses_Success() {
            // Given
            PaymentStatus[] statuses = {PaymentStatus.APPROVED, PaymentStatus.CANCELED, PaymentStatus.FAILED};
            
            for (PaymentStatus status : statuses) {
                Payment payment = Payment.builder()
                        .paymentKey(paymentKey + "_" + status.name())
                        .orderId(UUID.fromString(orderId))
                        .userId(userId)
                        .amount(amount)
                        .paymentMethod("CARD")
                        .paymentStatus(status)
                        .approvedAt(status == PaymentStatus.APPROVED ? LocalDateTime.now() : null)
                        .canceledAt(status == PaymentStatus.CANCELED ? LocalDateTime.now() : null)
                        .build();

                PaymentResponseDTO.PaymentDetailResponseDTO response = PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                        .paymentId(UUID.randomUUID())
                        .paymentKey(paymentKey + "_" + status.name())
                        .orderId(orderId)
                        .amount(amount)
                        .paymentMethod("CARD")
                        .paymentStatus(status)
                        .approvedAt(status == PaymentStatus.APPROVED ? LocalDateTime.now() : null)
                        .createdAt(LocalDateTime.now())
                        .build();

                when(paymentRepository.findByOrderIdAndUserId(UUID.fromString(orderId), userId))
                        .thenReturn(Optional.of(payment));
                when(paymentConverter.toDetailResponse(payment)).thenReturn(response);

                // When
                PaymentResponseDTO.PaymentDetailResponseDTO result = paymentQueryService.getDetailPayment(UUID.fromString(orderId), userId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getPaymentStatus()).isEqualTo(status);
            }

            verify(paymentRepository, times(statuses.length)).findByOrderIdAndUserId(UUID.fromString(orderId), userId);
            verify(paymentConverter, times(statuses.length)).toDetailResponse(any(Payment.class));
        }

        @Test
        @DisplayName("다양한 결제 방법에 대한 상세 조회")
        void getDetailPayment_VariousPaymentMethods_Success() {
            // Given
            String[] methods = {"CARD", "BANK_TRANSFER", "VIRTUAL_ACCOUNT"};
            
            for (String method : methods) {
                Payment payment = Payment.builder()
                        .paymentKey(paymentKey + "_" + method)
                        .orderId(UUID.fromString(orderId))
                        .userId(userId)
                        .amount(amount)
                        .paymentMethod(method)
                        .paymentStatus(PaymentStatus.APPROVED)
                        .approvedAt(LocalDateTime.now())
                        .build();

                PaymentResponseDTO.PaymentDetailResponseDTO response = PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                        .paymentId(UUID.randomUUID())
                        .paymentKey(paymentKey + "_" + method)
                        .orderId(orderId)
                        .amount(amount)
                        .paymentMethod(method)
                        .paymentStatus(PaymentStatus.APPROVED)
                        .approvedAt(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .build();

                when(paymentRepository.findByOrderIdAndUserId(UUID.fromString(orderId), userId))
                        .thenReturn(Optional.of(payment));
                when(paymentConverter.toDetailResponse(payment)).thenReturn(response);

                // When
                PaymentResponseDTO.PaymentDetailResponseDTO result = paymentQueryService.getDetailPayment(UUID.fromString(orderId), userId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getPaymentMethod()).isEqualTo(method);
            }

            verify(paymentRepository, times(methods.length)).findByOrderIdAndUserId(UUID.fromString(orderId), userId);
            verify(paymentConverter, times(methods.length)).toDetailResponse(any(Payment.class));
        }

        @Test
        @DisplayName("금액이 0인 결제 조회")
        void getDetailPayment_ZeroAmount_Success() {
            // Given
            Payment payment = Payment.builder()
                    .paymentKey(paymentKey)
                    .orderId(UUID.fromString(orderId))
                    .userId(userId)
                    .amount(0)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .build();

            PaymentResponseDTO.PaymentDetailResponseDTO response = PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                    .paymentId(paymentId)
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(0)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            when(paymentRepository.findByOrderIdAndUserId(UUID.fromString(orderId), userId))
                    .thenReturn(Optional.of(payment));
            when(paymentConverter.toDetailResponse(payment)).thenReturn(response);

            // When
            PaymentResponseDTO.PaymentDetailResponseDTO result = paymentQueryService.getDetailPayment(UUID.fromString(orderId), userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(0);

            verify(paymentRepository).findByOrderIdAndUserId(UUID.fromString(orderId), userId);
            verify(paymentConverter).toDetailResponse(payment);
        }

        @Test
        @DisplayName("매우 큰 금액의 결제 조회")
        void getDetailPayment_LargeAmount_Success() {
            // Given
            Integer largeAmount = Integer.MAX_VALUE;
            Payment payment = Payment.builder()
                    .paymentKey(paymentKey)
                    .orderId(UUID.fromString(orderId))
                    .userId(userId)
                    .amount(largeAmount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .build();

            PaymentResponseDTO.PaymentDetailResponseDTO response = PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                    .paymentId(paymentId)
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(largeAmount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            when(paymentRepository.findByOrderIdAndUserId(UUID.fromString(orderId), userId))
                    .thenReturn(Optional.of(payment));
            when(paymentConverter.toDetailResponse(payment)).thenReturn(response);

            // When
            PaymentResponseDTO.PaymentDetailResponseDTO result = paymentQueryService.getDetailPayment(UUID.fromString(orderId), userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAmount()).isEqualTo(largeAmount);

            verify(paymentRepository).findByOrderIdAndUserId(UUID.fromString(orderId), userId);
            verify(paymentConverter).toDetailResponse(payment);
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 예외 발생")
        void getDetailPayment_NotFound_ThrowsException() {
            // Given
            when(paymentRepository.findByOrderIdAndUserId(UUID.fromString(orderId), userId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentQueryService.getDetailPayment(UUID.fromString(orderId), userId))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("code", PaymentErrorCode.PAYMENT_NOT_FOUND);

            verify(paymentRepository).findByOrderIdAndUserId(UUID.fromString(orderId), userId);
            verify(paymentConverter, never()).toDetailResponse(any());
        }

        @Test
        @DisplayName("null orderId로 조회 시 예외 발생")
        void getDetailPayment_NullOrderId_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> paymentQueryService.getDetailPayment(null, userId))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("code", PaymentErrorCode.INVALID_INPUT);

            verify(paymentRepository, never()).findByOrderIdAndUserId(any(), any());
            verify(paymentConverter, never()).toDetailResponse(any());
        }

        @Test
        @DisplayName("null userId로 조회 시 예외 발생")
        void getDetailPayment_NullUserId_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> paymentQueryService.getDetailPayment(UUID.fromString(orderId), null))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("code", PaymentErrorCode.INVALID_INPUT);

            verify(paymentRepository, never()).findByOrderIdAndUserId(any(), any());
            verify(paymentConverter, never()).toDetailResponse(any());
        }
    }

    @Nested
    @DisplayName("사용자 결제 목록 조회 (getUserListPayment)")
    class GetUserListPaymentTests {

        @Test
        @DisplayName("정상적인 사용자 결제목록 조회 성공")
        void getUserListPayment_Success() {
            // Given
            List<Payment> payments = List.of(
                    Payment.builder()
                            .paymentKey(paymentKey + "_1")
                            .orderId(UUID.fromString(orderId))
                            .userId(userId)
                            .amount(amount)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .build(),
                    Payment.builder()
                            .paymentKey(paymentKey + "_2")
                            .orderId(UUID.randomUUID())
                            .userId(userId)
                            .amount(amount * 2)
                            .paymentMethod("BANK_TRANSFER")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .build()
            );

            List<PaymentResponseDTO.PaymentDetailResponseDTO> paymentResponses = List.of(
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_1")
                            .orderId(orderId)
                            .amount(amount)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build(),
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_2")
                            .orderId(UUID.randomUUID().toString())
                            .amount(amount * 2)
                            .paymentMethod("BANK_TRANSFER")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            PaymentResponseDTO.PaymentUserListResponseDTO response = PaymentResponseDTO.PaymentUserListResponseDTO.builder()
                    .paymentList(paymentResponses)
                    .totalCount(2)
                    .build();

            when(paymentRepository.findAllByUserId(userId)).thenReturn(payments);
            when(paymentConverter.toDetailResponse(payments.get(0))).thenReturn(paymentResponses.get(0));
            when(paymentConverter.toDetailResponse(payments.get(1))).thenReturn(paymentResponses.get(1));

            // When
            PaymentResponseDTO.PaymentUserListResponseDTO result = paymentQueryService.getUserListPayment(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPaymentList()).hasSize(2);
            assertThat(result.getTotalCount()).isEqualTo(2);
            assertThat(result.getPaymentList().get(0).getPaymentKey()).isEqualTo(paymentKey + "_1");
            assertThat(result.getPaymentList().get(1).getPaymentKey()).isEqualTo(paymentKey + "_2");

            verify(paymentRepository).findAllByUserId(userId);
            verify(paymentConverter, times(2)).toDetailResponse(any(Payment.class));
        }

        @Test
        @DisplayName("사용자의 결제 내역이 없을 때 빈 목록 반환")
        void getUserListPayment_NoPayments_ReturnsEmptyList() {
            // Given
            when(paymentRepository.findAllByUserId(userId)).thenReturn(List.of());

            // When
            PaymentResponseDTO.PaymentUserListResponseDTO result = paymentQueryService.getUserListPayment(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPaymentList()).isEmpty();
            assertThat(result.getTotalCount()).isEqualTo(0);

            verify(paymentRepository).findAllByUserId(userId);
            verify(paymentConverter, never()).toDetailResponse(any());
        }

        @Test
        @DisplayName("여러 개의 결제 내역이 있을 때 모두 반환")
        void getUserListPayment_MultiplePayments_ReturnsAll() {
            // Given
            List<Payment> payments = List.of(
                    Payment.builder()
                            .paymentKey(paymentKey + "_1")
                            .orderId(UUID.fromString(orderId))
                            .userId(userId)
                            .amount(amount)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .build(),
                    Payment.builder()
                            .paymentKey(paymentKey + "_2")
                            .orderId(UUID.randomUUID())
                            .userId(userId)
                            .amount(amount * 2)
                            .paymentMethod("BANK_TRANSFER")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .build(),
                    Payment.builder()
                            .paymentKey(paymentKey + "_3")
                            .orderId(UUID.randomUUID())
                            .userId(userId)
                            .amount(amount * 3)
                            .paymentMethod("VIRTUAL_ACCOUNT")
                            .paymentStatus(PaymentStatus.CANCELED)
                            .canceledAt(LocalDateTime.now())
                            .build()
            );

            List<PaymentResponseDTO.PaymentDetailResponseDTO> paymentResponses = List.of(
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_1")
                            .orderId(orderId)
                            .amount(amount)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build(),
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_2")
                            .orderId(UUID.randomUUID().toString())
                            .amount(amount * 2)
                            .paymentMethod("BANK_TRANSFER")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build(),
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_3")
                            .orderId(UUID.randomUUID().toString())
                            .amount(amount * 3)
                            .paymentMethod("VIRTUAL_ACCOUNT")
                            .paymentStatus(PaymentStatus.CANCELED)
                            .canceledAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            PaymentResponseDTO.PaymentUserListResponseDTO response = PaymentResponseDTO.PaymentUserListResponseDTO.builder()
                    .paymentList(paymentResponses)
                    .totalCount(3)
                    .build();

            when(paymentRepository.findAllByUserId(userId)).thenReturn(payments);
            when(paymentConverter.toDetailResponse(payments.get(0))).thenReturn(paymentResponses.get(0));
            when(paymentConverter.toDetailResponse(payments.get(1))).thenReturn(paymentResponses.get(1));
            when(paymentConverter.toDetailResponse(payments.get(2))).thenReturn(paymentResponses.get(2));

            // When
            PaymentResponseDTO.PaymentUserListResponseDTO result = paymentQueryService.getUserListPayment(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPaymentList()).hasSize(3);
            assertThat(result.getTotalCount()).isEqualTo(3);

            verify(paymentRepository).findAllByUserId(userId);
            verify(paymentConverter, times(3)).toDetailResponse(any(Payment.class));
        }

        @Test
        @DisplayName("null userId로 조회 시 예외 발생")
        void getUserListPayment_NullUserId_ThrowsException() {
            // When & Then
            assertThatThrownBy(() -> paymentQueryService.getUserListPayment(null))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("code", PaymentErrorCode.INVALID_INPUT);

            verify(paymentRepository, never()).findAllByUserId(any());
            verify(paymentConverter, never()).toDetailResponse(any());
        }
    }

    @Nested
    @DisplayName("결제 통계 조회")
    class PaymentStatisticsTests {

        @Test
        @DisplayName("결제 방법별 통계")
        void getPaymentMethodStatistics_Success() {
            // Given
            List<Payment> payments = List.of(
                    Payment.builder()
                            .paymentKey(paymentKey + "_1")
                            .orderId(UUID.fromString(orderId))
                            .userId(userId)
                            .amount(amount)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .build(),
                    Payment.builder()
                            .paymentKey(paymentKey + "_2")
                            .orderId(UUID.randomUUID())
                            .userId(userId)
                            .amount(amount * 2)
                            .paymentMethod("BANK_TRANSFER")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .build(),
                    Payment.builder()
                            .paymentKey(paymentKey + "_3")
                            .orderId(UUID.randomUUID())
                            .userId(userId)
                            .amount(amount * 3)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .build()
            );

            List<PaymentResponseDTO.PaymentDetailResponseDTO> paymentResponses = List.of(
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_1")
                            .orderId(orderId)
                            .amount(amount)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build(),
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_2")
                            .orderId(UUID.randomUUID().toString())
                            .amount(amount * 2)
                            .paymentMethod("BANK_TRANSFER")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build(),
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_3")
                            .orderId(UUID.randomUUID().toString())
                            .amount(amount * 3)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            PaymentResponseDTO.PaymentUserListResponseDTO response = PaymentResponseDTO.PaymentUserListResponseDTO.builder()
                    .paymentList(paymentResponses)
                    .totalCount(3)
                    .build();

            when(paymentRepository.findAllByUserId(userId)).thenReturn(payments);
            when(paymentConverter.toDetailResponse(payments.get(0))).thenReturn(paymentResponses.get(0));
            when(paymentConverter.toDetailResponse(payments.get(1))).thenReturn(paymentResponses.get(1));
            when(paymentConverter.toDetailResponse(payments.get(2))).thenReturn(paymentResponses.get(2));

            // When
            PaymentResponseDTO.PaymentUserListResponseDTO result = paymentQueryService.getUserListPayment(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPaymentList()).hasSize(3);

            long cardCount = result.getPaymentList().stream()
                    .filter(p -> "CARD".equals(p.getPaymentMethod()))
                    .count();
            long bankTransferCount = result.getPaymentList().stream()
                    .filter(p -> "BANK_TRANSFER".equals(p.getPaymentMethod()))
                    .count();
            
            assertThat(cardCount).isEqualTo(2);
            assertThat(bankTransferCount).isEqualTo(1);

            verify(paymentRepository).findAllByUserId(userId);
            verify(paymentConverter, times(3)).toDetailResponse(any(Payment.class));
        }

        @Test
        @DisplayName("결제 상태별 통계")
        void getPaymentStatusStatistics_Success() {
            // Given
            List<Payment> payments = List.of(
                    Payment.builder()
                            .paymentKey(paymentKey + "_1")
                            .orderId(UUID.fromString(orderId))
                            .userId(userId)
                            .amount(amount)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .build(),
                    Payment.builder()
                            .paymentKey(paymentKey + "_2")
                            .orderId(UUID.randomUUID())
                            .userId(userId)
                            .amount(amount * 2)
                            .paymentMethod("BANK_TRANSFER")
                            .paymentStatus(PaymentStatus.CANCELED)
                            .canceledAt(LocalDateTime.now())
                            .build(),
                    Payment.builder()
                            .paymentKey(paymentKey + "_3")
                            .orderId(UUID.randomUUID())
                            .userId(userId)
                            .amount(amount * 3)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.FAILED)
                            .build()
            );

            List<PaymentResponseDTO.PaymentDetailResponseDTO> paymentResponses = List.of(
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_1")
                            .orderId(orderId)
                            .amount(amount)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build(),
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_2")
                            .orderId(UUID.randomUUID().toString())
                            .amount(amount * 2)
                            .paymentMethod("BANK_TRANSFER")
                            .paymentStatus(PaymentStatus.CANCELED)
                            .canceledAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build(),
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_3")
                            .orderId(UUID.randomUUID().toString())
                            .amount(amount * 3)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.FAILED)
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            PaymentResponseDTO.PaymentUserListResponseDTO response = PaymentResponseDTO.PaymentUserListResponseDTO.builder()
                    .paymentList(paymentResponses)
                    .totalCount(3)
                    .build();

            when(paymentRepository.findAllByUserId(userId)).thenReturn(payments);
            when(paymentConverter.toDetailResponse(payments.get(0))).thenReturn(paymentResponses.get(0));
            when(paymentConverter.toDetailResponse(payments.get(1))).thenReturn(paymentResponses.get(1));
            when(paymentConverter.toDetailResponse(payments.get(2))).thenReturn(paymentResponses.get(2));

            // When
            PaymentResponseDTO.PaymentUserListResponseDTO result = paymentQueryService.getUserListPayment(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPaymentList()).hasSize(3);

            long approvedCount = result.getPaymentList().stream()
                    .filter(p -> PaymentStatus.APPROVED.equals(p.getPaymentStatus()))
                    .count();
            long canceledCount = result.getPaymentList().stream()
                    .filter(p -> PaymentStatus.CANCELED.equals(p.getPaymentStatus()))
                    .count();
            long failedCount = result.getPaymentList().stream()
                    .filter(p -> PaymentStatus.FAILED.equals(p.getPaymentStatus()))
                    .count();
            
            assertThat(approvedCount).isEqualTo(1);
            assertThat(canceledCount).isEqualTo(1);
            assertThat(failedCount).isEqualTo(1);

            verify(paymentRepository).findAllByUserId(userId);
            verify(paymentConverter, times(3)).toDetailResponse(any(Payment.class));
        }

        @Test
        @DisplayName("사용자별 결제 총액 계산")
        void getUserTotalPaymentAmount_Success() {
            // Given
            List<Payment> payments = List.of(
                    Payment.builder()
                            .paymentKey(paymentKey + "_1")
                            .orderId(UUID.fromString(orderId))
                            .userId(userId)
                            .amount(amount)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .build(),
                    Payment.builder()
                            .paymentKey(paymentKey + "_2")
                            .orderId(UUID.randomUUID())
                            .userId(userId)
                            .amount(amount * 2)
                            .paymentMethod("BANK_TRANSFER")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .build(),
                    Payment.builder()
                            .paymentKey(paymentKey + "_3")
                            .orderId(UUID.randomUUID())
                            .userId(userId)
                            .amount(amount * 3)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .build()
            );

            List<PaymentResponseDTO.PaymentDetailResponseDTO> paymentResponses = List.of(
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_1")
                            .orderId(orderId)
                            .amount(amount)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build(),
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_2")
                            .orderId(UUID.randomUUID().toString())
                            .amount(amount * 2)
                            .paymentMethod("BANK_TRANSFER")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build(),
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(UUID.randomUUID())
                            .paymentKey(paymentKey + "_3")
                            .orderId(UUID.randomUUID().toString())
                            .amount(amount * 3)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            PaymentResponseDTO.PaymentUserListResponseDTO response = PaymentResponseDTO.PaymentUserListResponseDTO.builder()
                    .paymentList(paymentResponses)
                    .totalCount(3)
                    .build();

            when(paymentRepository.findAllByUserId(userId)).thenReturn(payments);
            when(paymentConverter.toDetailResponse(payments.get(0))).thenReturn(paymentResponses.get(0));
            when(paymentConverter.toDetailResponse(payments.get(1))).thenReturn(paymentResponses.get(1));
            when(paymentConverter.toDetailResponse(payments.get(2))).thenReturn(paymentResponses.get(2));

            // When
            PaymentResponseDTO.PaymentUserListResponseDTO result = paymentQueryService.getUserListPayment(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPaymentList()).hasSize(3);

            int totalAmount = result.getPaymentList().stream()
                    .mapToInt(PaymentResponseDTO.PaymentDetailResponseDTO::getAmount)
                    .sum();
            
            assertThat(totalAmount).isEqualTo(amount * 6);

            verify(paymentRepository).findAllByUserId(userId);
            verify(paymentConverter, times(3)).toDetailResponse(any(Payment.class));
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValueTests {

        @Test
        @DisplayName("최대값 UUID로 조회")
        void getDetailPayment_MaxUuid_Success() {
            // Given
            UUID maxUuid = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
            Payment payment = Payment.builder()
                    .paymentKey(paymentKey)
                    .orderId(maxUuid)
                    .userId(userId)
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .build();

            PaymentResponseDTO.PaymentDetailResponseDTO response = PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                    .paymentId(paymentId)
                    .paymentKey(paymentKey)
                    .orderId(maxUuid.toString())
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            when(paymentRepository.findByOrderIdAndUserId(maxUuid, userId))
                    .thenReturn(Optional.of(payment));
            when(paymentConverter.toDetailResponse(payment)).thenReturn(response);

            // When
            PaymentResponseDTO.PaymentDetailResponseDTO result = paymentQueryService.getDetailPayment(maxUuid, userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(maxUuid.toString());

            verify(paymentRepository).findByOrderIdAndUserId(maxUuid, userId);
            verify(paymentConverter).toDetailResponse(payment);
        }

        @Test
        @DisplayName("최소값 UUID로 조회")
        void getDetailPayment_MinUuid_Success() {
            // Given
            UUID minUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
            Payment payment = Payment.builder()
                    .paymentKey(paymentKey)
                    .orderId(minUuid)
                    .userId(userId)
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .build();

            PaymentResponseDTO.PaymentDetailResponseDTO response = PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                    .paymentId(paymentId)
                    .paymentKey(paymentKey)
                    .orderId(minUuid.toString())
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            when(paymentRepository.findByOrderIdAndUserId(minUuid, userId))
                    .thenReturn(Optional.of(payment));
            when(paymentConverter.toDetailResponse(payment)).thenReturn(response);

            // When
            PaymentResponseDTO.PaymentDetailResponseDTO result = paymentQueryService.getDetailPayment(minUuid, userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOrderId()).isEqualTo(minUuid.toString());

            verify(paymentRepository).findByOrderIdAndUserId(minUuid, userId);
            verify(paymentConverter).toDetailResponse(payment);
        }

        @Test
        @DisplayName("매우 긴 paymentKey로 조회")
        void getDetailPayment_VeryLongPaymentKey_Success() {
            // Given
            String veryLongPaymentKey = "a".repeat(1000);
            Payment payment = Payment.builder()
                    .paymentKey(veryLongPaymentKey)
                    .orderId(UUID.fromString(orderId))
                    .userId(userId)
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .build();

            PaymentResponseDTO.PaymentDetailResponseDTO response = PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                    .paymentId(paymentId)
                    .paymentKey(veryLongPaymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            when(paymentRepository.findByOrderIdAndUserId(UUID.fromString(orderId), userId))
                    .thenReturn(Optional.of(payment));
            when(paymentConverter.toDetailResponse(payment)).thenReturn(response);

            // When
            PaymentResponseDTO.PaymentDetailResponseDTO result = paymentQueryService.getDetailPayment(UUID.fromString(orderId), userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPaymentKey()).isEqualTo(veryLongPaymentKey);

            verify(paymentRepository).findByOrderIdAndUserId(UUID.fromString(orderId), userId);
            verify(paymentConverter).toDetailResponse(payment);
        }
    }
}
