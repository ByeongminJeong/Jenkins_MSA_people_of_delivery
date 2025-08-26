package com.example.cloudfour.cartservice.domain.cart.entity;

import com.example.cloudfour.cartservice.domain.cart.exception.CartErrorCode;
import com.example.cloudfour.cartservice.domain.cart.exception.CartException;
import com.example.cloudfour.cartservice.domain.cartitem.entity.CartItem;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "p_cart")
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "userId", nullable = false)
    private UUID user;

    @Column(nullable = false)
    @Builder.Default
    private boolean userIsDeleted = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean storeIsDeleted = false;

    @Column(name = "storeId", nullable = false)
    private UUID store;

    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    public void addCartItem(CartItem cartItem) {
        if (cartItem == null) {
            throw new CartException(CartErrorCode.INVALID_INPUT);
        }
        cartItem.setCart(this);
    }

    public void removeCartItem(UUID cartItemId) {
        if (cartItemId == null) {
            throw new CartException(CartErrorCode.INVALID_INPUT);
        }
        this.cartItems.removeIf(item -> item.getId().equals(cartItemId));
    }

    public void clearCart() {
        this.cartItems.clear();
    }

    public int getItemCount() {
        return this.cartItems.size();
    }

    public boolean isEmpty() {
        return this.cartItems.isEmpty();
    }

    public void setUser(UUID user) {
        if (user == null) {
            throw new CartException(CartErrorCode.INVALID_INPUT);
        }
        this.user = user;
    }

    public void setStore(UUID store) {
        if (store == null) {
            throw new CartException(CartErrorCode.INVALID_INPUT);
        }
        this.store = store;
    }

    public void setUserIsDeleted() {
        this.userIsDeleted = true;
    }

    public void setStoreIsDeleted() {
        this.storeIsDeleted = true;
    }

    public void setCartItems(List<CartItem> cartItems) {
        if (cartItems == null) {
            throw new CartException(CartErrorCode.INVALID_INPUT);
        }
        this.cartItems.clear();
        this.cartItems.addAll(cartItems);
    }

    public static class CartBuilder {
        private CartBuilder id(UUID id) {
            throw new CartException(CartErrorCode.CREATE_FAILED);
        }
    }
}
