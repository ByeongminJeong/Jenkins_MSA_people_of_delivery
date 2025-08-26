package com.example.cloudfour.storeservice.domain.menu.exception;

import com.example.cloudfour.modulecommon.apiPayLoad.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum StockErrorCode implements BaseErrorCode {
    CREATE_FAILED(HttpStatus.BAD_REQUEST, "STOCK400_1", "재고를 생성할 수 없습니다."),
    ADD_FAILED(HttpStatus.BAD_REQUEST, "STOCK400_2", "재고를 추가할 수 없습니다."),
    MINUS_FAILED(HttpStatus.BAD_REQUEST, "STOCK400_3", "재고를 감소할 수 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "STOCK401", "재고에 접근할 수 있는 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK404", "재고를 찾을 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "STOCK500", "재고 처리 중 서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
