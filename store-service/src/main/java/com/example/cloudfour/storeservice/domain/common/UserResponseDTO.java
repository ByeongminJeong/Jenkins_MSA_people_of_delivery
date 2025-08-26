package com.example.cloudfour.storeservice.domain.common;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserResponseDTO {
    UUID userId;
    String email;
    String nickname;
    String number;
}
