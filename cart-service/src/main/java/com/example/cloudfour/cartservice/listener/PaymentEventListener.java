package com.example.cloudfour.cartservice.listener;

import com.example.cloudfour.cartservice.domain.order.service.command.OrderCommandService;
import com.example.cloudfour.cartservice.event.PaymentApprovedEvent;
import com.example.cloudfour.cartservice.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderCommandService orderCommandService;

    @KafkaListener(topics = "${kafka.topic.payment-events}", groupId = "${kafka.consumer.group-id}")
    public void handlePaymentApprovedEvent(
            @Payload PaymentApprovedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("PaymentApproved 이벤트 수신: eventId={}, orderId={}, paymentKey={}, topic={}, partition={}, offset={}",
            event.getEventId(), event.getOrderId(), event.getPaymentKey(), topic, partition, offset);

        try {
            orderCommandService.updateOrderStatusByPaymentEvent(event.getOrderId(), "주문완료");
            log.info("주문 상태 업데이트 완료: orderId={}, newStatus=주문완료", event.getOrderId());

            acknowledgment.acknowledge();
            log.info("PaymentApproved 이벤트 처리 완료 및 커밋: eventId={}, orderId={}",
                event.getEventId(), event.getOrderId());

        } catch (Exception e) {
            log.error("PaymentApproved 이벤트 처리 실패: eventId={}, orderId={}, error={}",
                event.getEventId(), event.getOrderId(), e.getMessage(), e);

            throw e;
        }
    }

    @KafkaListener(topics = "${kafka.topic.payment-events}", groupId = "${kafka.consumer.group-id}")
    public void handlePaymentFailedEvent(
            @Payload PaymentFailedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("PaymentFailed 이벤트 수신: eventId={}, orderId={}, failureReason={}, topic={}, partition={}, offset={}",
            event.getEventId(), event.getOrderId(), event.getFailureReason(), topic, partition, offset);

        try {
            log.info("결제 실패로 인한 재고 복구 시작: orderId={}, failureReason={}",
                event.getOrderId(), event.getFailureReason());

            orderCommandService.restoreStock(event.getOrderId());
            orderCommandService.updateOrderStatusByPaymentEvent(event.getOrderId(), "주문취소");

            log.info("결제 실패 처리 완료: orderId={}", event.getOrderId());

            acknowledgment.acknowledge();
            log.info("PaymentFailed 이벤트 처리 완료 및 커밋: eventId={}, orderId={}",
                event.getEventId(), event.getOrderId());

        } catch (Exception e) {
            log.error("PaymentFailed 이벤트 처리 실패: eventId={}, orderId={}, error={}",
                event.getEventId(), event.getOrderId(), e.getMessage(), e);

            throw e;
        }
    }
}