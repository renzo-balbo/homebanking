package com.mindhub.homebanking.controllers;


import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.mindhub.homebanking.DTO.FilteredTransactionsDTO;
import com.mindhub.homebanking.models.Account;
import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.models.Transaction;
import com.mindhub.homebanking.models.TransactionType;
import com.mindhub.homebanking.service.AccountService;
import com.mindhub.homebanking.service.ClientService;
import com.mindhub.homebanking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;


@RestController
@RequestMapping("/api")
public class TransactionController {
    @Autowired
    private ClientService clientService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Transactional
    @PostMapping("/transactions")
    public ResponseEntity<Object> makeTransaction(
            @RequestParam double amount, @RequestParam String description,
            @RequestParam String originAccountNumber, @RequestParam String destinyAccountNumber,
            Authentication authentication
    ){
        Client client = clientService.getClientByEmail(authentication.getName());

        if (description.isEmpty() || originAccountNumber.isEmpty() || destinyAccountNumber.isEmpty() || amount<=0){
            return new ResponseEntity<>("Missing data, please fill all the fields.", HttpStatus.FORBIDDEN);
        }
        if (originAccountNumber.equals(destinyAccountNumber)){
            return new ResponseEntity<>("You can't make a transaction from an account to itself.", HttpStatus.FORBIDDEN);
        }
        if(accountService.findByAccountNumber(originAccountNumber)==null || !accountService.findByAccountNumber(originAccountNumber).isActive()){
            return new ResponseEntity<>("The account from which you want to make the transaction does not exist.", HttpStatus.FORBIDDEN);
        }
        Account originAccount = accountService.findByAccountNumber(originAccountNumber);

        if (!client.getAccounts().contains(originAccount)){
            return new ResponseEntity<>("You can't make a transaction from an account that isn't yours.", HttpStatus.FORBIDDEN);
        }
        if (accountService.findByAccountNumber(destinyAccountNumber)==null || !accountService.findByAccountNumber(originAccountNumber).isActive()){
            return new ResponseEntity<>("The account to which you want to transfer does not exist.", HttpStatus.FORBIDDEN);
        }
        Account destinyAccount = accountService.findByAccountNumber(destinyAccountNumber);
        if (originAccount.getBalance()<amount){
            return new ResponseEntity<>("You don't have the required founds to make this transaction.", HttpStatus.FORBIDDEN);
        }
        Transaction debitTransaction = new Transaction(TransactionType.DEBIT, -amount, description, LocalDateTime.now(), originAccount);
        Transaction creditTransaction = new Transaction(TransactionType.CREDIT,amount,description,LocalDateTime.now(),destinyAccount);
        transactionService.saveTransaction(debitTransaction);
        transactionService.saveTransaction(creditTransaction);
        originAccount.setBalance(originAccount.getBalance()-amount);
        destinyAccount.setBalance(destinyAccount.getBalance()+amount);
        accountService.saveAccount(originAccount);
        accountService.saveAccount(destinyAccount);

        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @PostMapping("/transactions/filtered")
    public ResponseEntity<Object> getFilteredTransaction(
            @RequestBody FilteredTransactionsDTO filteredTransactionsDTO, Authentication authentication) throws FileNotFoundException, DocumentException {
        Client client = clientService.getClientByEmail(authentication.getName());
        Account account = accountService.findByAccountNumber(filteredTransactionsDTO.getAccountNumber());
        if(filteredTransactionsDTO.accountNumber.isEmpty() || filteredTransactionsDTO.fromDate==null || filteredTransactionsDTO.toDate==null){
            return new ResponseEntity<>("Please fill all the form fields.", HttpStatus.FORBIDDEN);
        }
        if(!client.getAccounts().contains(account)){
            return new ResponseEntity<>("You cannot request data from an account that isn't yours.", HttpStatus.FORBIDDEN);
        }
        if (account.getTransactions()==null){
            return new ResponseEntity<>("You don't have any transactions in this account.", HttpStatus.FORBIDDEN);
        }
        if (!account.isActive()){
            return new ResponseEntity<>("This account doesn't exist.", HttpStatus.FORBIDDEN);
        }

        Set<Transaction> transactions = transactionService.filterTransactionsWithDate(filteredTransactionsDTO.fromDate, filteredTransactionsDTO.toDate, account);

        if(transactions.isEmpty()){
            return new ResponseEntity<>("You don't have any transaction between those dates.", HttpStatus.FORBIDDEN);
        }

        createTable(transactions);

        return new ResponseEntity<>("Done! Check your desktop to find the document with that data.",HttpStatus.CREATED);
    }

    public void createTable(Set<Transaction> transactionArray) throws FileNotFoundException, DocumentException {

        var document = new Document();
        String route = System.getProperty("user.home");
        PdfWriter.getInstance(document, new FileOutputStream(route + "/Desktop/Transaction_Report.pdf"));

        document.open();

        var bold = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        var paragraph = new Paragraph("Transactions that occurred between the selected dates:");

        var table = new PdfPTable(4);
        Stream.of("Amount","Description","Date","Type").forEach(table::addCell);

        transactionArray
                .forEach(transaction -> {
                    table.addCell("$"+transaction.getAmount());
                    table.addCell(transaction.getDescription());
                    table.addCell(transaction.getDate().toString());
                    table.addCell(transaction.getType().toString());

                });

        paragraph.add(table);
        document.add(paragraph);
        document.close();
    }



}
