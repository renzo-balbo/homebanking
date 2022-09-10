package com.mindhub.homebanking.controllers;

import com.mindhub.homebanking.DTO.LoanApplicationDTO;
import com.mindhub.homebanking.DTO.LoanDTO;
import com.mindhub.homebanking.models.*;
import com.mindhub.homebanking.repositories.*;
import com.mindhub.homebanking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class LoanController {
    @Autowired
    private ClientService clientService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ClientLoanService clientLoanService;


    @GetMapping("/loans")
    public Set<LoanDTO> getLoans() {
        return loanService.getAllLoans().stream().map(loan -> new LoanDTO(loan)).collect(Collectors.toSet());
    }

    @Transactional
    @PostMapping("/loans")
    public ResponseEntity<Object> createNewLoan(
            @RequestBody LoanApplicationDTO requestedLoan, Authentication authentication) {
        //INSTANCIO DENTRO DEL METODO QUIEN VA A SER MI CLIENTE, A QUE CUENTA VOY A TRANSFERIR Y CON QUE PRESTAMO VOY A TRABAJAR
        Client client = clientService.getClientByEmail(authentication.getName());
        Account account = accountService.findByAccountNumber(requestedLoan.getDestinyAccountNumber());
        if(!loanService.getAllLoans().stream().map(loan -> loan.getId()).collect(Collectors.toList()).contains(requestedLoan.getId())){
            return new ResponseEntity<>("This is not a valid type of loan.", HttpStatus.FORBIDDEN);
        }
        Loan loan = loanService.getLoanById(requestedLoan.getId());
        if (requestedLoan.getAmount() <= 0 || requestedLoan.getDestinyAccountNumber().isEmpty() || !loan.getPayments().contains(requestedLoan.getPayments())) {
            return new ResponseEntity<>("Please fill all the fields with valid information.", HttpStatus.FORBIDDEN);
        }
        if (requestedLoan.getAmount() > loan.getMaxAmount()) {
            return new ResponseEntity<>("The amount that you asked for exceeds que max amount that we can lend in the selected type of loan", HttpStatus.FORBIDDEN);
        }
        if (!loan.getPayments().contains(requestedLoan.getPayments())) {
            return new ResponseEntity<>("Please select a valid quantity of payments for that type of loan.", HttpStatus.FORBIDDEN);
        }
        if (account == null || !accountService.findByAccountNumber(requestedLoan.getDestinyAccountNumber()).isActive()) {
            return new ResponseEntity<>("The account that you selected to deposit the founds doesn't exist.", HttpStatus.FORBIDDEN);
        }
        if (!client.getAccounts().contains(account)) {
            return new ResponseEntity<>("The account that you selected to deposit the founds doesn't belongs to you.", HttpStatus.FORBIDDEN);
        }
        // CHEQUEO QUE NO TENGA UN PRESTAMO YA DE ESE TIPO
        if (client.getLoans().stream().filter(clientLoan -> clientLoan.getLoan()==loan).count()>0){
            return new ResponseEntity<>("You already have an active "+loan.getName(), HttpStatus.FORBIDDEN);
        }

        ClientLoan clientLoan = new ClientLoan(requestedLoan.getAmount(), requestedLoan.getPayments(), client, loan);

        // APLICANDO INTERESES
        switch (loan.getName()) {
            case "Personal Loan":
                switch (clientLoan.getPayments()) {
                    case 6:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.20);
                        break;
                    case 12:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.22);
                        break;
                    case 24:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.25);
                        break;
                    case 36:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.30);
                        break;
                    default:
                        break;
                }
                break;
            case "Automobile Loan":
                switch (clientLoan.getPayments()) {
                    case 6:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.20);
                        break;
                    case 12:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.23);
                        break;
                    case 24:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.27);
                        break;
                    case 36:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.32);
                        break;
                    default:
                        break;
                }
                break;
            case "Mortgage Loan":
                switch (clientLoan.getPayments()) {
                    case 12:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.20);
                        break;
                    case 24:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.25);
                        break;
                    case 36:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.30);
                        break;
                    case 48:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.35);
                        break;
                    case 60:
                        clientLoan.setAmount(requestedLoan.getAmount() * 1.40);
                    default:
                        break;
                }
                break;
            default:
                break;
        }


        clientLoanService.saveClientLoan(clientLoan);
        Transaction transaction = new Transaction(TransactionType.CREDIT, requestedLoan.getAmount(), loan.getName() + " approved", LocalDateTime.now(), account);
        transactionService.saveTransaction(transaction);
        account.setBalance(account.getBalance() + requestedLoan.getAmount());
        accountService.saveAccount(account);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}










