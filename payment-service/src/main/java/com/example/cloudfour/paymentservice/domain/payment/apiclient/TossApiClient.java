package com.example.cloudfour.paymentservice.domain.payment.apiclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossApiClient {

    private final RestTemplate restTemplate;
    
    @Value("${toss.secret-key}")
    private String secretKey;

    private static final String BASE = "https://api.tosspayments.com";

    public TossApproveResponse approvePayment(String paymentKey, String orderId, Integer amount, String idempotencyKey) {
        String url = BASE + "/v1/payments/" + paymentKey;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((secretKey + ":").getBytes()));
        headers.set("Idempotency-Key", idempotencyKey);
        
        Map<String, Object> requestBody = Map.of(
            "orderId", orderId,
            "amount", amount
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            log.info("토스 결제 승인 요청: paymentKey={}, orderId={}, amount={}", paymentKey, orderId, amount);
            TossApproveResponse response = restTemplate.postForObject(url, request, TossApproveResponse.class);
            log.info("토스 결제 승인 성공: paymentKey={}", paymentKey);
            return response;
        } catch (Exception e) {
            log.error("토스 결제 승인 실패: paymentKey={}, error={}", paymentKey, e.getMessage());
            throw e;
        }
    }


    public void cancelPayment(String paymentKey, String cancelReason) {
        String url = BASE + "/v1/payments/" + paymentKey + "/cancel";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Basic " + java.util.Base64.getEncoder().encodeToString((secretKey + ":").getBytes()));
        
        Map<String, Object> requestBody = Map.of(
            "cancelReason", cancelReason
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            log.info("토스 결제 취소 요청: paymentKey={}, reason={}", paymentKey, cancelReason);
            restTemplate.postForObject(url, request, Object.class);
            log.info("토스 결제 취소 성공: paymentKey={}", paymentKey);
        } catch (Exception e) {
            log.error("토스 결제 취소 실패: paymentKey={}, error={}", paymentKey, e.getMessage());
            throw e;
        }
    }

    public static class TossApproveResponse {
        public String paymentKey;
        public String orderId;
        public Integer totalAmount;
        public String method;
        public String status;
        public String approvedAt;
    }
}
