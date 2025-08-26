package com.example.cloudfour.paymentservice.domain.payment.apiclient;

import com.example.cloudfour.paymentservice.commondto.UserResponseDTO;
import com.example.cloudfour.paymentservice.domain.payment.exception.PaymentErrorCode;
import com.example.cloudfour.paymentservice.domain.payment.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {
    
    private final RestTemplate restTemplate;
    
    private static final String USER_SERVICE_URL = "http://user-service";

    public boolean existsUser(UUID userId) {
        try {
            String url = USER_SERVICE_URL + "/users/" + userId + "/exists";
            Boolean exists = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("사용자 존재 여부 확인 실패: userId={}", userId, e);
            throw new PaymentException(PaymentErrorCode.USER_VALIDATION_FAILED);
        }
    }

    public UserResponseDTO getUserById(UUID userId) {
        try {
            String url = USER_SERVICE_URL + "/users/" + userId;
            UserResponseDTO user = restTemplate.getForObject(url, UserResponseDTO.class);
            if (user == null) {
                throw new PaymentException(PaymentErrorCode.USER_NOT_FOUND);
            }
            return user;
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: userId={}", userId, e);
            throw new PaymentException(PaymentErrorCode.USER_VALIDATION_FAILED);
        }
    }
}

