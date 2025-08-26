package com.example.cloudfour.paymentservice.domain.payment.service;

import com.example.cloudfour.paymentservice.domain.payment.event.PaymentApprovedEvent;
import com.example.cloudfour.paymentservice.domain.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublishService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.payment-events}")
    private String paymentEventsTopic;

    public void publishPaymentApprovedEvent(PaymentApprovedEvent event) {
        try {
            log.info("PaymentApproved 이벤트 발행 시작: eventId={}, orderId={}, paymentKey={}", 
                event.getEventId(), event.getOrderId(), event.getPaymentKey());

            String eventKey = event.getOrderId().toString();

            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(paymentEventsTopic, eventKey, event);

            future.whenComplete((result, throwable) -> {
                if (throwable == null) {
                    log.info("PaymentApproved 이벤트 발행 성공: eventId={}, orderId={}, partition={}, offset={}", 
                        event.getEventId(), 
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("PaymentApproved 이벤트 발행 실패: eventId={}, orderId={}, error={}", 
                        event.getEventId(), event.getOrderId(), throwable.getMessage(), throwable);
                }
            });

        } catch (Exception e) {
            log.error("PaymentApproved 이벤트 발행 중 예외 발생: eventId={}, orderId={}, error={}", 
                event.getEventId(), event.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("PaymentApproved 이벤트 발행 실패", e);
        }
    }

    public void publishPaymentFailedEvent(PaymentFailedEvent event) {
        try {
            log.info("PaymentFailed 이벤트 발행 시작: eventId={}, orderId={}, failureReason={}", 
                event.getEventId(), event.getOrderId(), event.getFailureReason());

            String eventKey = event.getOrderId().toString();

            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(paymentEventsTopic, eventKey, event);

            future.whenComplete((result, throwable) -> {
                if (throwable == null) {
                    log.info("PaymentFailed 이벤트 발행 성공: eventId={}, orderId={}, partition={}, offset={}", 
                        event.getEventId(), 
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                } else {
                    log.error("PaymentFailed 이벤트 발행 실패: eventId={}, orderId={}, error={}", 
                        event.getEventId(), event.getOrderId(), throwable.getMessage(), throwable);
                }
            });

        } catch (Exception e) {
            log.error("PaymentFailed 이벤트 발행 중 예외 발생: eventId={}, orderId={}, error={}", 
                event.getEventId(), event.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("PaymentFailed 이벤트 발행 실패", e);
        }
    }
}
