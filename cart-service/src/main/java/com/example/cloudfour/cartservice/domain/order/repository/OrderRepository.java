package com.example.cloudfour.cartservice.domain.order.repository;

import com.example.cloudfour.cartservice.domain.order.entity.Order;
import com.example.cloudfour.cartservice.domain.order.enums.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("select o from Order o where o.id = :orderId and o.isDeleted = false")
    Optional<Order> findById(@Param("orderId") UUID orderId);

    @Query("select o from Order o where o.user = :userId and o.userIsDeleted = false and o.isDeleted = false and o.createdAt < :cursor order by o.createdAt desc")
    Slice<Order> findAllByUserId(@Param("userId") UUID userId, @Param("cursor") LocalDateTime cursor, Pageable pageable);

    @Query("select o from Order o where o.store = :storeId and o.userIsDeleted = false and o.isDeleted = false and o.createdAt < :cursor order by o.createdAt desc")
    Slice<Order> findAllByStoreId(@Param("storeId") UUID storeId, @Param("cursor") LocalDateTime cursor, Pageable pageable);

    @Query("select count(o) > 0 from Order o where o.id = :orderId and o.user = :userId and o.userIsDeleted = false and o.isDeleted = false")
    boolean existsByOrderIdAndUserId(@Param("orderId") UUID orderId, @Param("userId") UUID userId);

    void deleteAllByCreatedAtBefore(LocalDateTime createdAtBefore);

    @Query("select count(o) from Order o where o.status = :status and o.isDeleted = false")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("select o from Order o where o.user = :userId and o.status = :status and o.userIsDeleted = false and o.isDeleted = false order by o.createdAt desc")
    List<Order> findAllByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") OrderStatus status);

    @Query("select o from Order o where o.store = :storeId and o.status = :status and o.userIsDeleted = false and o.isDeleted = false order by o.createdAt desc")
    List<Order> findAllByStoreIdAndStatus(@Param("storeId") UUID storeId, @Param("status") OrderStatus status);
}
