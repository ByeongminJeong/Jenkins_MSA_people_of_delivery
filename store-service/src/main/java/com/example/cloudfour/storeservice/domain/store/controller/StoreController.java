package com.example.cloudfour.storeservice.domain.store.controller;

import com.example.cloudfour.modulecommon.apiPayLoad.CustomResponse;
import com.example.cloudfour.modulecommon.dto.CurrentUser;
import com.example.cloudfour.storeservice.domain.store.dto.StoreRequestDTO;
import com.example.cloudfour.storeservice.domain.store.dto.StoreResponseDTO;
import com.example.cloudfour.storeservice.domain.store.service.command.StoreCommandService;
import com.example.cloudfour.storeservice.domain.store.service.query.StoreQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores")
@Tag(name = "Store", description = "가게 API by 지윤")
public class StoreController {

    private final StoreCommandService storeCommandService;
    private final StoreQueryService storeQueryService;
    
    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_OWNER') and authentication.principal.id == #user.id()")
    @Operation(summary = "가게 등록", description = "가게를 등록합니다.")
    public CustomResponse<StoreResponseDTO.StoreCreateResponseDTO> createStore(
            @Valid  @RequestBody StoreRequestDTO.StoreCreateRequestDTO dto,
            @AuthenticationPrincipal CurrentUser user
    ) {
        return CustomResponse.onSuccess(HttpStatus.CREATED, storeCommandService.createStore(dto, user));
    }

    @GetMapping("")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "키워드로 가게 목록 조회", description = "키워드에 해당하는 가게 목록을 커서 기반으로 조회합니다.")
    @Parameter(name = "cursor", description = "데이터가 시작하는 기준 시간입니다.")
    @Parameter(name = "size", description = "가져올 데이터 수입니다.")
    public CustomResponse<StoreResponseDTO.StoreCursorListResponseDTO> getStoreList(
            @RequestParam(name = "cursor", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursor,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @AuthenticationPrincipal CurrentUser user
    ) {
        StoreResponseDTO.StoreCursorListResponseDTO response = storeQueryService.getAllStores(cursor, size,keyword,user);
        return CustomResponse.onSuccess(HttpStatus.OK, response);
    }

    @GetMapping("/{storeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "가게 상세 정보 조회", description = "가게의 상세 정보를 조회합니다.")
    public CustomResponse<StoreResponseDTO.StoreDetailResponseDTO> getStoreDetail(
            @PathVariable UUID storeId,@AuthenticationPrincipal CurrentUser user
    ) {
        return CustomResponse.onSuccess(HttpStatus.OK, storeQueryService.getStoreById(storeId,user));
    }

    @PatchMapping("/{storeId}")
    @PreAuthorize("hasRole('ROLE_OWNER') and authentication.principal.id == #user.id()")
    @Operation(summary = "가게 정보 수정", description = "본인의 가게 정보를 수정합니다.")
    public CustomResponse<StoreResponseDTO.StoreUpdateResponseDTO> updateStore(
            @PathVariable UUID storeId,
            @Valid @RequestBody StoreRequestDTO.StoreUpdateRequestDTO dto,
            @AuthenticationPrincipal CurrentUser user
    ) {
        return CustomResponse.onSuccess(HttpStatus.OK, storeCommandService.updateStore(storeId, dto, user));
    }

    @PatchMapping("/{storeId}/deleted")
    @PreAuthorize("(hasRole('ROLE_OWNER') and authentication.principal.id == #user.id()) or hasRole('ROLE_MASTER')")
    @Operation(summary = "가게 삭제", description = "본인의 가게를 삭제합니다.")
    public CustomResponse<String> deleteStore(
            @PathVariable UUID storeId,
            @AuthenticationPrincipal CurrentUser user
    ) {
        storeCommandService.deleteStore(storeId, user);
        return CustomResponse.onSuccess(HttpStatus.OK, "가게 삭제 완료");
    }

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "카테고리별 가게 목록 조회", description = "카테고리 ID로 해당 카테고리의 가게 목록을 커서 기반으로 조회합니다.")
    @Parameter(name = "cursor", description = "데이터가 시작하는 기준 시간입니다.")
    @Parameter(name = "size", description = "가져올 데이터 수입니다.")
    public CustomResponse<StoreResponseDTO.StoreCursorListResponseDTO> getStoresByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(name = "cursor", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursor,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @AuthenticationPrincipal CurrentUser user
    ) {
        StoreResponseDTO.StoreCursorListResponseDTO response =
                storeQueryService.getStoresByCategory(categoryId, cursor, size,user);
        return CustomResponse.onSuccess(HttpStatus.OK, response);
    }


}
