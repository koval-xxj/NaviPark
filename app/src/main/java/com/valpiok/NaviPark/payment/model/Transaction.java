package com.valpiok.NaviPark.payment.model;

/**
 * Created by SERHIO on 14.09.2017.
 */

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Transaction {

    private long merchantId;
    private String referenceNumber;
    private String currency;
    private long amount;
    private String status;
    private long timestamp;

    public long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReference_number(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getFormattedAmount() {
        return Transaction.getFormattedAmount(amount);
    }

    public static String getFormattedAmount(long amount) {
        StringBuilder formattedAmount = new StringBuilder(String.valueOf(amount));

        if (formattedAmount.length() == 1) {
            formattedAmount.insert(0, "0.0");
        } else if (formattedAmount.length() == 2) {
            formattedAmount.insert(0, "0.");
        } else {
            formattedAmount.insert(formattedAmount.length() - 2, ".");
        }

        return formattedAmount.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedDate() {

        Calendar now = Calendar.getInstance();
        Calendar timeToCheck = Calendar.getInstance();
        timeToCheck.setTimeInMillis(timestamp);

        if (now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR)) {

            if (now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR)) {
                return new SimpleDateFormat("HH:mm").format(timestamp);
            }

            return new SimpleDateFormat("MMM dd").format(timestamp);
        }

        return new SimpleDateFormat("MM/dd/yyyy").format(timestamp);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Transaction{");
        sb.append("merchantId=").append(merchantId);
        sb.append(", referenceNumber='").append(referenceNumber).append('\'');
        sb.append(", currency='").append(currency).append('\'');
        sb.append(", amount=").append(amount);
        sb.append(", status='").append(status).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append('}');
        return sb.toString();
    }

}