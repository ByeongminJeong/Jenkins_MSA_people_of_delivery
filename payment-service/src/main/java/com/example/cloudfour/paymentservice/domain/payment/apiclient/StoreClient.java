package com.example.cloudfour.paymentservice.domain.payment.apiclient;

import com.example.cloudfour.paymentservice.commondto.StoreResponseDTO;
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
public class StoreClient {
    
    private final RestTemplate restTemplate;
    
    private static final String STORE_SERVICE_URL = "http://store-service";

    public boolean existsStore(UUID storeId) {
        try {
            String url = STORE_SERVICE_URL + "/stores/" + storeId + "/exists";
            Boolean exists = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("가게 존재 여부 확인 실패: storeId={}", storeId, e);
            throw new PaymentException(PaymentErrorCode.STORE_VALIDATION_FAILED);
        }
    }

    public StoreResponseDTO getStoreById(UUID storeId) {
        try {
            String url = STORE_SERVICE_URL + "/stores/" + storeId;
            StoreResponseDTO store = restTemplate.getForObject(url, StoreResponseDTO.class);
            if (store == null) {
                throw new PaymentException(PaymentErrorCode.STORE_NOT_FOUND);
            }
            return store;
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("가게 정보 조회 실패: storeId={}", storeId, e);
            throw new PaymentException(PaymentErrorCode.STORE_VALIDATION_FAILED);
        }
    }
}

