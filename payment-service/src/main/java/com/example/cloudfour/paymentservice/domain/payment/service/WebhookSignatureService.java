package com.example.cloudfour.paymentservice.domain.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookSignatureService {

    @Value("${toss.webhook-secret}")
    private String webhookSecret;

    public boolean verifySignature(String payload, String signature) {
        if (signature == null || signature.isEmpty()) {
            log.warn("웹훅 서명이 없습니다");
            return false;
        }

        try {
            String expectedSignature = generateSignature(payload);
            boolean isValid = signature.equals(expectedSignature);
            
            if (!isValid) {
                log.warn("웹훅 서명 검증 실패: expected={}, actual={}", expectedSignature, signature);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("웹훅 서명 검증 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    public boolean verifySignature(String payload) {
        log.warn("개발 환경: 웹훅 서명 검증을 건너뜁니다");
        return true;
    }

    private String generateSignature(String payload) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        
        byte[] signatureBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(signatureBytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
