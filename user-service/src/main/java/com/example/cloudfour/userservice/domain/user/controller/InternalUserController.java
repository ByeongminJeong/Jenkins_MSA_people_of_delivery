package com.example.cloudfour.userservice.domain.user.controller;

import com.example.cloudfour.userservice.domain.user.dto.AuthRequestDTO;
import com.example.cloudfour.userservice.domain.user.dto.AuthResponseDTO;
import com.example.cloudfour.userservice.domain.user.dto.UserAddressResponseDTO;
import com.example.cloudfour.userservice.domain.user.service.UserAddressService;
import com.example.cloudfour.userservice.domain.user.service.command.UserCommandService;
import com.example.cloudfour.userservice.domain.user.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserQueryService query;
    private final UserCommandService command;
    private final UserAddressService addressService;

    @GetMapping("/exists")
    public AuthResponseDTO.ExistsByEmailResponseDTO existsByEmail(@RequestParam String email) {
        return new AuthResponseDTO.ExistsByEmailResponseDTO(query.existsByEmail(email));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO.UserBriefResponseDTO create(@RequestBody AuthRequestDTO.CreateUserRequestDTO req) {
        return command.create(req);
    }

    @GetMapping("/by-email")
    public AuthResponseDTO.UserBriefResponseDTO byEmail(@RequestParam String email) {
        return query.byEmail(email);
    }

    @GetMapping("/{id}")
    public AuthResponseDTO.UserBriefResponseDTO byId(@PathVariable UUID id) {
        return query.byId(id);
    }

    @PostMapping("/{id}/verify-password")
    public AuthResponseDTO.PasswordVerifyResponseDTO verifyPassword(
            @PathVariable UUID id,
            @RequestBody AuthRequestDTO.PasswordVerifyRequestDTO req) {
        return query.verifyPassword(id, req);
    }

    @GetMapping("/addresses/{userId}")
    public UserAddressResponseDTO addressById(@PathVariable UUID userId){
        return addressService.findAddress(userId);
    }

    @PostMapping("/{id}/email-verified")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markEmailVerified(@PathVariable UUID id) {
        command.markEmailVerified(id);
    }

    @PostMapping("/{id}/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable UUID id,
                               @RequestBody AuthRequestDTO.ChangePasswordRequestDTO req) {
        command.changePassword(id, req);
    }

    @PostMapping("/{id}/email-change/start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void startEmailChange(@PathVariable UUID id,
                                 @RequestBody AuthRequestDTO.EmailChangeStartRequestDTO req) {
        command.startEmailChange(id, req);
    }

    @PostMapping("/{id}/email-change/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmEmailChange(@PathVariable UUID id,
                                   @RequestBody AuthRequestDTO.EmailChangeConfirmRequestDTO req) {
        command.confirmEmailChange(id, req);
    }
}