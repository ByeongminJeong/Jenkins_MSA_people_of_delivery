package com.example.cloudfour.cartservice.commondto;

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
    boolean emailVerified;
}
