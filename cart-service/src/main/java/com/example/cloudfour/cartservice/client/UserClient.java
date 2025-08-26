package com.example.cloudfour.cartservice.client;

import com.example.cloudfour.cartservice.commondto.UserAddressResponseDTO;
import com.example.cloudfour.cartservice.commondto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserClient {
    private final RestTemplate rt;

    private static final String BASE = "http://user-service/internal/users";

    @Cacheable(value = "userAddresses", key = "#userId")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public UserAddressResponseDTO addressById(UUID userId) {
        if (userId == null) {
            log.warn("User ID가 null입니다");
            return null;
        }

        try {
            log.info("사용자 주소 정보 조회 시작: {}", userId);
            UserAddressResponseDTO address = rt.getForObject(BASE + "/addresses/{userId}", UserAddressResponseDTO.class, userId);
            
            if (address != null) {
                log.info("사용자 주소 정보 조회 완료: {} - {}", userId, address.getAddress());
            } else {
                log.warn("사용자 주소 정보가 null입니다: {}", userId);
            }
            
            return address;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("사용자 주소 정보를 찾을 수 없습니다: {}", userId);
            return null;
        } catch (Exception e) {
            log.error("사용자 주소 정보 조회 실패: {}", userId, e);
            throw e;
        }
    }

    @Cacheable(value = "users", key = "#userId")
    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public UserResponseDTO userById(UUID userId) {
        if (userId == null) {
            log.warn("User ID가 null입니다");
            return null;
        }

        try {
            log.info("사용자 정보 조회 시작: {}", userId);
            UserResponseDTO user = rt.getForObject(BASE + "/{userId}", UserResponseDTO.class, userId);
            
            if (user != null) {
                log.info("사용자 정보 조회 완료: {} - {}", userId, user.getEmail());
            } else {
                log.warn("사용자 정보가 null입니다: {}", userId);
            }
            
            return user;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("사용자 정보를 찾을 수 없습니다: {}", userId);
            return null;
        } catch (Exception e) {
            log.error("사용자 정보 조회 실패: {}", userId, e);
            throw e;
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public Boolean existUser(UUID userId) {
        if (userId == null) {
            log.warn("User ID가 null입니다");
            return false;
        }

        try {
            log.info("사용자 존재 확인 시작: {}", userId);
            rt.headForHeaders(BASE + "/exists?userId=" + userId);
            log.info("사용자 존재 확인 완료: {}", userId);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            log.info("사용자가 존재하지 않습니다: {}", userId);
            return false;
        } catch (Exception e) {
            log.error("사용자 존재 여부 확인 실패: {}", userId, e);
            throw e;
        }
    }
}
