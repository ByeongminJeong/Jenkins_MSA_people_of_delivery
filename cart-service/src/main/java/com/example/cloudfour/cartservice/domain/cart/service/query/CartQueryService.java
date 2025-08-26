package com.example.cloudfour.cartservice.domain.cart.service.query;

import com.example.cloudfour.cartservice.domain.cart.converter.CartConverter;
import com.example.cloudfour.cartservice.domain.cart.dto.CartResponseDTO;
import com.example.cloudfour.cartservice.domain.cart.entity.Cart;
import com.example.cloudfour.cartservice.domain.cart.exception.CartErrorCode;
import com.example.cloudfour.cartservice.domain.cart.exception.CartException;
import com.example.cloudfour.cartservice.domain.cart.repository.CartRepository;
import com.example.cloudfour.cartservice.domain.cartitem.entity.CartItem;
import com.example.cloudfour.cartservice.domain.cartitem.repository.CartItemRepository;
import com.example.cloudfour.modulecommon.dto.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartQueryService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartResponseDTO.CartDetailResponseDTO getCartListById(UUID cartId, CurrentUser user) {
        validateUser(user);
        validateCartId(cartId);

        Cart cart = findCartWithOwnershipValidation(cartId, user.id());
        List<CartItem> cartItemsWithOptions = loadCartItemsWithOptions(cartId);

        replaceCartItems(cart, cartItemsWithOptions);
        
        log.info("장바구니 조회 완료 (cartId={}, itemCount={})", cartId, cartItemsWithOptions.size());
        return CartConverter.toCartDetailResponseDTO(cart);
    }


    private void validateUser(CurrentUser user) {
        if (user == null || user.id() == null) {
            log.warn("유효하지 않은 사용자");
            throw new CartException(CartErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private void validateCartId(UUID cartId) {
        if (cartId == null) {
            log.warn("Cart ID가 null입니다");
            throw new CartException(CartErrorCode.NOT_FOUND);
        }
    }

    private Cart findCartWithOwnershipValidation(UUID cartId, UUID userId) {
        return cartRepository.findByIdAndUserWithCartItems(cartId, userId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 장바구니 또는 접근 권한 없음 (cartId={}, userId={})", cartId, userId);
                    return new CartException(CartErrorCode.NOT_FOUND);
                });
    }

    private List<CartItem> loadCartItemsWithOptions(UUID cartId) {
        List<CartItem> cartItems = cartItemRepository.findAllByCartIdWithOptions(cartId);
        log.debug("CartItem 옵션과 함께 로드 완료 (cartId={}, itemCount={})", cartId, cartItems.size());
        return cartItems;
    }

    private void replaceCartItems(Cart cart, List<CartItem> newCartItems) {
        cart.getCartItems().clear();
        if (!newCartItems.isEmpty()) {
            cart.getCartItems().addAll(newCartItems);
        }
    }
}
