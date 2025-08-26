package com.example.cloudfour.cartservice.domain.order.exception;

import com.example.cloudfour.modulecommon.apiPayLoad.code.BaseErrorCode;
import com.example.cloudfour.modulecommon.apiPayLoad.exception.CustomException;

public class StockException extends CustomException {
    public StockException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
