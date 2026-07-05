package com.fidelity.leap.merchantportal;

public interface BankTransferClient {
    void transfer(Long merchantId, double amount) throws BankTransferException;
}
