package com.example.cloudfour.authservice.client;

import com.example.cloudfour.authservice.domain.auth.dto.UserRequestDTO;
import com.example.cloudfour.authservice.domain.auth.dto.UserResposneDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserClient {
    private final RestTemplate rt;

    //private static final String BASE = "http://user-service/internal/users";
    private static final String BASE = "http://user-service.peopleofdelivery/internal/users";

    public UserResposneDTO.ExistsByEmailResponseDTO existsByEmail(String email) {
        return rt.getForObject(BASE + "/exists?email={email}", UserResposneDTO.ExistsByEmailResponseDTO.class, email);
    }

    public UserResposneDTO.UserBriefResponseDTO create(UserRequestDTO.CreateUserRequestDTO req) {
        return rt.postForObject(BASE, req, UserResposneDTO.UserBriefResponseDTO.class);
    }

    public UserResposneDTO.UserBriefResponseDTO byEmail(String email) {
        return rt.getForObject(BASE + "/by-email?email={email}", UserResposneDTO.UserBriefResponseDTO.class, email);
    }

    public UserResposneDTO.UserBriefResponseDTO byId(UUID id) {
        return rt.getForObject(BASE + "/{id}", UserResposneDTO.UserBriefResponseDTO.class, id);
    }

    public UserResposneDTO.PasswordVerifyResponseDTO verifyPassword(UUID id, String rawPassword) {
        return rt.postForObject(BASE + "/{id}/verify-password",
                new UserRequestDTO.PasswordVerifyRequestDTO(rawPassword),
                UserResposneDTO.PasswordVerifyResponseDTO.class, id);
    }

    public void markEmailVerified(UUID id) {
        rt.postForLocation(BASE + "/{id}/email-verified", null, id);
    }

    public void changePassword(UUID id, String current, String next) {
        rt.postForLocation(BASE + "/{id}/change-password",
                new UserRequestDTO.ChangePasswordRequestDTO(current, next), id);
    }

    public boolean existsByEmailBool(String email) {
        var res = existsByEmail(email);
        return res != null && res.exists();
    }

    public void startEmailChange(UUID id, String newEmail) {
        var body = new UserRequestDTO.EmailChangeStartRequestDTO(newEmail);
        rt.postForLocation(BASE + "/{id}/email-change/start", body, id);
    }

    public void confirmEmailChange(UUID id, String newEmail) {
        var body = new UserRequestDTO.EmailChangeConfirmRequestDTO(newEmail);
        rt.postForLocation(BASE + "/{id}/email-change/confirm", body, id);
    }
}
