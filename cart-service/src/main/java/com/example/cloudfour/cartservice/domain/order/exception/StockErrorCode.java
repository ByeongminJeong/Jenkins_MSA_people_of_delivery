package com.example.cloudfour.cartservice.domain.order.exception;

import com.example.cloudfour.modulecommon.apiPayLoad.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum StockErrorCode implements BaseErrorCode {
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "STOCK400_1", "재고가 부족합니다."),
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK404_1", "재고 정보를 찾을 수 없습니다."),
    STOCK_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STOCK500_1", "재고 업데이트에 실패했습니다."),
    STOCK_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "STOCK400_2", "재고 검증에 실패했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
