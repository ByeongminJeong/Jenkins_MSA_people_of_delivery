package com.example.cloudfour.cartservice.domain.order.entity;

import com.example.cloudfour.cartservice.domain.order.exception.OrderItemErrorCode;
import com.example.cloudfour.cartservice.domain.order.exception.OrderItemException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
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
@Table(name = "p_orderItem")
public class OrderItem {
    @Id
    @GeneratedValue
    private UUID id;

    private Integer quantity;

    private Integer price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    private Order order;

    @Column(name = "menuId", nullable = false)
    private UUID menu;

    @Column(name = "menuOptionId")
    private UUID menuOption;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemOption> options = new ArrayList<>();

    public static class OrderItemBuilder{
        private OrderItemBuilder id(UUID id){
            throw new OrderItemException(OrderItemErrorCode.CREATE_FAILED);
        }
    }

    public void setOrder(Order order){
        this.order = order;
        order.getOrderItems().add(this);
    }

    public void setMenu(UUID menu){
        this.menu = menu;
    }

    public void setMenuOption(UUID menuOption){
        if (menuOption != null) {
            this.menuOption = menuOption;
        }
    }

    public void addOption(OrderItemOption option) {
        option.setOrderItem(this);
        this.options.add(option);
    }

    public void addOptions(List<OrderItemOption> options) {
        if (options != null) {
            options.forEach(this::addOption);
        }
    }
}
