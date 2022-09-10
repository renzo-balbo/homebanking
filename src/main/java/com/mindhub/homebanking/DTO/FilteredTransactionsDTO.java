package com.mindhub.homebanking.DTO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FilteredTransactionsDTO {
    public LocalDateTime fromDate;
    public LocalDateTime toDate;
    public String accountNumber;

    public FilteredTransactionsDTO() {
    }

    public FilteredTransactionsDTO(LocalDateTime fromDate, LocalDateTime toDate, String accountNumber) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.accountNumber = accountNumber;
    }

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDateTime fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDateTime getToDate() {
        return toDate;
    }

    public void setToDate(LocalDateTime toDate) {
        this.toDate = toDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
