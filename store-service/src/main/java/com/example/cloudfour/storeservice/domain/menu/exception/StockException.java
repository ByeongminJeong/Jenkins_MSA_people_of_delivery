package com.example.cloudfour.storeservice.domain.menu.exception;

import com.example.cloudfour.modulecommon.apiPayLoad.code.BaseErrorCode;
import com.example.cloudfour.modulecommon.apiPayLoad.exception.CustomException;

public class StockException extends CustomException {
    public StockException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
