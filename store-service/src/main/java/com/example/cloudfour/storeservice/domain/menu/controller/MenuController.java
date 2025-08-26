package com.example.cloudfour.storeservice.domain.menu.controller;

import com.example.cloudfour.modulecommon.apiPayLoad.CustomResponse;
import com.example.cloudfour.modulecommon.dto.CurrentUser;
import com.example.cloudfour.storeservice.domain.menu.dto.MenuRequestDTO;
import com.example.cloudfour.storeservice.domain.menu.dto.MenuResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.dto.MenuOptionResponseDTO;
import com.example.cloudfour.storeservice.domain.menu.service.command.MenuCommandService;
import com.example.cloudfour.storeservice.domain.menu.service.query.MenuQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/menus")
@Tag(name = "Menu", description = "메뉴 API by 정병민")
public class MenuController {

    private final MenuCommandService menuCommandService;
    private final MenuQueryService menuQueryService;

    @PostMapping("/{storeId}")
    @PreAuthorize("hasRole('ROLE_OWNER') and authentication.principal.id == #user.id()")
    @Operation(summary = "메뉴 생성", description = "메뉴를 생성합니다.")
    public CustomResponse<MenuResponseDTO.MenuDetailResponseDTO> createMenu(
            @PathVariable("storeId") UUID storeId,
            @Valid @RequestBody MenuRequestDTO.MenuCreateRequestDTO requestDTO,
            @AuthenticationPrincipal CurrentUser user) {

        MenuResponseDTO.MenuDetailResponseDTO result = menuCommandService.createMenu(requestDTO, storeId, user);
        return CustomResponse.onSuccess(HttpStatus.CREATED, result);
    }

    @GetMapping("/{menuId}/detail")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "메뉴 상세 조회", description = "메뉴의 상세 정보를 조회합니다.")
    public CustomResponse<MenuResponseDTO.MenuDetailResponseDTO> getMenuDetail(
            @PathVariable("menuId") UUID menuId,@AuthenticationPrincipal CurrentUser user) {

        MenuResponseDTO.MenuDetailResponseDTO result = menuQueryService.getMenuDetail(menuId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, result);
    }

    @GetMapping("/{storeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "해당 가게 메뉴 목록 조회", description = "가게의 메뉴 목록을 조회합니다.")
    public CustomResponse<MenuResponseDTO.MenuStoreListResponseDTO> getMenusByStore(
            @PathVariable("storeId") UUID storeId,
            @AuthenticationPrincipal CurrentUser user
        ) {

        MenuResponseDTO.MenuStoreListResponseDTO result =
                menuQueryService.getMenusByStoreWithCursor(storeId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, result);
    }

    @GetMapping("/{storeId}/category")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "해당 가게 메뉴 카테고리 별 목록 조회", description = "가게의 카테고리 별 목록을 조회합니다.")
    public CustomResponse<MenuResponseDTO.MenuStoreListResponseDTO> getMenusByCategory(
            @PathVariable("storeId") UUID storeId,
            @RequestParam(name = "categoryId") UUID categoryId,
            @AuthenticationPrincipal CurrentUser user
            ) {

        MenuResponseDTO.MenuStoreListResponseDTO result =
                menuQueryService.getMenusByStoreWithCategory(storeId, categoryId, user);
        return CustomResponse.onSuccess(HttpStatus.OK, result);
    }

    @PatchMapping("/{menuId}")
    @PreAuthorize("hasRole('ROLE_OWNER') and authentication.principal.id == #user.id()")
    @Operation(summary = "메뉴 수정", description = "메뉴를 수정합니다.")
    public CustomResponse<MenuResponseDTO.MenuDetailResponseDTO> updateMenu(
            @Valid @RequestBody MenuRequestDTO.MenuUpdateRequestDTO requestDTO,
            @PathVariable("menuId") UUID menuId,
            @AuthenticationPrincipal CurrentUser user) {

        MenuResponseDTO.MenuDetailResponseDTO result = menuCommandService.updateMenu(menuId, requestDTO, user);
        return CustomResponse.onSuccess(HttpStatus.OK, result);
    }

    @DeleteMapping("/{menuId}/deleted")
    @PreAuthorize("(hasRole('ROLE_OWNER') and authentication.principal.id == #user.id()) or hasRole('ROLE_MASTER')")
    @Operation(summary = "메뉴 삭제", description = "메뉴를 삭제합니다.")
    public CustomResponse<String> deleteMenu(
            @PathVariable("menuId") UUID menuId,
            @AuthenticationPrincipal CurrentUser user) {

        menuCommandService.deleteMenu(menuId, user);
        return CustomResponse.onSuccess(HttpStatus.OK, "메뉴 삭제 완료");
    }

    @PostMapping("/{menuId}/options")
    @PreAuthorize("hasRole('ROLE_OWNER') and authentication.principal.id == #user.id()")
    @Operation(summary = "메뉴 옵션 생성", description = "특정 메뉴에 새로운 옵션을 추가합니다.")
    public CustomResponse<MenuOptionResponseDTO.MenuOptionSimpleResponseDTO> createMenuOption(
            @PathVariable("menuId") UUID menuId,
            @Valid @RequestBody MenuRequestDTO.MenuOptionCreateRequestDTO requestDTO,
            @AuthenticationPrincipal CurrentUser user) {

        MenuOptionResponseDTO.MenuOptionSimpleResponseDTO result =
                menuCommandService.createMenuOption(requestDTO, user, menuId);
        return CustomResponse.onSuccess(HttpStatus.CREATED, result);
    }

    @GetMapping("/{menuId}/options")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "메뉴별 옵션 목록 조회", description = "특정 메뉴의 모든 옵션을 조회합니다.")
    public CustomResponse<MenuOptionResponseDTO.MenuOptionsByMenuResponseDTO> getMenuOptions(
            @PathVariable("menuId") UUID menuId,@AuthenticationPrincipal CurrentUser user
            ) {

        MenuOptionResponseDTO.MenuOptionsByMenuResponseDTO result =
                menuQueryService.getMenuOptionsByMenu(menuId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, result);
    }

    @GetMapping("/options/{optionId}/detail")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "메뉴 옵션 상세 조회", description = "메뉴 옵션의 상세 정보를 조회합니다.")
    public CustomResponse<MenuOptionResponseDTO.MenuOptionSimpleResponseDTO> getMenuOptionDetail(
            @PathVariable("optionId") UUID optionId,@AuthenticationPrincipal CurrentUser user
            ) {

        MenuOptionResponseDTO.MenuOptionSimpleResponseDTO result =
                menuQueryService.getMenuOptionDetail(optionId,user);
        return CustomResponse.onSuccess(HttpStatus.OK, result);
    }

    @PatchMapping("/options/{optionId}")
    @PreAuthorize("(hasRole('ROLE_OWNER') and authentication.principal.id == #user.id()) or hasRole('ROLE_MASTER')")
    @Operation(summary = "메뉴 옵션 수정", description = "메뉴 옵션의 정보를 수정합니다.")
    public CustomResponse<MenuOptionResponseDTO.MenuOptionSimpleResponseDTO> updateMenuOption(
            @PathVariable("optionId") UUID optionId,
            @Valid @RequestBody MenuRequestDTO.MenuOptionUpdateRequestDTO requestDTO,
            @AuthenticationPrincipal CurrentUser user) {

        MenuOptionResponseDTO.MenuOptionSimpleResponseDTO result =
                menuCommandService.updateMenuOption(optionId, requestDTO, user);
        return CustomResponse.onSuccess(HttpStatus.OK, result);
    }

    @DeleteMapping("/options/{optionId}/deleted")
    @PreAuthorize("(hasRole('ROLE_OWNER') and authentication.principal.id == #user.id()) or hasRole('ROLE_MASTER')")
    @Operation(summary = "메뉴 옵션 삭제", description = "메뉴 옵션을 삭제합니다.")
    public CustomResponse<String> deleteMenuOption(
            @PathVariable("optionId") UUID optionId,
            @AuthenticationPrincipal CurrentUser user) {

        menuCommandService.deleteMenuOption(optionId, user);
        return CustomResponse.onSuccess(HttpStatus.OK, "메뉴 옵션 삭제 완료");
    }
}
