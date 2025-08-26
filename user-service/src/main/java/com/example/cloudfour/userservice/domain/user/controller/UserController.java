package com.example.cloudfour.userservice.domain.user.controller;

import com.example.cloudfour.modulecommon.apiPayLoad.CustomResponse;
import com.example.cloudfour.modulecommon.dto.CurrentUser;
import com.example.cloudfour.userservice.domain.user.dto.UserRequestDTO;
import com.example.cloudfour.userservice.domain.user.dto.UserResponseDTO;
import com.example.cloudfour.userservice.domain.user.service.UserAddressService;
import com.example.cloudfour.userservice.domain.user.service.command.UserProfileCommandService;
import com.example.cloudfour.userservice.domain.user.service.query.UserProfileQueryService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User", description = "유저 API by 모시은")
public class UserController {
    private final UserProfileQueryService queryService;
    private final UserProfileCommandService commandService;
    private final UserAddressService addressService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated() and authentication.principal.id == #user.id()")
    @Operation(summary = "내 정보 조회", description = "내 계정의 상세 정보를 조회합니다.")
    public CustomResponse<UserResponseDTO.MeResponseDTO> getMyInfo(
            @AuthenticationPrincipal CurrentUser user
    ) {
        return CustomResponse.onSuccess(queryService.getMyInfo(user.id()));
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated() and authentication.principal.id == #user.id()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "내 정보 수정", description = "닉네임/전화번호를 수정합니다.")
    public void updateMyInfo(
            @AuthenticationPrincipal CurrentUser user,
            @Valid @RequestBody UserRequestDTO.UserUpdateRequestDTO request
    ) {
        commandService.updateProfile(user.id(), request.nickname(), request.number());
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated() and authentication.principal.id == #user.id() or hasRole('ROLE_MASTER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "내 계정 삭제", description = "내 계정을 소프트 삭제합니다.")
    public void deleteAccount(@AuthenticationPrincipal CurrentUser user) {
        commandService.deleteAccount(user.id());
    }

    @PostMapping("/addresses")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "내 주소 등록", description = "내 주소를 등록합니다.")
    public CustomResponse<UserResponseDTO.AddressResponseDTO> addAddress(@Valid @RequestBody UserRequestDTO.AddressRequestDTO request,
                                                                         @AuthenticationPrincipal CurrentUser user) {
        UserResponseDTO.AddressResponseDTO address = addressService.addAddress(user.id(), request);
        return CustomResponse.onSuccess(HttpStatus.CREATED, address);
    }

    @GetMapping("/addresses")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "내 주소 조회", description = "내 주소를 조회합니다.")
    public CustomResponse<List<UserResponseDTO.AddressResponseDTO>> getAddressList(
            @AuthenticationPrincipal CurrentUser user) {
        List<UserResponseDTO.AddressResponseDTO> list = addressService.getAddresses(user.id());
        return CustomResponse.onSuccess(list);
    }

    @PatchMapping("/addresses/{addressId}")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "내 주소 수정", description = "내 주소를 수정합니다.")
    public CustomResponse<UserResponseDTO.AddressResponseDTO> updateAddress(@PathVariable UUID addressId,
                                              @Valid @RequestBody UserRequestDTO.AddressRequestDTO request,
                                              @AuthenticationPrincipal CurrentUser user) {
        UserResponseDTO.AddressResponseDTO address = addressService.updateAddress(user.id(), addressId, request);
        return CustomResponse.onSuccess(HttpStatus.OK, address);
    }

    @PatchMapping("/addresses/delete/{addressId}")
    @PreAuthorize("hasRole('ROLE_USER') and authentication.principal.id == #user.id()")
    @Operation(summary = "내 주소 삭제", description = "내 주소를 삭제합니다.")
    public CustomResponse<Void> deleteAddress(@PathVariable UUID addressId,
                                              @AuthenticationPrincipal CurrentUser user) {
        addressService.deleteAddress(user.id(), addressId);
        return CustomResponse.onSuccess(null);
    }
}
