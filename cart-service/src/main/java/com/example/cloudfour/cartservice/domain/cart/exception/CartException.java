package com.example.cloudfour.cartservice.domain.cart.exception;


import com.example.cloudfour.modulecommon.apiPayLoad.code.BaseErrorCode;
import com.example.cloudfour.modulecommon.apiPayLoad.exception.CustomException;

public class CartException extends CustomException {
    public CartException(BaseErrorCode errorCode) {
      super(errorCode);
    }
}
