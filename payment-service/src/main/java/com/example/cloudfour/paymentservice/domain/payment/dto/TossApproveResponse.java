package com.example.cloudfour.paymentservice.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossApproveResponse {

    private String paymentKey;
    private String orderId;
    private String status;
    private String requestedAt;
    private String approvedAt;
    private Integer totalAmount;
    private String method;
    private String version;

    private Card card;

    @Getter
    @Builder
    public static class Card {
        private String company;
        private String number;
        private int installmentPlanMonths;
        private String approveNo;
        private boolean isInterestFree;
        private String cardType;
        private String ownerType;
    }
}
