package com.example.cloudfour.cartservice.domain.order.exception;

import com.example.cloudfour.modulecommon.apiPayLoad.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum OrderErrorCode implements BaseErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "ORDER400_0", "잘못된 입력값입니다."),
    CREATE_FAILED(HttpStatus.BAD_REQUEST, "ORDER400_1", "주문 정보를 생성할 수 없습니다."),
    UPDATE_FAILED(HttpStatus.BAD_REQUEST, "ORDER400_2", "주문 정보를 수정할 수 없습니다."),
    DELETE_FAILED(HttpStatus.BAD_REQUEST, "ORDER400_3", "주문 정보를 삭제할 수 없습니다."),
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "ORDER401", "주문에 접근할 수 있는 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER404", "주문을 찾을 수 없습니다."),
    ALREADY_ADD(HttpStatus.CONFLICT, "ORDER409", "이미 등록된 주문입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ORDER500", "주문 처리 중 서버 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
