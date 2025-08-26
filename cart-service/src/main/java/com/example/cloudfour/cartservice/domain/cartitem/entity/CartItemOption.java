package com.example.cloudfour.cartservice.domain.cartitem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "p_cartitem_option")
public class CartItemOption {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cartitem_id", nullable = false)
    private CartItem cartItem;

    @Column(name = "menuoption_id", nullable = false, columnDefinition = "uuid")
    private UUID menuOptionId;

    @Column(name = "additional_price", nullable = false)
    private Integer additionalPrice;

    @Column(name = "option_name", nullable = false, length = 100)
    private String optionName;

    public void setCartItem(CartItem cartItem) {
        this.cartItem = cartItem;
    }
}

