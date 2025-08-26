package com.example.cloudfour.paymentservice.domain.payment.service.command;

import com.example.cloudfour.paymentservice.commondto.OrderResponseDTO;
import com.example.cloudfour.paymentservice.domain.payment.apiclient.OrderClient;
import com.example.cloudfour.paymentservice.domain.payment.apiclient.TossApiClient;
import com.example.cloudfour.paymentservice.domain.payment.apiclient.UserClient;
import com.example.cloudfour.paymentservice.domain.payment.apiclient.StoreClient;
import com.example.cloudfour.paymentservice.domain.payment.converter.PaymentConverter;
import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentRequestDTO;
import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentResponseDTO;
import com.example.cloudfour.paymentservice.domain.payment.entity.Payment;
import com.example.cloudfour.paymentservice.domain.payment.entity.PaymentHistory;
import com.example.cloudfour.paymentservice.domain.payment.enums.PaymentStatus;
import com.example.cloudfour.paymentservice.domain.payment.exception.PaymentErrorCode;
import com.example.cloudfour.paymentservice.domain.payment.exception.PaymentException;
import com.example.cloudfour.paymentservice.domain.payment.repository.PaymentRepository;
import com.example.cloudfour.paymentservice.domain.payment.repository.PaymentHistoryRepository;
import com.example.cloudfour.paymentservice.domain.payment.service.IdempotencyService;
import com.example.cloudfour.paymentservice.domain.payment.service.WebhookSignatureService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import com.example.cloudfour.paymentservice.domain.payment.dto.TossWebhookPayload;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentCommandServiceImpl 단위테스트")
class PaymentCommandServiceImplTest {

    @Mock
    private TossApiClient tossApiClient;
    
    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
    private PaymentHistoryRepository paymentHistoryRepository;
    
    @Mock
    private IdempotencyService idempotencyService;
    
    @Mock
    private WebhookSignatureService webhookSignatureService;
    
    @Mock
    private PaymentConverter paymentConverter;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private OrderClient orderClient;
    
    @Mock
    private UserClient userClient;
    
    @Mock
    private StoreClient storeClient;

    @InjectMocks
    private PaymentCommandServiceImpl paymentCommandService;

    private UUID userId;
    private String orderId;
    private String paymentKey;
    private Integer amount;
    private TossApiClient.TossApproveResponse tossResponse;
    private PaymentRequestDTO.PaymentConfirmRequestDTO confirmRequest;
    private PaymentRequestDTO.PaymentCancelRequestDTO cancelRequest;
    private Payment samplePayment;
    private PaymentResponseDTO.PaymentConfirmResponseDTO confirmResponse;
    private PaymentResponseDTO.PaymentCancelResponseDTO cancelResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID().toString();
        paymentKey = "toss_payment_key_" + UUID.randomUUID();
        amount = 15000;
        
        tossResponse = new TossApiClient.TossApproveResponse();
        tossResponse.paymentKey = paymentKey;
        tossResponse.orderId = orderId;
        tossResponse.totalAmount = amount;
        tossResponse.method = "CARD";
        tossResponse.status = "DONE";
        tossResponse.approvedAt = LocalDateTime.now().toString();
        
        confirmRequest = PaymentRequestDTO.PaymentConfirmRequestDTO.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .build();
        
        cancelRequest = PaymentRequestDTO.PaymentCancelRequestDTO.builder()
                .cancelReason("고객 요청")
                .build();

        samplePayment = Payment.builder()
                .paymentKey(paymentKey)
                .orderId(UUID.fromString(orderId))
                .userId(userId)
                .amount(amount)
                .paymentMethod("CARD")
                .paymentStatus(PaymentStatus.APPROVED)
                .approvedAt(LocalDateTime.now())
                .build();

        confirmResponse = PaymentResponseDTO.PaymentConfirmResponseDTO.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .paymentMethod("CARD")
                .paymentStatus(PaymentStatus.APPROVED)
                .approvedAt(LocalDateTime.now())
                .approvedAtStr(LocalDateTime.now().toString())
                .build();

        cancelResponse = PaymentResponseDTO.PaymentCancelResponseDTO.builder()
                .paymentId(UUID.randomUUID())
                .paymentStatus(PaymentStatus.CANCELED)
                .cancelReason("고객 요청")
                .canceledAt(LocalDateTime.now())
                .build();

        lenient().when(userClient.existsUser(any(UUID.class))).thenReturn(true);
        lenient().when(storeClient.existsStore(any(UUID.class))).thenReturn(true);

        OrderResponseDTO orderResponse = OrderResponseDTO.builder()
                .id(UUID.fromString(orderId))
                .userId(userId)
                .storeId(UUID.randomUUID())
                .totalPrice(amount)
                .status("결제전")
                .build();
        lenient().when(orderClient.getOrderById(anyString(), any(UUID.class))).thenReturn(orderResponse);
    }

    @Nested
    @DisplayName("결제 승인 (confirmPayment)")
    class ConfirmPaymentTests {
        
        @Test
        @DisplayName("정상적인 결제 승인 성공")
        void confirmPayment_Success() throws Exception {
            // Given
            when(idempotencyService.checkPaymentApprovalIdempotency(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(tossApiClient.approvePayment(anyString(), anyString(), any(), anyString()))
                    .thenReturn(tossResponse);
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"test\":\"data\"}");
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                return payment;
            });
            when(paymentHistoryRepository.save(any(PaymentHistory.class))).thenReturn(PaymentHistory.builder().build());
            when(paymentConverter.toConfirmResponse(any(Payment.class))).thenReturn(confirmResponse);

            // When
            PaymentResponseDTO.PaymentConfirmResponseDTO response = paymentCommandService.confirmPayment(confirmRequest, userId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPaymentKey()).isEqualTo(paymentKey);
            assertThat(response.getOrderId()).isEqualTo(orderId);
            assertThat(response.getAmount()).isEqualTo(amount);
            assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);

            verify(idempotencyService).checkPaymentApprovalIdempotency(paymentKey, orderId);
            verify(tossApiClient).approvePayment(eq(paymentKey), eq(orderId), eq(amount), anyString());
            verify(paymentRepository).save(any(Payment.class));
            verify(paymentHistoryRepository).save(any(PaymentHistory.class));
            verify(paymentConverter).toConfirmResponse(any(Payment.class));
        }

        @Test
        @DisplayName("중복 결제 승인 요청 시 기존 결제 정보 반환")
        void confirmPayment_DuplicateRequest_ReturnsExistingPayment() {
            // Given
            when(idempotencyService.checkPaymentApprovalIdempotency(anyString(), anyString()))
                    .thenReturn(Optional.of(samplePayment));
            when(paymentConverter.toConfirmResponse(any(Payment.class))).thenReturn(confirmResponse);

            // When
            PaymentResponseDTO.PaymentConfirmResponseDTO response = paymentCommandService.confirmPayment(confirmRequest, userId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPaymentKey()).isEqualTo(paymentKey);
            assertThat(response.getOrderId()).isEqualTo(orderId);
            assertThat(response.getAmount()).isEqualTo(amount);
            assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.APPROVED);

            verify(tossApiClient, never()).approvePayment(anyString(), anyString(), any(), anyString());
        }

        @Test
        @DisplayName("토스 API 호출 실패 시 예외 발생")
        void confirmPayment_TossApiCallFailed_ThrowsException() {
            // Given
            when(idempotencyService.checkPaymentApprovalIdempotency(paymentKey, orderId))
                    .thenReturn(Optional.empty());
            when(tossApiClient.approvePayment(anyString(), anyString(), anyInt(), anyString()))
                    .thenThrow(new RuntimeException("토스 API 호출 실패"));

            // When & Then
            assertThatThrownBy(() -> paymentCommandService.confirmPayment(confirmRequest, userId))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("code", PaymentErrorCode.PAYMENT_APPROVAL_FAILED);

            verify(idempotencyService).checkPaymentApprovalIdempotency(paymentKey, orderId);
            verify(tossApiClient).approvePayment(anyString(), anyString(), anyInt(), anyString());

            verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
            verify(paymentHistoryRepository, atLeastOnce()).save(any(PaymentHistory.class));
        }

        @Test
        @DisplayName("ObjectMapper 직렬화 실패 시 예외 발생")
        void confirmPayment_ObjectMapperSerializationFailed_ThrowsException() throws Exception {
            // Given
            when(idempotencyService.checkPaymentApprovalIdempotency(paymentKey, orderId))
                    .thenReturn(Optional.empty());
            when(tossApiClient.approvePayment(anyString(), anyString(), anyInt(), anyString()))
                    .thenReturn(tossResponse);
            when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("직렬화 실패"));

            // When & Then
            assertThatThrownBy(() -> paymentCommandService.confirmPayment(confirmRequest, userId))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("code", PaymentErrorCode.PAYMENT_APPROVAL_FAILED);

            verify(idempotencyService).checkPaymentApprovalIdempotency(paymentKey, orderId);
            verify(tossApiClient).approvePayment(anyString(), anyString(), anyInt(), anyString());

            verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
            verify(paymentHistoryRepository, atLeastOnce()).save(any(PaymentHistory.class));
        }

        @Test
        @DisplayName("잘못된 amount로 요청 시 예외 발생")
        void confirmPayment_InvalidAmount_ThrowsException() {
            // Given
            int invalidAmount = -1000;
            PaymentRequestDTO.PaymentConfirmRequestDTO invalidRequest = PaymentRequestDTO.PaymentConfirmRequestDTO.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(invalidAmount)
                    .build();

            // When & Then - 주문 정보와 결제 정보 불일치로 인한 예외 발생
            assertThatThrownBy(() -> paymentCommandService.confirmPayment(invalidRequest, userId))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("code", PaymentErrorCode.INVALID_INPUT);

            // 검증 단계에서 예외가 발생하므로 이후 로직은 실행되지 않음
            verify(idempotencyService, never()).checkPaymentApprovalIdempotency(anyString(), anyString());
            verify(tossApiClient, never()).approvePayment(anyString(), anyString(), anyInt(), anyString());
        }

        @Test
        @DisplayName("amount가 0인 경우 예외 발생")
        void confirmPayment_ZeroAmount_ThrowsException() {
            // Given
            int invalidAmount = 0;
            PaymentRequestDTO.PaymentConfirmRequestDTO invalidRequest = PaymentRequestDTO.PaymentConfirmRequestDTO.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(invalidAmount)
                    .build();

            // When & Then - 주문 정보와 결제 정보 불일치로 인한 예외 발생
            assertThatThrownBy(() -> paymentCommandService.confirmPayment(invalidRequest, userId))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("code", PaymentErrorCode.INVALID_INPUT);

            // 검증 단계에서 예외가 발생하므로 이후 로직은 실행되지 않음
            verify(idempotencyService, never()).checkPaymentApprovalIdempotency(anyString(), anyString());
            verify(tossApiClient, never()).approvePayment(anyString(), anyString(), anyInt(), anyString());
        }

        @Test
        @DisplayName("amount가 999인 경우 예외 발생")
        void confirmPayment_SmallAmount_ThrowsException() {
            // Given
            int invalidAmount = 999;
            PaymentRequestDTO.PaymentConfirmRequestDTO invalidRequest = PaymentRequestDTO.PaymentConfirmRequestDTO.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(invalidAmount)
                    .build();

            // When & Then - 주문 정보와 결제 정보 불일치로 인한 예외 발생
            assertThatThrownBy(() -> paymentCommandService.confirmPayment(invalidRequest, userId))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("code", PaymentErrorCode.INVALID_INPUT);

            // 검증 단계에서 예외가 발생하므로 이후 로직은 실행되지 않음
            verify(idempotencyService, never()).checkPaymentApprovalIdempotency(anyString(), anyString());
            verify(tossApiClient, never()).approvePayment(anyString(), anyString(), anyInt(), anyString());
        }
    }

    @Nested
    @DisplayName("결제 취소 (cancelPayment)")
    class CancelPaymentTests {
        
        @Test
        @DisplayName("정상적인 결제 취소 성공")
        void cancelPayment_Success() throws Exception {
            // Given
            when(paymentRepository.findByOrderIdAndUserId(any(), eq(userId)))
                    .thenReturn(Optional.of(samplePayment));
            when(idempotencyService.checkPaymentCancelIdempotency(any(), anyString()))
                    .thenReturn(Optional.empty());
            doNothing().when(tossApiClient).cancelPayment(anyString(), anyString());
            when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
                Payment payment = invocation.getArgument(0);
                return payment;
            });
            when(paymentHistoryRepository.save(any(PaymentHistory.class))).thenReturn(PaymentHistory.builder().build());
            when(paymentConverter.toCancelResponse(any(Payment.class), any(PaymentHistory.class))).thenReturn(cancelResponse);

            // When
            PaymentResponseDTO.PaymentCancelResponseDTO response = paymentCommandService.cancelPayment(cancelRequest, UUID.fromString(orderId), userId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELED);
            assertThat(response.getCancelReason()).isEqualTo("고객 요청");
            assertThat(response.getCanceledAt()).isNotNull();

            verify(paymentRepository).findByOrderIdAndUserId(UUID.fromString(orderId), userId);
            verify(idempotencyService).checkPaymentCancelIdempotency(samplePayment.getId(), "고객 요청");
            verify(tossApiClient).cancelPayment(eq(paymentKey), eq("고객 요청"));
            verify(paymentRepository).save(any(Payment.class));
            verify(paymentHistoryRepository).save(any(PaymentHistory.class));
            verify(paymentConverter).toCancelResponse(any(Payment.class), any(PaymentHistory.class));
        }

        @Test
        @DisplayName("중복 결제 취소 요청 시 기존 취소 정보 반환")
        void cancelPayment_DuplicateRequest_ReturnsExistingCancellation() {
            // Given
            PaymentHistory existingHistory = PaymentHistory.builder().build();
            when(paymentRepository.findByOrderIdAndUserId(any(), eq(userId)))
                    .thenReturn(Optional.of(samplePayment));
            when(idempotencyService.checkPaymentCancelIdempotency(any(), anyString()))
                    .thenReturn(Optional.of(existingHistory));
            when(paymentConverter.toCancelResponse(any(Payment.class), any(PaymentHistory.class))).thenReturn(cancelResponse);

            // When
            PaymentResponseDTO.PaymentCancelResponseDTO response = paymentCommandService.cancelPayment(cancelRequest, UUID.fromString(orderId), userId);

            // Then
            assertThat(response).isNotNull();

            verify(tossApiClient, never()).cancelPayment(anyString(), anyString());
        }

        @Test
        @DisplayName("존재하지 않는 결제 취소 시 예외 발생")
        void cancelPayment_PaymentNotFound_ThrowsException() {
            // Given
            when(paymentRepository.findByOrderIdAndUserId(UUID.fromString(orderId), userId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> paymentCommandService.cancelPayment(cancelRequest, UUID.fromString(orderId), userId))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("code", PaymentErrorCode.PAYMENT_NOT_FOUND);

            verify(paymentRepository).findByOrderIdAndUserId(UUID.fromString(orderId), userId);
            verify(idempotencyService, never()).checkPaymentCancelIdempotency(any(), any());
            verify(tossApiClient, never()).cancelPayment(anyString(), anyString());
        }

        @Test
        @DisplayName("이미 취소된 결제 취소 시 예외 발생")
        void cancelPayment_AlreadyCanceled_ThrowsException() {
            // Given
            Payment canceledPayment = Payment.builder()
                    .paymentKey(paymentKey)
                    .orderId(UUID.fromString(orderId))
                    .userId(userId)
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.CANCELED)
                    .canceledAt(LocalDateTime.now())
                    .build();

            when(paymentRepository.findByOrderIdAndUserId(UUID.fromString(orderId), userId))
                    .thenReturn(Optional.of(canceledPayment));

            // When & Then
            assertThatThrownBy(() -> paymentCommandService.cancelPayment(cancelRequest, UUID.fromString(orderId), userId))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("code", PaymentErrorCode.INVALID_PAYMENT_STATUS);

            verify(paymentRepository).findByOrderIdAndUserId(UUID.fromString(orderId), userId);
            verify(idempotencyService, never()).checkPaymentCancelIdempotency(any(), any());
            verify(tossApiClient, never()).cancelPayment(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("웹훅 처리 (updateStatusFromWebhook)")
    class WebhookTests {
        
        @Test
        @DisplayName("정상적인 웹훅 처리 성공")
        void updateStatusFromWebhook_Success() throws Exception {
            // Given
            String testOrderId = UUID.randomUUID().toString();
            String webhookPayload = String.format("{\"paymentKey\":\"test_key\",\"orderId\":\"%s\",\"status\":\"DONE\"}", testOrderId);
            TossWebhookPayload parsedPayload = TossWebhookPayload.builder()
                    .paymentKey("test_key")
                    .orderId(testOrderId)
                    .status("DONE")
                    .build();

            when(webhookSignatureService.verifySignature(webhookPayload)).thenReturn(true);
            when(objectMapper.readValue(webhookPayload, TossWebhookPayload.class))
                    .thenReturn(parsedPayload);
            when(idempotencyService.checkWebhookIdempotency(anyString(), anyString()))
                    .thenReturn(Optional.empty());
            Payment existingPayment = Payment.builder()
                    .paymentKey("test_key")
                    .orderId(UUID.fromString(testOrderId))
                    .userId(userId)
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.READY)
                    .build();
                    
            when(paymentRepository.findByPaymentKey("test_key"))
                    .thenReturn(Optional.of(existingPayment));
            when(paymentRepository.save(any(Payment.class)))
                    .thenReturn(existingPayment);

            // When
            paymentCommandService.updateStatusFromWebhook(webhookPayload);

            // Then
            verify(webhookSignatureService).verifySignature(webhookPayload);
            verify(objectMapper).readValue(webhookPayload, TossWebhookPayload.class);
            verify(paymentRepository).findByPaymentKey("test_key");
        }

        @Test
        @DisplayName("웹훅 페이로드 파싱 실패 시 예외 발생")
        void updateStatusFromWebhook_ParsingFailed_ThrowsException() throws Exception {
            // Given
            String invalidPayload = "invalid json";
            when(webhookSignatureService.verifySignature(invalidPayload)).thenReturn(true);
            when(objectMapper.readValue(invalidPayload, TossWebhookPayload.class))
                    .thenThrow(new RuntimeException("JSON 파싱 실패"));

            // When & Then
            assertThatThrownBy(() -> paymentCommandService.updateStatusFromWebhook(invalidPayload))
                    .isInstanceOf(PaymentException.class)
                    .hasFieldOrPropertyWithValue("code", PaymentErrorCode.WEBHOOK_PROCESSING_FAILED);

            verify(webhookSignatureService).verifySignature(invalidPayload);
            verify(objectMapper).readValue(invalidPayload, TossWebhookPayload.class);
            verify(paymentRepository, never()).findByPaymentKey(anyString());
        }
    }
}
