package com.example.cloudfour.cartservice.domain.order.exception;


import com.example.cloudfour.modulecommon.apiPayLoad.code.BaseErrorCode;
import com.example.cloudfour.modulecommon.apiPayLoad.exception.CustomException;

public class OrderException extends CustomException {
    public OrderException(BaseErrorCode errorCode) {
      super(errorCode);
    }
}
