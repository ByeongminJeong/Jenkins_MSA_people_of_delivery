package com.example.cloudfour.cartservice.domain.order.repository;

import com.example.cloudfour.cartservice.domain.order.entity.OrderItemOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderItemOptionRepository extends JpaRepository<OrderItemOption, UUID> {
}
