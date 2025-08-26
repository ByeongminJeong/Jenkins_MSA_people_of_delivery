package com.example.cloudfour.authservice.domain.auth.controller;

import ch.qos.logback.classic.Logger;
import com.example.cloudfour.modulecommon.apiPayLoad.CustomResponse;
import com.example.cloudfour.authservice.domain.auth.dto.AuthRequestDTO;
import com.example.cloudfour.authservice.domain.auth.dto.AuthResponseDTO;
import com.example.cloudfour.authservice.domain.auth.service.AuthService;
import com.example.cloudfour.modulecommon.dto.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Auth", description = "인증 API by 모시은")
public class AuthController {

    private final AuthService authService;
    private Logger log;

    @PostMapping("/register/customer")
    @Operation(summary = "고객 로컬 회원가입", description = "고객 계정을 생성합니다.")
    public CustomResponse<AuthResponseDTO.AuthRegisterResponseDTO> registercustomer(
            @Valid @RequestBody AuthRequestDTO.RegisterRequestDTO request) {
        var user = authService.register(request);
        return CustomResponse.onSuccess(HttpStatus.CREATED, user);
    }

    @PostMapping("/register/owner")
    @Operation(summary = "점주 로컬 회원가입", description = "점주 계정을 생성합니다.")
    public CustomResponse<AuthResponseDTO.AuthRegisterResponseDTO> registerowner(
            @Valid @RequestBody AuthRequestDTO.RegisterRequestDTO request) {
        var user = authService.register(request);
        return CustomResponse.onSuccess(HttpStatus.CREATED, user);
    }

    @PostMapping("/login")
    @Operation(summary = "로컬 로그인", description = "생성된 계정으로 로그인합니다. (Gateway가 JWT 발급)")
    public CustomResponse<AuthResponseDTO.AuthTokenResponseDTO> login(@Valid @RequestBody AuthRequestDTO.LoginRequestDTO request) {
        AuthResponseDTO.AuthTokenResponseDTO result = authService.login(request);
        return CustomResponse.onSuccess(HttpStatus.OK, result);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그인된 계정을 로그아웃합니다.")
    public CustomResponse<Void> logout(@RequestHeader("Authorization") String accessToken) {
        authService.logout(accessToken);
        return CustomResponse.onSuccess(null);
    }

    @PostMapping("/refresh")
    @Operation(summary = "token 재발급", description = "토큰을 재발급합니다.")
    public CustomResponse<AuthResponseDTO.AuthTokenResponseDTO> refresh(@RequestBody AuthRequestDTO.RefreshTokenRequestDTO request) {
        AuthResponseDTO.AuthTokenResponseDTO result = authService.refreshAccessToken(request);
        return CustomResponse.onSuccess(HttpStatus.OK, result);
    }

    @PostMapping("/password")
    @PreAuthorize("isAuthenticated() and authentication.principal.id == #user.id()")
    @Operation(summary = "비밀번호 재설정", description = "비밀번호를 재설정합니다.")
    public CustomResponse<Void> changePassword(
            @Valid @RequestBody AuthRequestDTO.PasswordChangeDto request,
            @AuthenticationPrincipal CurrentUser user) {
        authService.changePassword(user.id(), request);
        return CustomResponse.onSuccess(null);
    }

    @PostMapping("/email/send")
    @Operation(summary = "이메일 인증 전송", description = "입력된 계정으로 인증 이메일을 전송합니다.")
    public CustomResponse<Void> sendEmail(@Valid @RequestBody AuthRequestDTO.EmailCodeRequestDTO request) {
        authService.sendVerificationEmail(request.email());
        return CustomResponse.onSuccess(null);
    }

    @PostMapping("/email/verify")
    @Operation(summary = "이메일 인증 검증", description = "입력된 이메일의 인증 코드를 검증합니다.")
    public CustomResponse<Void> verifyEmail(@Valid @RequestBody AuthRequestDTO.EmailVerifyRequestDTO request) {
        authService.verifyEmailCode(request);
        return CustomResponse.onSuccess(null);
    }

    @PostMapping("/email/change/start")
    @PreAuthorize("isAuthenticated() and authentication.principal.id == #user.id()")
    @Operation(summary = "이메일 수정", description = "새로 입력된 이메일로 이메일을 수정합니다.")
    public CustomResponse<Void> startEmailChange(@AuthenticationPrincipal CurrentUser user,
                                                 @Valid @RequestBody AuthRequestDTO.EmailChangeStartRequestDTO req) {
        authService.startEmailChange(user.id(), req.newEmail());
        return CustomResponse.onSuccess(null);
    }

    @PostMapping("/email/change/verify")
    @PreAuthorize("isAuthenticated() and authentication.principal.id == #user.id()")
    @Operation(summary = "수정된 이메일 검증", description = "수정된 이메일의 인증 코드를 검증합니다.")
    public CustomResponse<Void> verifyEmailChange(@AuthenticationPrincipal CurrentUser user,
                                                  @Valid @RequestBody AuthRequestDTO.EmailChangeVerifyRequestDTO req) {
        authService.verifyEmailChange(user.id(), req.newEmail(), req.code());
        return CustomResponse.onSuccess(null);
    }
}