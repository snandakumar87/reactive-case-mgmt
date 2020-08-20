package com.myspace.offermanagement.transactionmodel;

import java.util.Date;

public class Transaction {
  private String txnId;
  private String txnCountry;
  private Double txnAmount;
  private java.util.Date txnTs;
  private String merchantType;
  private String custId;

  public String getCustId() {
    return custId;
  }

  public void setCustId(String custId) {
    this.custId = custId;
  }

  public String getTxnId() {
    return txnId;
  }

  public void setTxnId(String txnId) {
    this.txnId = txnId;
  }

  public String getTxnCountry() {
    return txnCountry;
  }

  public void setTxnCountry(String txnCountry) {
    this.txnCountry = txnCountry;
  }

  public Double getTxnAmount() {
    return txnAmount;
  }

  public void setTxnAmount(Double txnAmount) {
    this.txnAmount = txnAmount;
  }

  public Date getTxnTs() {
    return txnTs;
  }

  public void setTxnTs(Date txnTs) {
    this.txnTs = txnTs;
  }

  public String getMerchantType() {
    return merchantType;
  }

  public void setMerchantType(String merchantType) {
    this.merchantType = merchantType;
  }
}
