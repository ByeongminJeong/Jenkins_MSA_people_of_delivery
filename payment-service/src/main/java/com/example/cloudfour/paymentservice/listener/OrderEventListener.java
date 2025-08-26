package com.example.cloudfour.paymentservice.listener;

import com.example.cloudfour.paymentservice.domain.payment.dto.PaymentRequestDTO;
import com.example.cloudfour.paymentservice.domain.payment.event.OrderCreatedEvent;
import com.example.cloudfour.paymentservice.domain.payment.service.command.PaymentCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final PaymentCommandService paymentCommandService;

    @KafkaListener(topics = "${kafka.topic.order-events}", groupId = "${kafka.consumer.group-id}")
    public void handleOrderCreatedEvent(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("OrderCreated 이벤트 수신: eventId={}, orderId={}, userId={}, topic={}, partition={}, offset={}", 
            event.getEventId(), event.getOrderId(), event.getUserId(), topic, partition, offset);

        try {
            PaymentRequestDTO.PaymentConfirmRequestDTO paymentRequest = 
                PaymentRequestDTO.PaymentConfirmRequestDTO.builder()
                    .paymentKey(generatePaymentKey(event.getOrderId()))
                    .orderId(event.getOrderId().toString())
                    .amount(event.getTotalAmount().intValue())
                    .build();

            paymentCommandService.confirmPayment(paymentRequest, event.getUserId());
            
            log.info("결제 프로세스 완료: orderId={}", event.getOrderId());

            acknowledgment.acknowledge();
            log.info("OrderCreated 이벤트 처리 완료 및 커밋: eventId={}, orderId={}", 
                event.getEventId(), event.getOrderId());

        } catch (Exception e) {
            log.error("OrderCreated 이벤트 처리 실패: eventId={}, orderId={}, error={}", 
                event.getEventId(), event.getOrderId(), e.getMessage(), e);
            
            throw e;
        }
    }

    private String generatePaymentKey(UUID orderId) {
        return "payment_" + orderId.toString().replace("-", "").substring(0, 20);
    }
}
