package com.valpiok.NaviPark.payment.model;

/**
 * Created by SERHIO on 14.09.2017.
 */

import java.io.Serializable;

public class TransactionDetails implements Serializable {

    private String merchantId;
    private int amount;
    private String currency;
    private String referenceNumber;
    private String sign;
    private String status;
    private String paymentMethod;

    // TODO - builder
    public TransactionDetails(String merchantId, int amount, String currency, String referenceNumber, String paymentMethod, String sign) {
        // this.merchantId = merchantId;
        this.merchantId = "1100007091";
        this.amount = amount;
        // this.currency = currency;
        this.currency = "CHF";
        // this.referenceNumber = referenceNumber;
        this.referenceNumber = "LOL2017";
        this.paymentMethod = paymentMethod;
        // this.sign = sign;
        this.sign = "170922110407948062";
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRefrenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionDetails that = (TransactionDetails) o;

        if (amount != that.amount) return false;
        if (merchantId != null ? !merchantId.equals(that.merchantId) : that.merchantId != null)
            return false;
        if (currency != null ? !currency.equals(that.currency) : that.currency != null)
            return false;
        if (referenceNumber != null ? !referenceNumber.equals(that.referenceNumber) : that.referenceNumber != null)
            return false;
        if (sign != null ? !sign.equals(that.sign) : that.sign != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        return !(paymentMethod != null ? !paymentMethod.equals(that.paymentMethod) : that.paymentMethod != null);

    }

    @Override
    public int hashCode() {
        int result = merchantId != null ? merchantId.hashCode() : 0;
        result = 31 * result + amount;
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (referenceNumber != null ? referenceNumber.hashCode() : 0);
        result = 31 * result + (sign != null ? sign.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (paymentMethod != null ? paymentMethod.hashCode() : 0);
        return result;
    }
}