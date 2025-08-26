package com.example.cloudfour.paymentservice.domain.payment.apiclient;

import com.example.cloudfour.paymentservice.commondto.OrderResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderClient {

    private final RestTemplate restTemplate;

    private static final String BASE = "http://cart-service/internal";

    public OrderResponseDTO getOrderById(String orderId, UUID userId) {
        try {
            String url = BASE + "/orders/" + orderId + "?userId=" + userId;
            log.info("주문 정보 조회 요청: url={}", url);
            
            OrderResponseDTO order = restTemplate.getForObject(url, OrderResponseDTO.class);
            log.info("주문 정보 조회 성공: orderId={}, userId={}", orderId, userId);
            
            return order;
        } catch (Exception e) {
            log.error("주문 정보 조회 실패: orderId={}, userId={}, error={}", orderId, userId, e.getMessage());
            throw new RuntimeException("주문 정보 조회에 실패했습니다: " + e.getMessage());
        }
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        try {
            String url = BASE + "/orders/" + orderId + "/status";
            log.info("주문 상태 업데이트 요청: url={}, newStatus={}", url, newStatus);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = String.format("{\"newStatus\":\"%s\"}", newStatus);
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            
            restTemplate.exchange(url, HttpMethod.PATCH, requestEntity, Void.class);
            log.info("주문 상태 업데이트 성공: orderId={}, newStatus={}", orderId, newStatus);
            
        } catch (Exception e) {
            log.error("주문 상태 업데이트 실패: orderId={}, newStatus={}, error={}", orderId, newStatus, e.getMessage());
            log.warn("주문 상태 업데이트 실패했지만 결제 처리는 계속 진행합니다.");
        }
    }
}

