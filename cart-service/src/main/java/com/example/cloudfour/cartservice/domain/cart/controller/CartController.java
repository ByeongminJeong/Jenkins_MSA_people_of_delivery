package com.example.cloudfour.cartservice.domain.cart.controller;

import com.example.cloudfour.cartservice.domain.cart.dto.CartRequestDTO;
import com.example.cloudfour.cartservice.domain.cart.dto.CartResponseDTO;
import com.example.cloudfour.cartservice.domain.cart.service.command.CartCommandService;
import com.example.cloudfour.cartservice.domain.cart.service.query.CartQueryService;
import com.example.cloudfour.modulecommon.apiPayLoad.CustomResponse;
import com.example.cloudfour.modulecommon.dto.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.UUID;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "장바구니 API by 조성칠")
public class CartController {
    private final CartCommandService cartCommandService;
    private final CartQueryService cartQueryService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "장바구니 생성", description = "장바구니를 생성합니다. 장바구니 생성에 사용되는 API입니다.")
    public CustomResponse<CartResponseDTO.CartCreateResponseDTO> createCart(
            @Valid @RequestBody CartRequestDTO.CartCreateRequestDTO cartCreateRequestDTO,
            @AuthenticationPrincipal CurrentUser user
    ){
        CartResponseDTO.CartCreateResponseDTO cart = cartCommandService.createCart(cartCreateRequestDTO,user);
        return CustomResponse.onSuccess(HttpStatus.CREATED, cart);
    }

    @GetMapping("/{cartId}")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "장바구니 조회", description = "장바구니를 조회합니다. 장바구니 조회에 사용되는 API입니다.")
    public CustomResponse<CartResponseDTO.CartDetailResponseDTO> getCart(
            @PathVariable("cartId") UUID cartId,
            @AuthenticationPrincipal CurrentUser user
    ){
        CartResponseDTO.CartDetailResponseDTO cart = cartQueryService.getCartListById(cartId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, cart);
    }

    @DeleteMapping("/{cartId}")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "장바구니 삭제", description = "장바구니를 삭제합니다. 장바구니 삭제에 사용되는 API입니다.")
    public CustomResponse<String> deleteCart(
            @PathVariable("cartId") UUID cartId,
            @AuthenticationPrincipal CurrentUser user
    ) {
        cartCommandService.deleteCart(cartId, user);
        return CustomResponse.onSuccess(HttpStatus.OK, "장바구니 삭제 완료");
    }

}
