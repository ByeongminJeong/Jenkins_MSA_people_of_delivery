package com.example.cloudfour.cartservice.service;

import com.example.cloudfour.cartservice.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventPublishService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.order-events}")
    private String orderEventsTopic;

    public void publishOrderCreatedEvent(
            UUID orderId,
            UUID userId,
            UUID storeId,
            BigDecimal totalAmount,
            String orderStatus,
            String deliveryAddress) {

        OrderCreatedEvent event = OrderCreatedEvent.create(
            orderId, userId, storeId, totalAmount, orderStatus, deliveryAddress
        );

        publishOrderCreatedEvent(event);
    }

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            log.info("OrderCreated 이벤트 발행 시작: eventId={}, orderId={}, userId={}",
                event.getEventId(), event.getOrderId(), event.getUserId());

            String eventKey = event.getOrderId().toString();

            CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(orderEventsTopic, eventKey, event);

            future.whenComplete((result, throwable) -> {
                if (throwable == null) {
                    log.info("OrderCreated 이벤트 발행 성공: eventId={}, orderId={}, partition={}, offset={}",
                        event.getEventId(),
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("OrderCreated 이벤트 발행 실패: eventId={}, orderId={}, error={}",
                        event.getEventId(), event.getOrderId(), throwable.getMessage(), throwable);
                }
            });

        } catch (Exception e) {
            log.error("OrderCreated 이벤트 발행 중 예외 발생: eventId={}, orderId={}, error={}",
                event.getEventId(), event.getOrderId(), e.getMessage(), e);
            log.warn("이벤트 발행 실패했지만 주문 생성은 계속 진행: orderId={}", event.getOrderId());
        }
    }
}
