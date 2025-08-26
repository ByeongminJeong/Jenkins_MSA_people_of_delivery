package com.example.cloudfour.cartservice.domain.cartitem.controller;

import com.example.cloudfour.cartservice.domain.cartitem.dto.CartItemRequestDTO;
import com.example.cloudfour.cartservice.domain.cartitem.dto.CartItemResponseDTO;
import com.example.cloudfour.cartservice.domain.cartitem.service.command.CartItemCommandService;
import com.example.cloudfour.cartservice.domain.cartitem.service.query.CartItemQueryService;
import com.example.cloudfour.modulecommon.apiPayLoad.CustomResponse;
import com.example.cloudfour.modulecommon.dto.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/cartItems")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CartItem", description = "장바구니아이템 API by 조성칠")
public class CartItemController {
    private final CartItemCommandService cartItemCommandService;
    private final CartItemQueryService cartItemIdQueryService;

    @PostMapping("/{cartId}")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "장바구니 항목 추가", description = "장바구니 항목을 추가합니다. 장바구니 항목 추가에 사용되는 API입니다.")
    public CustomResponse<CartItemResponseDTO.CartItemAddResponseDTO> addCartItem(
            @PathVariable("cartId") UUID cartId,
            @Valid @RequestBody CartItemRequestDTO.CartItemAddRequestDTO cartItemAddRequestDTO,
            @AuthenticationPrincipal CurrentUser user
    ){
        CartItemResponseDTO.CartItemAddResponseDTO cartItem = cartItemCommandService.AddCartItem(cartItemAddRequestDTO, cartId, user);
        return CustomResponse.onSuccess(HttpStatus.CREATED, cartItem);
    }

    @GetMapping("/{cartItemId}")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "장바구니 항목 조회", description = "장바구니 항목을 조회합니다. 장바구니 항목 조회에 사용되는 API입니다.")
    public CustomResponse<CartItemResponseDTO.CartItemListResponseDTO> getCartItem(
            @PathVariable("cartItemId") UUID cartItemId,
            @AuthenticationPrincipal CurrentUser user
    ){
        CartItemResponseDTO.CartItemListResponseDTO cartItem = cartItemIdQueryService.getCartItemById(cartItemId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, cartItem);
    }

    @PatchMapping("/{cartItemId}")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "장바구니 항목, 옵션 수정", description = "장바구니 항목, 옵션을 수정합니다. 장바구니 항목, 옵션 수정에 사용되는 API입니다.")
    public CustomResponse<CartItemResponseDTO.CartItemUpdateResponseDTO> updateCartItem(
            @PathVariable("cartItemId") UUID cartItemId,
            @Valid @RequestBody CartItemRequestDTO.CartItemUpdateRequestDTO request,
            @AuthenticationPrincipal CurrentUser user
    ){
        log.info("CartItem 수정 요청 - cartItemId: {}, request: menuOptionIds={}, quantity={}", 
            cartItemId, request.getMenuOptionIds(), request.getQuantity());
        
        CartItemResponseDTO.CartItemUpdateResponseDTO cartItem = cartItemCommandService.updateCartItem(request, cartItemId, user);
        return CustomResponse.onSuccess(HttpStatus.OK, cartItem);
    }

    @DeleteMapping("/{cartItemId}")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "장바구니 항목 삭제", description = "장바구니 항목을 삭제합니다. 장바구니 항목 삭제에 사용되는 API입니다.")
    public CustomResponse<String> deleteCartItem(
            @PathVariable("cartItemId") UUID cartItemId,
            @AuthenticationPrincipal CurrentUser user
    ) {
        cartItemCommandService.deleteCartItem(cartItemId, user);
        return CustomResponse.onSuccess(HttpStatus.OK, "장바구니 항목 삭제 완료");
    }
}
