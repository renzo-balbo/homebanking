package com.mindhub.homebanking.controllers;



import com.mindhub.homebanking.DTO.ClientDTO;
import com.mindhub.homebanking.emails.VerificationEmail;
import com.mindhub.homebanking.models.Account;
import com.mindhub.homebanking.models.AccountType;
import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.service.AccountService;
import com.mindhub.homebanking.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;



@RestController
public class ClientController {
    @Autowired
    private ClientService clientService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private VerificationEmail verificationEmail;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/api/clients")
    public List<ClientDTO> getClients(){
        return clientService.getClients().stream().map(client -> new ClientDTO(client)).collect(Collectors.toList());}

    @GetMapping("/api/clients/{id}")
    public ClientDTO getClient(@PathVariable Long id){
        return new ClientDTO (clientService.getClientById(id));
    }

    @GetMapping("/api/clients/verify/{id}")
    public ResponseEntity<Object> verifyClient(@PathVariable Long id) {
        Client client = clientService.getClientById(id);
        client.setVerified(true);
        clientService.saveClient(client);
        return new ResponseEntity<>("Your account has been verified!", HttpStatus.CREATED);
    }

    @PostMapping("/api/clients")
    public ResponseEntity<Object>register(
            @RequestParam String firstName, @RequestParam String lastName,
            @RequestParam String email, @RequestParam String password){
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()){
            return new ResponseEntity<>("Missing data, please fill all the form fields.", HttpStatus.FORBIDDEN);
        }
        if (clientService.getClientByEmail(email)!=null){
            return new ResponseEntity<>("This email belongs to an existing client.", HttpStatus.FORBIDDEN);
        }

        Client client = new Client(firstName, lastName, email, passwordEncoder.encode(password));
        clientService.saveClient(client);
        String accountNumber = "VIN-"+getRandomNumber(1,99999999);
        while(accountService.findByAccountNumber(accountNumber) != null){
            accountNumber = "VIN-"+getRandomNumber(1,99999999);
        }
        Account account = new Account(accountNumber, LocalDateTime.now(), 00.00, AccountType.CURRENT_ACCOUNT, client);
        accountService.saveAccount(account);

        String mailSubject = "RLBP Verification Email";
        String mailBody = "Hey! We are so close to complete your account registration, just one more thing to do. We need you to go to this link: localhost:8080/web/verification.html?id="+client.getId();
        clientService.sendVerificationEmail(client.getEmail(),mailSubject,mailBody);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/api/clients/current")
    public ClientDTO getCurrentClient(Authentication authentication){

        return new ClientDTO(clientService.getClientByEmail(authentication.getName()));
    }


    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }




}
