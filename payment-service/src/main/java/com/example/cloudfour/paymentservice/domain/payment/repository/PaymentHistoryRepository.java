package com.example.cloudfour.paymentservice.domain.payment.repository;

import com.example.cloudfour.paymentservice.domain.payment.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, UUID> {

    List<PaymentHistory> findAllByPaymentIdOrderByCreatedAtDesc(UUID paymentId);
    
    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.idempotencyKey = :idempotencyKey")
    Optional<PaymentHistory> findByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
    
    @Query("SELECT ph FROM PaymentHistory ph WHERE ph.payment.id = :paymentId AND ph.currentStatus = :status")
    List<PaymentHistory> findByPaymentIdAndStatus(@Param("paymentId") UUID paymentId, @Param("status") String status);
}
