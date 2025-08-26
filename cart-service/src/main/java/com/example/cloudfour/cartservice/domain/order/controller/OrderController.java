package com.example.cloudfour.cartservice.domain.order.controller;

import com.example.cloudfour.cartservice.domain.order.dto.OrderItemResponseDTO;
import com.example.cloudfour.cartservice.domain.order.dto.OrderRequestDTO;
import com.example.cloudfour.cartservice.domain.order.dto.OrderResponseDTO;
import com.example.cloudfour.cartservice.domain.order.service.command.OrderCommandService;
import com.example.cloudfour.cartservice.domain.order.service.query.OrderQueryService;
import com.example.cloudfour.modulecommon.apiPayLoad.CustomResponse;
import com.example.cloudfour.modulecommon.dto.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Order", description = "주문 API by 김준형")
public class OrderController {
    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    @PostMapping("/{cartId}")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "주문 생성", description = "주문을 생성합니다. 주문 생성에 사용되는 API입니다.")
    public CustomResponse<OrderResponseDTO.OrderCreateResponseDTO> createOrder(
            @PathVariable("cartId") UUID cartId,
            @Valid @RequestBody OrderRequestDTO.OrderCreateRequestDTO orderCreateRequestDTO,
            @AuthenticationPrincipal CurrentUser user
    ){
        OrderResponseDTO.OrderCreateResponseDTO order = orderCommandService.createOrder(orderCreateRequestDTO,cartId,user);
        return CustomResponse.onSuccess(HttpStatus.CREATED, order);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "주문 상세 조회", description = "주문을 상세 조회합니다. 주문 상세 조회에 사용되는 API입니다.")
    public CustomResponse<OrderResponseDTO.OrderDetailResponseDTO> getOrder(
            @PathVariable("orderId") UUID orderId,
            @AuthenticationPrincipal CurrentUser user
    ){
        OrderResponseDTO.OrderDetailResponseDTO order = orderQueryService.getOrderById(orderId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, order);
    }

    @GetMapping("/{orderItemId}")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "주문 아이템 상세 조회", description = "주문 아이템을 상세 조회합니다. 주문 아이템 상세 조회에 사용되는 API입니다.")
    public CustomResponse<OrderItemResponseDTO.OrderItemListResponseDTO> getOrderItem(
            @PathVariable("orderItemId") UUID orderItemId,
            @AuthenticationPrincipal CurrentUser user
    ){
        OrderItemResponseDTO.OrderItemListResponseDTO orderItem = orderQueryService.getOrderItemById(orderItemId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, orderItem);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "내 주문 내역 조회", description = "내 주문 내역을 조회합니다. 내 주문 내역 조회에 사용되는 API입니다.")
    @Parameter(name = "cursor", description = "데이터가 시작하는 부분을 표시합니다")
    @Parameter(name = "size", description = "size만큼 데이터를 가져옵니다.")
    public CustomResponse<OrderResponseDTO.OrderUserListResponseDTO> getMyOrder(
            @AuthenticationPrincipal CurrentUser user,
            @RequestParam(name = "cursor", required = false) LocalDateTime cursor,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ){
        OrderResponseDTO.OrderUserListResponseDTO order = orderQueryService.getOrderListByUser(user,cursor,size);
        return CustomResponse.onSuccess(HttpStatus.OK, order);
    }

    @GetMapping("/{storeId}/orders")
    @Secured("ROLE_OWNER")
    @Operation(summary = "가게 주문 조회", description = "가게 주문을 조회합니다. 가게 주문 조회에 사용되는 API입니다.")
    @Parameter(name = "cursor", description = "데이터가 시작하는 부분을 표시합니다")
    @Parameter(name = "size", description = "size만큼 데이터를 가져옵니다.")
    public CustomResponse<OrderResponseDTO.OrderStoreListResponseDTO> getStoreOrder(
            @PathVariable("storeId") UUID storeId,
            @AuthenticationPrincipal CurrentUser user,
            @RequestParam(name = "cursor", required = false) LocalDateTime cursor,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ){
        OrderResponseDTO.OrderStoreListResponseDTO order = orderQueryService.getOrderListByStore(storeId,cursor,size,user);
        return CustomResponse.onSuccess(HttpStatus.OK, order);
    }

    @PatchMapping("/{orderId}/status")
    @Secured("ROLE_OWNER")
    @Operation(summary = "주문 상태 변경", description = "주문 상태를 변경합니다. 주문 상태 변경에 사용되는 API입니다.")
    public CustomResponse<OrderResponseDTO.OrderUpdateResponseDTO>  updateOrderStatus(
            @Valid @RequestBody OrderRequestDTO.OrderUpdateRequestDTO orderUpdateRequestDTO,
            @PathVariable("orderId") UUID orderId,
            @AuthenticationPrincipal CurrentUser user
    ){
        OrderResponseDTO.OrderUpdateResponseDTO order = orderCommandService.updateOrder(orderUpdateRequestDTO,orderId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, order);
    }

    @PatchMapping("/{orderId}/canceled")
    @PreAuthorize("hasRole('ROLE_OWNER') or hasRole('ROLE_MASTER')")
    @Operation(summary = "주문 취소", description = "주문을 취소합니다. 주문 취소에 사용되는 API입니다.")
    public CustomResponse<String>  deleteOrder(
            @PathVariable("orderId") UUID orderId,
            @AuthenticationPrincipal CurrentUser user
    ){
        orderCommandService.deleteOrder(orderId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, "주문 취소 완료.");
    }
}
