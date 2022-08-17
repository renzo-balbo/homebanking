package com.mindhub.homebanking.controllers;


import com.mindhub.homebanking.DTO.AccountDTO;
import com.mindhub.homebanking.models.Account;
import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.repositories.AccountRepository;
import com.mindhub.homebanking.repositories.ClientRepository;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ClientRepository clientRepository;

    @RequestMapping("/accounts")
    public List<AccountDTO> getAccounts(){
        return accountRepository.findAll().stream().map(account -> new AccountDTO(account)).collect(Collectors.toList());
    }

    @RequestMapping("/accounts/{id}")
    public AccountDTO getAccount(@PathVariable Long id){
        return accountRepository.findById(id).map(account -> new AccountDTO(account)).orElse(null);
    }

    @RequestMapping(path = "/clients/current/accounts", method = RequestMethod.POST)
    public ResponseEntity<Object>createAccount(
            //@RequestParam String clientEmail,
            Authentication authentication){
        Client client = clientRepository.findByEmail(authentication.getName());
        if (client.getAccounts().toArray().length > 2){
            return new ResponseEntity<>("Clients can only have 3 accounts.", HttpStatus.FORBIDDEN);
        } else {
            Random vinNumber = new Random();
                vinNumber.nextInt(100000000);
                String accountNumber = "VIN-"+vinNumber.nextInt(100000000);
                while(accountRepository.findByNumber(accountNumber) != null){
                    accountNumber = "VIN-"+vinNumber.nextInt(100000000);
                }

            Account account = new Account(accountNumber, LocalDateTime.now(), 00.00, client);
            accountRepository.save(account);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
    }




}
