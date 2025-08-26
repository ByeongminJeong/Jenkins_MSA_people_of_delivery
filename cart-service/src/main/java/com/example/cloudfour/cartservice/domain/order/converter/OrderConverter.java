package com.example.cloudfour.cartservice.domain.order.converter;

import com.example.cloudfour.cartservice.domain.order.controller.OrderCommonResponseDTO;
import com.example.cloudfour.cartservice.domain.order.dto.OrderItemResponseDTO;
import com.example.cloudfour.cartservice.domain.order.dto.OrderRequestDTO;
import com.example.cloudfour.cartservice.domain.order.dto.OrderResponseDTO;
import com.example.cloudfour.cartservice.domain.order.entity.Order;
import com.example.cloudfour.cartservice.domain.order.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public final class OrderConverter {

    private OrderConverter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    public static Order toOrder(OrderRequestDTO.OrderCreateRequestDTO orderCreateRequestDTO, Integer totalPrice, String address) {
        return Order.builder()
                .orderType(orderCreateRequestDTO.getOrderType())
                .receiptType(orderCreateRequestDTO.getReceiptType())
                .request(orderCreateRequestDTO.getRequest())
                .address(address)
                .totalPrice(totalPrice)
                .status(orderCreateRequestDTO.getOrderStatus())
                .build();
    }

    public static OrderResponseDTO.OrderCreateResponseDTO toOrderCreateResponseDTO(Order order) {
        return OrderResponseDTO.OrderCreateResponseDTO.builder()
                .orderCommonResponseDTO(toOrderCommonResponseDTO(order))
                .build();
    }

    public static OrderResponseDTO.OrderUpdateResponseDTO toOrderUpdateResponseDTO(Order order, OrderStatus prev_orderStatus) {
        return OrderResponseDTO.OrderUpdateResponseDTO.builder()
                .orderId(order.getId())
                .previousStatus(prev_orderStatus)
                .currentStatus(order.getStatus())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public static OrderResponseDTO.OrderDetailResponseDTO toOrderDetailResponseDTO(Order order, List<OrderItemResponseDTO.OrderItemListResponseDTO> orderItems) {
        return OrderResponseDTO.OrderDetailResponseDTO.builder()
                .orderType(order.getOrderType())
                .receiptType(order.getReceiptType())
                .address(order.getAddress())
                .request(order.getRequest())
                .items(orderItems)
                .orderCommonResponseDTO(toOrderCommonResponseDTO(order))
                .build();
    }

    public static OrderResponseDTO.OrderDetailResponseDTO toOrderDetailResponseDTO(Order order, List<OrderItemResponseDTO.OrderItemListResponseDTO> orderItems, String storeName) {
        return OrderResponseDTO.OrderDetailResponseDTO.builder()
                .storeName(storeName)
                .orderType(order.getOrderType())
                .receiptType(order.getReceiptType())
                .address(order.getAddress())
                .request(order.getRequest())
                .items(orderItems)
                .orderCommonResponseDTO(toOrderCommonResponseDTO(order))
                .build();
    }

    public static OrderResponseDTO.OrderUserResponseDTO toOrderUserResponseDTO(Order order,String storeName) {
        return OrderResponseDTO.OrderUserResponseDTO.builder()
                .storeName(storeName)
                .orderCommonResponseDTO(toOrderCommonResponseDTO(order))
                .build();
    }

    public static OrderResponseDTO.OrderUserListResponseDTO toOrderUserListResponseDTO(List<OrderResponseDTO.OrderUserResponseDTO> orders, Boolean hasNext, LocalDateTime cursor) {
        return OrderResponseDTO.OrderUserListResponseDTO.builder()
                .orderUsers(orders)
                .hasNext(hasNext)
                .cursor(cursor)
                .build();
    }

    public static OrderResponseDTO.OrderStoreResponseDTO toOrderStoreResponseDTO(Order order, String userName) {
        return OrderResponseDTO.OrderStoreResponseDTO.builder()
                .userName(userName)
                .orderType(order.getOrderType())
                .receiptType(order.getReceiptType())
                .orderCommonResponseDTO(toOrderCommonResponseDTO(order))
                .build();
    }

    public static OrderResponseDTO.OrderStoreListResponseDTO toOrderStoreListResponseDTO(List<OrderResponseDTO.OrderStoreResponseDTO> orders, Boolean hasNext, LocalDateTime cursor) {
        return OrderResponseDTO.OrderStoreListResponseDTO.builder()
                .orderStores(orders)
                .hasNext(hasNext)
                .cursor(cursor)
                .build();
    }

    public static OrderCommonResponseDTO toOrderCommonResponseDTO(Order order){
        return OrderCommonResponseDTO.builder()
                .orderId(order.getId())
                .orderStatus(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
