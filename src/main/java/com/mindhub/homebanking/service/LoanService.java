package com.mindhub.homebanking.service;

import com.mindhub.homebanking.models.Loan;

import java.util.List;

public interface LoanService {
    public List<Loan> getAllLoans();

    public Loan getLoanById(Long id);
}
