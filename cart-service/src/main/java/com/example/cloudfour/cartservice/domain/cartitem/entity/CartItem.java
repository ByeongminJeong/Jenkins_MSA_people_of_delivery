package com.example.cloudfour.cartservice.domain.cartitem.entity;

import com.example.cloudfour.cartservice.domain.cart.entity.Cart;
import com.example.cloudfour.cartservice.domain.cartitem.exception.CartItemErrorCode;
import com.example.cloudfour.cartservice.domain.cartitem.exception.CartItemException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "p_cartitem")
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cartId", nullable = false)
    private Cart cart;

    @Column(name = "menuId", nullable = false)
    private UUID menu;

    @OneToMany(mappedBy = "cartItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItemOption> options = new ArrayList<>();

    public void update(Integer quantity, Integer price) {
        if (quantity != null && quantity > 0) {
            this.quantity = quantity;
        }
        if (price != null && price >= 0) {
            this.price = price;
        }
    }

    public void increaseQuantity(int increment) {
        if (increment > 0) {
            this.quantity += increment;
        }
    }

    public void decreaseQuantity(int decrement) {
        if (decrement > 0 && this.quantity > decrement) {
            this.quantity -= decrement;
        }
    }

    public void addOption(CartItemOption option) {
        if (option == null) {
            throw new CartItemException(CartItemErrorCode.INVALID_INPUT);
        }
        option.setCartItem(this);
        this.options.add(option);
    }

    public void removeOption(UUID optionId) {
        if (optionId == null) {
            throw new CartItemException(CartItemErrorCode.INVALID_INPUT);
        }
        this.options.removeIf(option -> option.getMenuOptionId().equals(optionId));
    }

    public void clearOptions() {
        this.options.clear();
    }

    public int getOptionCount() {
        return this.options.size();
    }

    public boolean hasOptions() {
        return !this.options.isEmpty();
    }

    public int getTotalPrice() {
        return this.quantity * this.price;
    }

    public void setCart(Cart cart) {
        if (cart == null) {
            throw new CartItemException(CartItemErrorCode.INVALID_INPUT);
        }
        this.cart = cart;
        cart.getCartItems().add(this);
    }

    public void setMenu(UUID menu) {
        if (menu == null) {
            throw new CartItemException(CartItemErrorCode.INVALID_INPUT);
        }
        this.menu = menu;
    }

    public static class CartItemBuilder {
        private CartItemBuilder id(UUID id) {
            throw new CartItemException(CartItemErrorCode.CREATE_FAILED);
        }
    }
}
