package com.mindhub.homebanking.DTO;

import com.mindhub.homebanking.models.Account;
import com.mindhub.homebanking.models.ClientLoan;
import com.mindhub.homebanking.models.Loan;

import java.util.List;

public class LoanApplicationDTO {
    private long id;
    private double amount;
    private int payments;
    private String destinyAccountNumber;

    public LoanApplicationDTO() {}

    public LoanApplicationDTO(Loan loan, Account account, ClientLoan clientLoan) {
        this.id = loan.getId();
        this.amount = clientLoan.getAmount();
        this.payments = clientLoan.getPayments();
        this.destinyAccountNumber = account.getNumber();
    }

    public long getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getPayments() {
        return payments;
    }

    public void setPayments(int payments) {
        this.payments = payments;
    }

    public String getDestinyAccountNumber() {
        return destinyAccountNumber;
    }

    public void setDestinyAccountNumber(String destinyAccountNumber) {
        this.destinyAccountNumber = destinyAccountNumber;
    }
}
