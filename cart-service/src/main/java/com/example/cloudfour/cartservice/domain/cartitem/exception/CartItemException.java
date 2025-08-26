package com.example.cloudfour.cartservice.domain.cartitem.exception;

import com.example.cloudfour.modulecommon.apiPayLoad.code.BaseErrorCode;
import com.example.cloudfour.modulecommon.apiPayLoad.exception.CustomException;

public class CartItemException extends CustomException {
    public CartItemException(BaseErrorCode errorCode) {
      super(errorCode);
    }
}
