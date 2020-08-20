package com.myspace.offermanagement.validation;

public class FraudValidationPayload {

    private String txnId;
    private boolean valid;
    private String validationPayload;

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getValidationPayload() {
        return validationPayload;
    }

    public void setValidationPayload(String validationPayload) {
        this.validationPayload = validationPayload;
    }
}
