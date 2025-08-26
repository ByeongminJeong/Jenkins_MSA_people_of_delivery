package com.example.cloudfour.cartservice.domain.cartitem.service.query;

import com.example.cloudfour.cartservice.domain.cartitem.converter.CartItemConverter;
import com.example.cloudfour.cartservice.domain.cartitem.dto.CartItemResponseDTO;
import com.example.cloudfour.cartservice.domain.cartitem.entity.CartItem;
import com.example.cloudfour.cartservice.domain.cartitem.exception.CartItemErrorCode;
import com.example.cloudfour.cartservice.domain.cartitem.exception.CartItemException;
import com.example.cloudfour.cartservice.domain.cartitem.repository.CartItemRepository;
import com.example.cloudfour.modulecommon.dto.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartItemQueryService {

    private final CartItemRepository cartItemRepository;

    public CartItemResponseDTO.CartItemListResponseDTO getCartItemById(UUID cartItemId, CurrentUser user) {
        validateUser(user);
        validateCartItemId(cartItemId);
        validateCartItemOwnership(cartItemId, user.id());

        CartItem cartItem = findCartItemWithOptions(cartItemId);
        
        log.info("장바구니 아이템 조회 완료 (cartItemId={})", cartItemId);
        return CartItemConverter.toCartItemListResponseDTO(cartItem);
    }

    private void validateUser(CurrentUser user) {
        if (user == null || user.id() == null) {
            log.warn("유효하지 않은 사용자");
            throw new CartItemException(CartItemErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private void validateCartItemId(UUID cartItemId) {
        if (cartItemId == null) {
            log.warn("CartItem ID가 null입니다");
            throw new CartItemException(CartItemErrorCode.NOT_FOUND);
        }
    }

    private void validateCartItemOwnership(UUID cartItemId, UUID userId) {
        if (!cartItemRepository.existsByCartItemAndUser(cartItemId, userId)) {
            log.warn("장바구니 아이템 조회 권한 없음 (cartItemId={}, userId={})", cartItemId, userId);
            throw new CartItemException(CartItemErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private CartItem findCartItemWithOptions(UUID cartItemId) {
        return cartItemRepository.findByIdWithOptions(cartItemId)
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 장바구니 아이템: {}", cartItemId);
                    return new CartItemException(CartItemErrorCode.NOT_FOUND);
                });
    }
}
