package com.example.cloudfour.cartservice.domain.cart.repository;

import com.example.cloudfour.cartservice.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    @Query("select c from Cart c where c.id = :cartId and c.user = :userId")
    Optional<Cart> findByIdAndUser(@Param("cartId") UUID cartId, @Param("userId") UUID userId);

    @Query("select c from Cart c left join fetch c.cartItems where c.id = :cartId and c.user = :userId")
    Optional<Cart> findByIdAndUserWithCartItems(@Param("cartId") UUID cartId, @Param("userId") UUID userId);

    @Query("select count(c) > 0 from Cart c where c.user = :userId and c.store = :storeId")
    boolean existsByUserAndStore(@Param("userId") UUID userId, @Param("storeId") UUID storeId);

    @Query("select count(c) > 0 from Cart c where c.id = :cartId and c.user = :userId")
    boolean existsByUserAndCart(@Param("userId") UUID userId, @Param("cartId") UUID cartId);

    @Query("select c from Cart c where c.user = :userId")
    List<Cart> findAllByUserId(@Param("userId") UUID userId);

    @Query("select c from Cart c where c.store = :storeId")
    List<Cart> findAllByStoreId(@Param("storeId") UUID storeId);
}