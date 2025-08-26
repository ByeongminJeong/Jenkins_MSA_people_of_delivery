package com.example.cloudfour.cartservice.domain.cartitem.repository;

import com.example.cloudfour.cartservice.domain.cartitem.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    @Query("select count(ci) > 0 from CartItem ci where ci.id = :cartItemId and ci.cart.user = :userId")
    boolean existsByCartItemAndUser(@Param("cartItemId") UUID cartItemId, @Param("userId") UUID userId);
    
    @Query("select ci from CartItem ci left join fetch ci.options where ci.cart.id = :cartId")
    List<CartItem> findAllByCartIdWithOptions(@Param("cartId") UUID cartId);
    
    @Query("select ci from CartItem ci left join fetch ci.options where ci.cart.id = :cartId and ci.menu = :menuId")
    List<CartItem> findByCartIdAndMenuId(@Param("cartId") UUID cartId, @Param("menuId") UUID menuId);
    
    @Query("select ci from CartItem ci left join fetch ci.options where ci.id = :cartItemId")
    Optional<CartItem> findByIdWithOptions(@Param("cartItemId") UUID cartItemId);
}
