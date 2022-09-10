package com.mindhub.homebanking.service;

import com.mindhub.homebanking.models.Account;
import org.springframework.stereotype.Service;

import java.util.List;


public interface AccountService {
    public List<Account> getAllAccounts();

    public Account findAccountById(Long id);
    public Account findByAccountNumber(String accountNumber);

    public void saveAccount(Account account);
}
