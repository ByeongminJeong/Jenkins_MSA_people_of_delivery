package com.example.cloudfour.cartservice.domain.order.service.command;

import com.example.cloudfour.cartservice.client.StoreClient;
import com.example.cloudfour.cartservice.client.UserClient;
import com.example.cloudfour.cartservice.commondto.MenuQuantityResponseDTO;
import com.example.cloudfour.cartservice.domain.cart.entity.Cart;
import com.example.cloudfour.cartservice.domain.cart.exception.CartErrorCode;
import com.example.cloudfour.cartservice.domain.cart.exception.CartException;
import com.example.cloudfour.cartservice.domain.cart.repository.CartRepository;
import com.example.cloudfour.cartservice.domain.cartitem.entity.CartItem;
import com.example.cloudfour.cartservice.domain.cartitem.exception.CartItemException;
import com.example.cloudfour.cartservice.domain.cartitem.exception.CartItemErrorCode;
import com.example.cloudfour.cartservice.commondto.UserAddressResponseDTO;
import com.example.cloudfour.cartservice.domain.order.converter.OrderConverter;
import com.example.cloudfour.cartservice.domain.order.converter.OrderItemConverter;
import com.example.cloudfour.cartservice.domain.order.dto.OrderRequestDTO;
import com.example.cloudfour.cartservice.domain.order.dto.OrderResponseDTO;
import com.example.cloudfour.cartservice.domain.order.entity.Order;
import com.example.cloudfour.cartservice.domain.order.entity.OrderItem;
import com.example.cloudfour.cartservice.domain.order.enums.OrderStatus;
import com.example.cloudfour.cartservice.domain.order.exception.OrderErrorCode;
import com.example.cloudfour.cartservice.domain.order.exception.OrderException;
import com.example.cloudfour.cartservice.domain.order.exception.StockErrorCode;
import com.example.cloudfour.cartservice.domain.order.exception.StockException;
import com.example.cloudfour.cartservice.domain.order.repository.OrderItemOptionRepository;
import com.example.cloudfour.cartservice.domain.order.repository.OrderItemRepository;
import com.example.cloudfour.cartservice.domain.order.repository.OrderRepository;
import com.example.cloudfour.cartservice.service.OrderEventPublishService;
import com.example.cloudfour.modulecommon.dto.CurrentUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final CartRepository cartRepository;
    private final StoreClient storeClient;
    private final UserClient userClient;
    private final OrderEventPublishService orderEventPublishService;


    public OrderResponseDTO.OrderCreateResponseDTO createOrder(
            OrderRequestDTO.OrderCreateRequestDTO req, 
            UUID cartId, 
            CurrentUser user
    ) {
        validateUser(user);
        validateCartId(cartId);

        Cart cart = findCartWithOwnershipValidation(cartId, user.id());
        UserAddressResponseDTO userAddress = fetchUserAddress(user.id());
        validateStoreExists(cart.getStore());
        validateCartItemsNotEmpty(cart.getCartItems());

        validateAndDecreaseStock(cart.getCartItems());

        int totalPrice = calculateTotalPrice(cart.getCartItems());
        
        Order order = createOrderEntity(req, totalPrice, userAddress.getAddress(), cart.getStore(), user.id());
        orderRepository.save(order);
        
        List<OrderItem> orderItems = createOrderItems(cart.getCartItems(), order);
        orderItemRepository.saveAll(orderItems);
        
        saveOrderItemOptions(orderItems);
        
        deleteCart(cart);

        publishOrderCreatedEvent(order, totalPrice, userAddress.getAddress());

        log.info("주문 생성 완료 (orderId={}, totalPrice={})", order.getId(), totalPrice);
        return OrderConverter.toOrderCreateResponseDTO(order);
    }

    public OrderResponseDTO.OrderUpdateResponseDTO updateOrder(
            OrderRequestDTO.OrderUpdateRequestDTO req, 
            UUID orderId, 
            CurrentUser user
    ) {
        validateUser(user);
        validateOrderId(orderId);
        validateOrderOwnership(orderId, user.id());

        Order order = findOrderById(orderId);
        OrderStatus prevStatus = order.getStatus();
        OrderStatus newStatus = req.getNewStatus();

        if (newStatus == OrderStatus.주문취소 && prevStatus != OrderStatus.주문취소) {
            log.info("주문 취소로 인한 재고 복구 시작: orderId={}", orderId);
            restoreStock(orderId);
        }
        
        order.updateOrderStatus(newStatus);
        orderRepository.save(order);
        
        log.info("주문 수정 완료 (orderId={}, status: {} -> {})", 
            orderId, prevStatus, order.getStatus());
        
        return OrderConverter.toOrderUpdateResponseDTO(order, prevStatus);
    }

    public void deleteOrder(UUID orderId, CurrentUser user) {
        validateUser(user);
        validateOrderId(orderId);
        validateOrderOwnership(orderId, user.id());

        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.주문취소) {
            log.info("주문 삭제로 인한 재고 복구 시작: orderId={}", orderId);
            restoreStock(orderId);
        }
        
        order.softDelete();
        
        log.info("주문 삭제 완료 (orderId={})", orderId);
    }

    public void updateOrderStatusByPaymentEvent(UUID orderId, String newStatus) {
        if (orderId == null) {
            log.warn("PaymentEvent 처리 실패: Order ID가 null입니다");
            throw new OrderException(OrderErrorCode.INVALID_INPUT);
        }

        if (newStatus == null || newStatus.trim().isEmpty()) {
            log.warn("PaymentEvent 처리 실패: 새로운 상태가 null이거나 비어있습니다");
            throw new OrderException(OrderErrorCode.INVALID_INPUT);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("PaymentEvent 처리 실패: 존재하지 않는 주문 (orderId={})", orderId);
                    return new OrderException(OrderErrorCode.NOT_FOUND);
                });

        OrderStatus prevStatus = order.getStatus();
        OrderStatus newOrderStatus;

        try {
            newOrderStatus = OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            log.warn("PaymentEvent 처리 실패: 유효하지 않은 주문 상태 (orderId={}, status={})", 
                orderId, newStatus);
            throw new OrderException(OrderErrorCode.INVALID_INPUT);
        }

        if (prevStatus == newOrderStatus) {
            log.info("PaymentEvent 처리: 이미 동일한 상태 (orderId={}, status={})", 
                orderId, newStatus);
            return;
        }

        order.updateOrderStatus(newOrderStatus);
        orderRepository.save(order);

        log.info("PaymentEvent에 의한 주문 상태 업데이트 완료 (orderId={}, status: {} -> {})", 
            orderId, prevStatus, newOrderStatus);
    }

    private void validateUser(CurrentUser user) {
        if (user == null || user.id() == null) {
            log.warn("유효하지 않은 사용자");
            throw new OrderException(OrderErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private void validateCartId(UUID cartId) {
        if (cartId == null) {
            log.warn("Cart ID가 null입니다");
            throw new OrderException(OrderErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private void validateOrderId(UUID orderId) {
        if (orderId == null) {
            log.warn("Order ID가 null입니다");
            throw new OrderException(OrderErrorCode.NOT_FOUND);
        }
    }

    private void validateOrderOwnership(UUID orderId, UUID userId) {
        if (!orderRepository.existsByOrderIdAndUserId(orderId, userId)) {
            log.warn("주문 접근 권한 없음 (orderId={}, userId={})", orderId, userId);
            throw new OrderException(OrderErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 주문: {}", orderId);
                    return new OrderException(OrderErrorCode.NOT_FOUND);
                });
    }

    private Cart findCartWithOwnershipValidation(UUID cartId, UUID userId) {
        Cart cart = cartRepository.findByIdAndUserWithCartItems(cartId, userId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 장바구니 또는 접근 권한 없음 (cartId={}, userId={})", cartId, userId);
                    return new CartException(CartErrorCode.NOT_FOUND);
                });

        if (!cartRepository.existsByUserAndCart(userId, cartId)) {
            log.warn("주문 생성 권한 없음 (cartId={}, userId={})", cartId, userId);
            throw new OrderException(OrderErrorCode.UNAUTHORIZED_ACCESS);
        }

        return cart;
    }

    private UserAddressResponseDTO fetchUserAddress(UUID userId) {
        try {
            return userClient.addressById(userId);
        } catch (Exception e) {
            log.error("사용자 주소 조회 실패 (userId={})", userId, e);
            throw new OrderException(OrderErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private void validateStoreExists(UUID storeId) {
        if (storeId == null) {
            log.warn("Store ID가 null입니다");
            throw new CartException(CartErrorCode.STORE_NOT_FOUND);
        }

        if (!storeClient.existStore(storeId)) {
            log.warn("존재하지 않는 스토어: {}", storeId);
            throw new CartException(CartErrorCode.STORE_NOT_FOUND);
        }
    }

    private void validateCartItemsNotEmpty(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            log.warn("장바구니에 아이템이 없습니다");
            throw new CartItemException(CartItemErrorCode.NOT_FOUND);
        }
    }

    private int calculateTotalPrice(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToInt(CartItem::getPrice)
                .sum();
    }

    private Order createOrderEntity(
            OrderRequestDTO.OrderCreateRequestDTO req, 
            int totalPrice, 
            String address, 
            UUID storeId, 
            UUID userId
    ) {
        Order order = OrderConverter.toOrder(req, totalPrice, address);
        order.setStore(storeId);
        order.setUser(userId);
        return order;
    }

    private List<OrderItem> createOrderItems(List<CartItem> cartItems, Order order) {
        return cartItems.stream()
                .map(cartItem -> OrderItemConverter.CartItemtoOrderItem(cartItem, order))
                .toList();
    }

    private void saveOrderItemOptions(List<OrderItem> orderItems) {
        orderItems.stream()
                .filter(orderItem -> orderItem.getOptions() != null && !orderItem.getOptions().isEmpty())
                .forEach(orderItem -> orderItemOptionRepository.saveAll(orderItem.getOptions()));
    }

    private void deleteCart(Cart cart) {
        try {
            cartRepository.delete(cart);
            log.debug("장바구니 삭제 완료 (cartId={})", cart.getId());
        } catch (Exception e) {
            log.error("장바구니 삭제 실패 (cartId={})", cart.getId(), e);
        }
    }

    private void validateAndDecreaseStock(List<CartItem> cartItems) {
        log.info("재고 확인 및 감소 시작 - {} 개 아이템", cartItems.size());
        
        for (CartItem cartItem : cartItems) {
            UUID menuId = cartItem.getMenu();
            Long orderQuantity = Long.valueOf(cartItem.getQuantity());
            
            log.debug("메뉴 재고 확인: menuId={}, 주문수량={}", menuId, orderQuantity);

            MenuQuantityResponseDTO stockInfo = storeClient.getMenuStock(menuId);
            if (stockInfo == null) {
                log.error("재고 정보 조회 실패: menuId={}", menuId);
                throw new StockException(StockErrorCode.STOCK_NOT_FOUND);
            }

            if (stockInfo.getQuantity() < orderQuantity) {
                log.error("재고 부족: menuId={}, 현재재고={}, 주문수량={}", 
                    menuId, stockInfo.getQuantity(), orderQuantity);
                throw new StockException(StockErrorCode.INSUFFICIENT_STOCK);
            }

            boolean success = storeClient.decreaseStock(stockInfo.getStockId(), orderQuantity);
            if (!success) {
                log.error("재고 감소 실패: stockId={}, quantity={}", stockInfo.getStockId(), orderQuantity);
                throw new StockException(StockErrorCode.STOCK_UPDATE_FAILED);
            }
            
            log.info("재고 감소 성공: menuId={}, stockId={}, quantity={}", 
                menuId, stockInfo.getStockId(), orderQuantity);
        }
        
        log.info("모든 아이템의 재고 확인 및 감소 완료");
    }

    public void restoreStock(UUID orderId) {
        log.info("재고 복구 시작: orderId={}", orderId);
        
        Order order = findOrderById(orderId);
        List<OrderItem> orderItems = order.getOrderItems();
        
        for (OrderItem orderItem : orderItems) {
            UUID menuId = orderItem.getMenu();
            Long quantity = Long.valueOf(orderItem.getQuantity());
            
            try {
                MenuQuantityResponseDTO stockInfo = storeClient.getMenuStock(menuId);
                if (stockInfo == null) {
                    log.warn("재고 복구 실패 - 재고 정보 없음: menuId={}", menuId);
                    continue;
                }

                boolean success = storeClient.increaseStock(stockInfo.getStockId(), quantity);
                if (success) {
                    log.info("재고 복구 성공: menuId={}, stockId={}, quantity={}", 
                        menuId, stockInfo.getStockId(), quantity);
                } else {
                    log.warn("재고 복구 실패: menuId={}, stockId={}, quantity={}", 
                        menuId, stockInfo.getStockId(), quantity);
                }
                
            } catch (Exception e) {
                log.error("재고 복구 중 오류 발생: menuId={}, quantity={}", menuId, quantity, e);
            }
        }
        
        log.info("재고 복구 완료: orderId={}", orderId);
    }

    private void publishOrderCreatedEvent(Order order, int totalPrice, String deliveryAddress) {
        try {
            orderEventPublishService.publishOrderCreatedEvent(
                order.getId(),
                order.getUser(),
                order.getStore(),
                BigDecimal.valueOf(totalPrice),
                order.getStatus().name(),
                deliveryAddress
            );
            log.info("주문 생성 이벤트 발행 완료: orderId={}", order.getId());
        } catch (Exception e) {
            log.error("주문 생성 이벤트 발행 실패: orderId={}, error={}", order.getId(), e.getMessage(), e);
        }
    }
}
