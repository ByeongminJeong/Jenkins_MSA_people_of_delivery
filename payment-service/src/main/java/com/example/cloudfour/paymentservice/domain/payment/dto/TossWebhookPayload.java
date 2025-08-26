package com.example.cloudfour.paymentservice.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossWebhookPayload {
    
    @JsonProperty("paymentKey")
    private String paymentKey;
    
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("totalAmount")
    private Integer totalAmount;
    
    @JsonProperty("method")
    private String method;
    
    @JsonProperty("approvedAt")
    private String approvedAt;
    
    @JsonProperty("useEscrow")
    private Boolean useEscrow;
    
    @JsonProperty("card")
    private CardInfo card;
    
    @JsonProperty("virtualAccount")
    private VirtualAccountInfo virtualAccount;
    
    @JsonProperty("transfer")
    private TransferInfo transfer;
    
    @JsonProperty("mobilePhone")
    private MobilePhoneInfo mobilePhone;
    
    @JsonProperty("giftCertificate")
    private GiftCertificateInfo giftCertificate;
    
    @JsonProperty("cashReceipt")
    private CashReceiptInfo cashReceipt;
    
    @JsonProperty("discount")
    private DiscountInfo discount;
    
    @JsonProperty("cancels")
    private CancelInfo[] cancels;
    
    @JsonProperty("receiptUrl")
    private String receiptUrl;
    
    @JsonProperty("checkout")
    private CheckoutInfo checkout;
    
    @JsonProperty("easyPay")
    private EasyPayInfo easyPay;
    
    @JsonProperty("country")
    private String country;
    
    @JsonProperty("failure")
    private FailureInfo failure;
    
    @JsonProperty("cashReceipts")
    private CashReceiptInfo[] cashReceipts;
    
    @JsonProperty("cashReceipt")
    private CashReceiptInfo cashReceipt2;
    
    @JsonProperty("discount")
    private DiscountInfo discount2;
    
    @JsonProperty("cancels")
    private CancelInfo[] cancels2;
    
    @JsonProperty("receiptUrl")
    private String receiptUrl2;
    
    @JsonProperty("checkout")
    private CheckoutInfo checkout2;
    
    @JsonProperty("easyPay")
    private EasyPayInfo easyPay2;
    
    @JsonProperty("country")
    private String country2;
    
    @JsonProperty("failure")
    private FailureInfo failure2;
    
    @JsonProperty("cashReceipts")
    private CashReceiptInfo[] cashReceipts2;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardInfo {
        private String company;
        private String number;
        private String installmentPlanMonths;
        private Boolean isInterestFree;
        private String approveNo;
        private String useCardPoint;
        private String cardType;
        private String ownerType;
        private String acquireStatus;
        private String amount;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VirtualAccountInfo {
        private String accountNumber;
        private String accountType;
        private String bankCode;
        private String customerName;
        private String dueDate;
        private String refundStatus;
        private String expired;
        private String settlementStatus;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransferInfo {
        private String bankCode;
        private String settlementStatus;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MobilePhoneInfo {
        private String customerMobilePhone;
        private String settlementStatus;
        private String receiptUrl;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GiftCertificateInfo {
        private String approveNo;
        private String settlementStatus;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashReceiptInfo {
        private String type;
        private String amount;
        private String taxFreeAmount;
        private String issueNumber;
        private String receiptUrl;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscountInfo {
        private String amount;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelInfo {
        private String cancelAmount;
        private String cancelReason;
        private String taxFreeAmount;
        private String taxAmount;
        private String refundableAmount;
        private String canceledAt;
        private String transactionKey;
        private String receiptKey;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutInfo {
        private String url;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EasyPayInfo {
        private String provider;
        private String amount;
        private String discountAmount;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailureInfo {
        private String code;
        private String message;
    }
}