package com.example.cloudfour.cartservice.scheduler;

import com.example.cloudfour.cartservice.domain.order.repository.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CartServiceScheduler {
    private final OrderRepository orderRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOrder(){
        LocalDateTime threeDays = LocalDateTime.now().minusDays(3);
        orderRepository.deleteAllByCreatedAtBefore(threeDays);
        log.info("Soft Deleted된 Order 삭제 (3일)");
    }
}
