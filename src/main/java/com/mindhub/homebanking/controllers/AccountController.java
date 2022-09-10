package com.mindhub.homebanking.controllers;


import com.mindhub.homebanking.DTO.AccountDTO;
import com.mindhub.homebanking.models.Account;
import com.mindhub.homebanking.models.AccountType;
import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.models.Transaction;
import com.mindhub.homebanking.repositories.AccountRepository;
import com.mindhub.homebanking.repositories.ClientRepository;
import com.mindhub.homebanking.service.AccountService;
import com.mindhub.homebanking.service.ClientService;
import com.mindhub.homebanking.service.TransactionService;
import com.sun.source.tree.DoWhileLoopTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AccountController {
    @Autowired
    private ClientService clientService;

    @Autowired
    private AccountService accountService;
    @Autowired
    private TransactionService transactionService;

    @GetMapping("/accounts")
    public List<AccountDTO> getAccounts(){
        return accountService.getAllAccounts().stream().map(account -> new AccountDTO(account)).collect(Collectors.toList());
    }

    @GetMapping("/accounts/{id}")
    public AccountDTO getAccount(@PathVariable Long id){
        return new AccountDTO (accountService.findAccountById(id));
    }

    @PostMapping("/clients/current/accounts")
    public ResponseEntity<Object>createAccount(
            @RequestParam AccountType accountType,
            Authentication authentication){
        Client client = clientService.getClientByEmail(authentication.getName());
        List <Account> activeAccounts = client.getAccounts().stream().filter(account -> account.isActive()).collect(Collectors.toList());
        if (activeAccounts.stream().filter(account -> account.getAccountType() == accountType).count()>0){
            return new ResponseEntity<>("You already have an account of this type.", HttpStatus.FORBIDDEN);
        }
        if (activeAccounts.toArray().length > 2){
            return new ResponseEntity<>("Clients can only have 3 accounts.", HttpStatus.FORBIDDEN);
        } else {

                String accountNumber = "VIN-"+getRandomNumber(0,99999999);
                while(accountService.findByAccountNumber(accountNumber) != null){
                    accountNumber = "VIN-"+getRandomNumber(1,99999999);
                }

            Account account = new Account(accountNumber, LocalDateTime.now(), 00.00, accountType, client);
            accountService.saveAccount(account);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
    }

    @PatchMapping("/clients/current/accounts")
    public ResponseEntity<Object> disableAccount(
            @RequestParam String accountNumber, Authentication authentication){
        Client client = clientService.getClientByEmail(authentication.getName());
        List <Account> activeAccounts = client.getAccounts().stream().filter(account -> account.isActive()).collect(Collectors.toList());
        Account account = accountService.findByAccountNumber(accountNumber);
        if(accountNumber.isEmpty()){
            return new ResponseEntity<>("Please specify the account that you want to disable.", HttpStatus.FORBIDDEN);
        }
        if (!activeAccounts.contains(account)){
            return new ResponseEntity<>("You can't disable an account that isn't yours.", HttpStatus.FORBIDDEN);
        }
        if (account==null){
            return new ResponseEntity<>("This account doesn't exist.", HttpStatus.FORBIDDEN);
        }
        if(account.getBalance()>0){
            return new ResponseEntity<>("You can't disable an account that has founds, please transfer or withdraw them in order to proceed.", HttpStatus.FORBIDDEN);
        }
        if(client.getAccounts().stream().count()<2){
            return new ResponseEntity<>("Every client must have 1 active account at all times, for that reason we cannot disable this one.",HttpStatus.FORBIDDEN);
        }
        account.setActive(false);
        transactionService.deleteTransactionList(account.getTransactions().stream().collect(Collectors.toList()));
        account.setTransactions(null);
        accountService.saveAccount(account);
        return new ResponseEntity<>("The account "+accountNumber+" has been successfully disabled.", HttpStatus.CREATED);
    }


    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }




}
