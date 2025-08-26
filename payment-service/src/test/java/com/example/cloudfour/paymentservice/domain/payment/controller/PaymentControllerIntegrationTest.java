package com.example.cloudfour.paymentservice.domain.payment.controller;

import com.example.cloudfour.modulecommon.dto.CurrentUser;
import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentRequestDTO;
import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentResponseDTO;
import com.example.cloudfour.paymentservice.domain.payment.enums.PaymentStatus;
import com.example.cloudfour.paymentservice.domain.payment.exception.PaymentErrorCode;
import com.example.cloudfour.paymentservice.domain.payment.exception.PaymentException;
import com.example.cloudfour.paymentservice.domain.payment.service.command.PaymentCommandService;
import com.example.cloudfour.paymentservice.domain.payment.service.query.PaymentQueryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentController 통합테스트")
class PaymentControllerIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentCommandService paymentCommandService;

    @Mock
    private PaymentQueryService paymentQueryService;

    @InjectMocks
    private PaymentController paymentController;

    private ObjectMapper objectMapper;
    private UUID userId;
    private String orderId;
    private String paymentKey;
    private Integer amount;
    private CurrentUser currentUser;
    private UUID paymentId;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID().toString();
        paymentKey = "toss_payment_key_" + UUID.randomUUID();
        amount = 15000;
        paymentId = UUID.randomUUID();

        currentUser = new CurrentUser(userId, "ROLE_USER");

        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(currentUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        this.mockMvc = MockMvcBuilders.standaloneSetup(paymentController)
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new com.example.cloudfour.modulecommon.apiPayLoad.exception.handler.GlobalExceptionHandler())
                .build();

        reset(paymentCommandService, paymentQueryService);
    }

    @Nested
    @DisplayName("결제 승인 API (POST /api/payments/confirm)")
    class ConfirmPaymentAPITests {

        @Test
        @DisplayName("정상적인 결제 승인 요청 성공")
        void confirmPayment_Success() throws Exception {
            // Given
            PaymentRequestDTO.PaymentConfirmRequestDTO request = PaymentRequestDTO.PaymentConfirmRequestDTO.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .build();

            PaymentResponseDTO.PaymentConfirmResponseDTO response = PaymentResponseDTO.PaymentConfirmResponseDTO.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .approvedAtStr(LocalDateTime.now().toString())
                    .build();

            when(paymentCommandService.confirmPayment(any(PaymentRequestDTO.PaymentConfirmRequestDTO.class), any(UUID.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(post("/api/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.paymentKey").value(paymentKey))
                    .andExpect(jsonPath("$.result.orderId").value(orderId))
                    .andExpect(jsonPath("$.result.amount").value(amount));

            verify(paymentCommandService).confirmPayment(any(PaymentRequestDTO.PaymentConfirmRequestDTO.class), any(UUID.class));
        }

        @Test
        @DisplayName("서비스에서 예외 발생 시 400 에러")
        void confirmPayment_ServiceException_Returns400() throws Exception {
            // Given
            PaymentRequestDTO.PaymentConfirmRequestDTO request = PaymentRequestDTO.PaymentConfirmRequestDTO.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .build();

            when(paymentCommandService.confirmPayment(any(PaymentRequestDTO.PaymentConfirmRequestDTO.class), any(UUID.class)))
                    .thenThrow(new PaymentException(PaymentErrorCode.PAYMENT_APPROVAL_FAILED));

            // When & Then
            mockMvc.perform(post("/api/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value(PaymentErrorCode.PAYMENT_APPROVAL_FAILED.getCode()));

            verify(paymentCommandService).confirmPayment(any(PaymentRequestDTO.PaymentConfirmRequestDTO.class), any(UUID.class));
        }

        @Test
        @DisplayName("필수 필드가 누락된 결제 승인 요청 시 400 에러")
        void confirmPayment_MissingRequiredFields_Returns400() throws Exception {
            // Given
            PaymentRequestDTO.PaymentConfirmRequestDTO request = PaymentRequestDTO.PaymentConfirmRequestDTO.builder()
                    .paymentKey(paymentKey)
                    // orderId와 amount 누락
                    .build();

            // When & Then
            mockMvc.perform(post("/api/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(paymentCommandService, never()).confirmPayment(any(), any());
        }

        @ParameterizedTest
        @DisplayName("빈 paymentKey로 요청 시 400 에러")
        @ValueSource(strings = {"", " ", "  "})
        void confirmPayment_EmptyPaymentKey_Returns400(String emptyKey) throws Exception {
            // Given
            PaymentRequestDTO.PaymentConfirmRequestDTO request = PaymentRequestDTO.PaymentConfirmRequestDTO.builder()
                    .paymentKey(emptyKey)
                    .orderId(orderId)
                    .amount(amount)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(paymentCommandService, never()).confirmPayment(any(), any());
        }

        @ParameterizedTest
        @DisplayName("잘못된 amount로 요청 시 400 에러")
        @ValueSource(ints = {-1000, 0, 999})
        void confirmPayment_InvalidAmount_Returns400(int invalidAmount) throws Exception {
            // Given
            PaymentRequestDTO.PaymentConfirmRequestDTO request = PaymentRequestDTO.PaymentConfirmRequestDTO.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(invalidAmount)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(paymentCommandService, never()).confirmPayment(any(), any());
        }

    }

    @Nested
    @DisplayName("결제 취소 API (PATCH /api/payments/{orderId}/cancel)")
    class CancelPaymentAPITests {

        @Test
        @DisplayName("정상적인 결제 취소 요청 성공")
        void cancelPayment_Success() throws Exception {
            // Given
            PaymentRequestDTO.PaymentCancelRequestDTO request = PaymentRequestDTO.PaymentCancelRequestDTO.builder()
                    .cancelReason("고객 요청")
                    .build();

            PaymentResponseDTO.PaymentCancelResponseDTO response = PaymentResponseDTO.PaymentCancelResponseDTO.builder()
                    .paymentId(paymentId)
                    .paymentStatus(PaymentStatus.CANCELED)
                    .cancelReason("고객 요청")
                    .canceledAt(LocalDateTime.now())
                    .build();

            when(paymentCommandService.cancelPayment(any(PaymentRequestDTO.PaymentCancelRequestDTO.class), any(UUID.class), any(UUID.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(patch("/api/payments/{orderId}/cancel", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.paymentStatus").value("CANCELED"))
                    .andExpect(jsonPath("$.result.cancelReason").value("고객 요청"))
                    .andExpect(jsonPath("$.result.canceledAt").exists());

            verify(paymentCommandService).cancelPayment(any(PaymentRequestDTO.PaymentCancelRequestDTO.class), any(UUID.class), any(UUID.class));
        }

        @Test
        @DisplayName("존재하지 않는 orderId로 취소 요청 시 404 에러")
        void cancelPayment_OrderNotFound_Returns404() throws Exception {
            // Given
            PaymentRequestDTO.PaymentCancelRequestDTO request = PaymentRequestDTO.PaymentCancelRequestDTO.builder()
                    .cancelReason("고객 요청")
                    .build();

            when(paymentCommandService.cancelPayment(any(PaymentRequestDTO.PaymentCancelRequestDTO.class), any(UUID.class), any(UUID.class)))
                    .thenThrow(new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

            // When & Then
            mockMvc.perform(patch("/api/payments/{orderId}/cancel", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value(PaymentErrorCode.PAYMENT_NOT_FOUND.getCode()));

            verify(paymentCommandService).cancelPayment(any(PaymentRequestDTO.PaymentCancelRequestDTO.class), any(UUID.class), any(UUID.class));
        }

        @Test
        @DisplayName("취소사유가 누락된 요청 시 400 에러")
        void cancelPayment_MissingCancelReason_Returns400() throws Exception {
            // Given
            PaymentRequestDTO.PaymentCancelRequestDTO request = PaymentRequestDTO.PaymentCancelRequestDTO.builder()
                    .build();

            // When & Then
            mockMvc.perform(patch("/api/payments/{orderId}/cancel", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(paymentCommandService, never()).cancelPayment(any(), any(), any());
        }

        @ParameterizedTest
        @DisplayName("빈 취소 사유로 요청 시 400 에러")
        @ValueSource(strings = {"", " ", "  "})
        void cancelPayment_EmptyCancelReason_Returns400(String emptyReason) throws Exception {
            // Given
            PaymentRequestDTO.PaymentCancelRequestDTO request = PaymentRequestDTO.PaymentCancelRequestDTO.builder()
                    .cancelReason(emptyReason)
                    .build();

            // When & Then
            mockMvc.perform(patch("/api/payments/{orderId}/cancel", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(paymentCommandService, never()).cancelPayment(any(), any(), any());
        }

        @Test
        @DisplayName("너무 짧은 취소 사유로 요청 시 400 에러")
        void cancelPayment_TooShortCancelReason_Returns400() throws Exception {
            // Given
            PaymentRequestDTO.PaymentCancelRequestDTO request = PaymentRequestDTO.PaymentCancelRequestDTO.builder()
                    .cancelReason("a") // 1글자 (최소 2글자 필요)
                    .build();

            // When & Then
            mockMvc.perform(patch("/api/payments/{orderId}/cancel", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verify(paymentCommandService, never()).cancelPayment(any(), any(), any());
        }

    }

    @Nested
    @DisplayName("결제 상세 조회 API (GET /api/payments/{orderId})")
    class GetPaymentAPITests {

        @Test
        @DisplayName("정상적인 결제 상세 조회 성공")
        void getPayment_Success() throws Exception {
            // Given
            PaymentResponseDTO.PaymentDetailResponseDTO response = PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                    .paymentId(paymentId)
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            when(paymentQueryService.getDetailPayment(any(UUID.class), any(UUID.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/payments/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.paymentKey").value(paymentKey))
                    .andExpect(jsonPath("$.result.orderId").value(orderId))
                    .andExpect(jsonPath("$.result.amount").value(amount));

            verify(paymentQueryService).getDetailPayment(any(UUID.class), any(UUID.class));
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 404 에러")
        void getPayment_NotFound_Returns404() throws Exception {
            // Given
            when(paymentQueryService.getDetailPayment(any(UUID.class), any(UUID.class)))
                    .thenThrow(new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

            // When & Then
            mockMvc.perform(get("/api/payments/{orderId}", orderId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value(PaymentErrorCode.PAYMENT_NOT_FOUND.getCode()));

            verify(paymentQueryService).getDetailPayment(any(UUID.class), any(UUID.class));
        }
    }

    @Nested
    @DisplayName("내 결제 이력 조회 API (GET /api/payments/me)")
    class GetUserPaymentsAPITests {

        @Test
        @DisplayName("정상적인 사용자 결제 이력 조회 성공")
        void getUserPayments_Success() throws Exception {
            // Given
            List<PaymentResponseDTO.PaymentDetailResponseDTO> payments = List.of(
                    PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                            .paymentId(paymentId)
                            .paymentKey(paymentKey)
                            .orderId(orderId)
                            .amount(amount)
                            .paymentMethod("CARD")
                            .paymentStatus(PaymentStatus.APPROVED)
                            .approvedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            PaymentResponseDTO.PaymentUserListResponseDTO response = PaymentResponseDTO.PaymentUserListResponseDTO.builder()
                    .paymentList(payments)
                    .totalCount(1)
                    .build();

            when(paymentQueryService.getUserListPayment(any(UUID.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/payments/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.paymentList").isArray())
                    .andExpect(jsonPath("$.result.totalCount").value(1));

            verify(paymentQueryService).getUserListPayment(any(UUID.class));
        }

        @Test
        @DisplayName("사용자의 결제 내역이 없을 때 빈 목록 반환")
        void getUserPayments_NoPayments_ReturnsEmptyList() throws Exception {
            // Given
            PaymentResponseDTO.PaymentUserListResponseDTO response = PaymentResponseDTO.PaymentUserListResponseDTO.builder()
                    .paymentList(List.of())
                    .totalCount(0)
                    .build();

            when(paymentQueryService.getUserListPayment(any(UUID.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/payments/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.paymentList").isArray())
                    .andExpect(jsonPath("$.result.totalCount").value(0));

            verify(paymentQueryService).getUserListPayment(any(UUID.class));
        }
    }

    @Nested
    @DisplayName("웹훅 API (POST /api/payments/webhook)")
    class WebhookAPITests {

        @Test
        @DisplayName("정상적인 웹훅 수신 성공")
        void receiveWebhook_Success() throws Exception {
            // Given
            String webhookPayload = "{\"paymentKey\":\"" + paymentKey + "\",\"status\":\"DONE\"}";

            doNothing().when(paymentCommandService).updateStatusFromWebhook(any(String.class));

            // When & Then
            mockMvc.perform(post("/api/payments/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(webhookPayload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true));

            verify(paymentCommandService).updateStatusFromWebhook(any(String.class));
        }

        @Test
        @DisplayName("웹훅 처리 중 서비스 예외 발생 시 500 에러")
        void receiveWebhook_ServiceException_Returns500() throws Exception {
            // Given
            String webhookPayload = "{\"paymentKey\":\"" + paymentKey + "\",\"status\":\"DONE\"}";

            doThrow(new PaymentException(PaymentErrorCode.WEBHOOK_PROCESSING_FAILED))
                    .when(paymentCommandService).updateStatusFromWebhook(any(String.class));

            // When & Then
            mockMvc.perform(post("/api/payments/webhook")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(webhookPayload))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.isSuccess").value(false))
                    .andExpect(jsonPath("$.code").value(PaymentErrorCode.WEBHOOK_PROCESSING_FAILED.getCode()));

            verify(paymentCommandService).updateStatusFromWebhook(any(String.class));
        }

        @Test
        @DisplayName("다양한 웹훅 상태에 대한 처리")
        void receiveWebhook_VariousStatuses_ProcessedCorrectly() throws Exception {
            // Given
            String[] statuses = {"DONE", "CANCELED", "FAILED"};
            
            doNothing().when(paymentCommandService).updateStatusFromWebhook(any(String.class));
            
            for (String status : statuses) {
                String webhookPayload = "{\"paymentKey\":\"" + paymentKey + "\",\"status\":\"" + status + "\"}";

                // When & Then
                mockMvc.perform(post("/api/payments/webhook")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(webhookPayload))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.isSuccess").value(true));
            }

            verify(paymentCommandService, times(statuses.length)).updateStatusFromWebhook(any(String.class));
        }
    }

    @Nested
    @DisplayName("API 공통 테스트")
    class CommonAPITests {

        @Test
        @DisplayName("특수 문자가 포함된 요청 처리")
        void handleSpecialCharacters_Success() throws Exception {
            // Given
            String specialContent = "특수문자!@#$%^&*()_+-=[]{}|;':\",./<>?";
            PaymentResponseDTO.PaymentDetailResponseDTO response = PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                    .paymentId(paymentId)
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            when(paymentQueryService.getDetailPayment(any(UUID.class), any(UUID.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/payments/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true));

            verify(paymentQueryService).getDetailPayment(any(UUID.class), any(UUID.class));
        }

        @Test
        @DisplayName("동시에 여러 결제 승인 요청 처리")
        void handleConcurrentPaymentConfirmations_Success() throws Exception {
            // Given
            PaymentRequestDTO.PaymentConfirmRequestDTO request = PaymentRequestDTO.PaymentConfirmRequestDTO.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .build();

            PaymentResponseDTO.PaymentConfirmResponseDTO response = PaymentResponseDTO.PaymentConfirmResponseDTO.builder()
                    .paymentKey(paymentKey)
                    .orderId(orderId)
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .approvedAtStr(LocalDateTime.now().toString())
                    .build();

            when(paymentCommandService.confirmPayment(any(PaymentRequestDTO.PaymentConfirmRequestDTO.class), any(UUID.class))).thenReturn(response);

            // When & Then
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/api/payments/confirm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.isSuccess").value(true));
            }

            verify(paymentCommandService, times(3)).confirmPayment(any(PaymentRequestDTO.PaymentConfirmRequestDTO.class), any(UUID.class));
        }

        @Test
        @DisplayName("매우 긴 URL 경로 처리")
        void handleVeryLongUrlPath_Success() throws Exception {
            // Given
            String validOrderId = UUID.randomUUID().toString();
            PaymentResponseDTO.PaymentDetailResponseDTO response = PaymentResponseDTO.PaymentDetailResponseDTO.builder()
                    .paymentId(paymentId)
                    .paymentKey(paymentKey)
                    .orderId(validOrderId)
                    .amount(amount)
                    .paymentMethod("CARD")
                    .paymentStatus(PaymentStatus.APPROVED)
                    .approvedAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();

            when(paymentQueryService.getDetailPayment(any(UUID.class), any(UUID.class))).thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/payments/{orderId}", validOrderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true));

            verify(paymentQueryService).getDetailPayment(any(UUID.class), any(UUID.class));
        }
    }
}
