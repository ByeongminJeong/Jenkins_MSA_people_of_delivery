package com.example.cloudfour.cartservice.domain.order.converter;

import com.example.cloudfour.cartservice.domain.cartitem.entity.CartItem;
import com.example.cloudfour.cartservice.domain.cartitem.entity.CartItemOption;
import com.example.cloudfour.cartservice.domain.order.dto.OrderItemResponseDTO;
import com.example.cloudfour.cartservice.domain.order.entity.Order;
import com.example.cloudfour.cartservice.domain.order.entity.OrderItem;
import com.example.cloudfour.cartservice.domain.order.entity.OrderItemOption;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class OrderItemConverter {

    private OrderItemConverter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    public static OrderItemResponseDTO.OrderItemListResponseDTO toOrderItemClassListDTO(OrderItem orderItem) {
        if (orderItem == null) {
            throw new IllegalArgumentException("OrderItem cannot be null");
        }

        List<OrderItemResponseDTO.OrderItemOptionDTO> options = convertOptionsToDto(orderItem.getOptions());

        return OrderItemResponseDTO.OrderItemListResponseDTO.builder()
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .menuId(orderItem.getMenu())
                .options(options)
                .build();
    }

    public static OrderItem CartItemtoOrderItem(CartItem cartItem, Order order) {
        if (cartItem == null) {
            throw new IllegalArgumentException("CartItem cannot be null");
        }
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        OrderItem orderItem = OrderItem.builder()
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .build();
        
        orderItem.setMenu(cartItem.getMenu());
        orderItem.setOrder(order);

        List<OrderItemOption> orderItemOptions = convertCartItemOptionsToOrderItemOptions(cartItem.getOptions());
        if (!orderItemOptions.isEmpty()) {
            orderItem.addOptions(orderItemOptions);
        }
        
        return orderItem;
    }

    private static List<OrderItemResponseDTO.OrderItemOptionDTO> convertOptionsToDto(List<OrderItemOption> options) {
        if (options == null || options.isEmpty()) {
            return Collections.emptyList();
        }

        return options.stream()
                .map(OrderItemConverter::convertOptionToDto)
                .collect(Collectors.toList());
    }

    private static OrderItemResponseDTO.OrderItemOptionDTO convertOptionToDto(OrderItemOption option) {
        if (option == null) {
            return null;
        }

        return OrderItemResponseDTO.OrderItemOptionDTO.builder()
                .menuOptionId(option.getMenuOptionId())
                .additionalPrice(option.getAdditionalPrice())
                .optionName(option.getOptionName())
                .build();
    }

    private static List<OrderItemOption> convertCartItemOptionsToOrderItemOptions(List<CartItemOption> cartItemOptions) {
        if (cartItemOptions == null || cartItemOptions.isEmpty()) {
            return Collections.emptyList();
        }

        return cartItemOptions.stream()
                .map(OrderItemConverter::convertCartItemOptionToOrderItemOption)
                .collect(Collectors.toList());
    }

    private static OrderItemOption convertCartItemOptionToOrderItemOption(CartItemOption cartItemOption) {
        if (cartItemOption == null) {
            return null;
        }

        return OrderItemOption.builder()
                .menuOptionId(cartItemOption.getMenuOptionId())
                .additionalPrice(cartItemOption.getAdditionalPrice())
                .optionName(cartItemOption.getOptionName())
                .build();
    }
}