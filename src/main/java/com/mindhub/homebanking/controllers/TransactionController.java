package com.mindhub.homebanking.controllers;


import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
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

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        createTable(transactions, account);


        return new ResponseEntity<>("Done! Check your desktop to find the document with that data.",HttpStatus.CREATED);
    }

    public void createTable(Set<Transaction> transactions, Account account) throws FileNotFoundException, DocumentException {

         Font titleFont = new Font(Font.FontFamily.HELVETICA, 18,
                Font.BOLD);
         Font headerFont = new Font(Font.FontFamily.HELVETICA, 14,
                Font.BOLD, BaseColor.WHITE);

         Font subFont = new Font(Font.FontFamily.HELVETICA, 12,
                Font.NORMAL);
        try {
            Document document = new Document(PageSize.A4);

            String route = System.getProperty("user.home");
            PdfWriter.getInstance(document, new FileOutputStream(route + "/Desktop/Transaction_Report.pdf"));


            document.open();
            document.setMargins(2,2,2,2);



            /*TITLES*/
            Paragraph title = new Paragraph("RLBP Bank - Selected transactions", titleFont);
            title.setSpacingAfter(3);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(-2);

            Paragraph subTitle = new Paragraph("Account number: " + account.getNumber(), subFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            subTitle.setSpacingAfter(1);

            Paragraph date = new Paragraph("Current date: " + LocalDate.now(), subFont);
            date.setSpacingAfter(6);
            date.setAlignment(Element.ALIGN_CENTER);




            /*LOGO*/
            Image img = Image.getInstance("./src/main/resources/static/web/img/logosmall.png");
            img.scaleAbsoluteWidth(75);
            img.scaleAbsoluteHeight(100);
            img.setAlignment(Element.ALIGN_CENTER);

            /*HEADERS*/
            PdfPTable pdfPTable = new PdfPTable(4);
            PdfPCell pdfPCell1 = new PdfPCell(new Paragraph("Description", headerFont));
            PdfPCell pdfPCell2 = new PdfPCell(new Paragraph("Date", headerFont));
            PdfPCell pdfPCell3 = new PdfPCell(new Paragraph("Type", headerFont));
            PdfPCell pdfPCell4 = new PdfPCell(new Paragraph("Amount", headerFont));
            pdfPCell1.setBackgroundColor(new BaseColor(241, 26, 26));
            pdfPCell2.setBackgroundColor(new BaseColor(241, 26, 26));
            pdfPCell3.setBackgroundColor(new BaseColor(241, 26, 26));
            pdfPCell4.setBackgroundColor(new BaseColor(241, 26, 26));
            pdfPCell1.setBorder(0);
            pdfPCell2.setBorder(0);
            pdfPCell3.setBorder(0);
            pdfPCell4.setBorder(0);
            pdfPTable.addCell(pdfPCell1);
            pdfPTable.addCell(pdfPCell2);
            pdfPTable.addCell(pdfPCell3);
            pdfPTable.addCell(pdfPCell4);

            /*TABLE OF TRANSACTIONS*/
            transactions.forEach(transaction -> {

                PdfPCell pdfPCell5 = new PdfPCell(new Paragraph(transaction.getDescription(), subFont));
                PdfPCell pdfPCell6 = new PdfPCell(new Paragraph(transaction.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), subFont));
                PdfPCell pdfPCell7 = new PdfPCell(new Paragraph(String.valueOf(transaction.getType()), subFont));
                PdfPCell pdfPCell8 = new PdfPCell(new Paragraph("$" + String.valueOf(transaction.getAmount()), subFont));
                pdfPCell5.setBorder(1);
                pdfPCell6.setBorder(1);
                pdfPCell7.setBorder(1);
                pdfPCell8.setBorder(1);

                pdfPTable.addCell(pdfPCell5);
                pdfPTable.addCell(pdfPCell6);
                pdfPTable.addCell(pdfPCell7);
                pdfPTable.addCell(pdfPCell8);
            });

            document.add(img);
            document.add(title);
            document.add(subTitle);
            document.add(date);
            document.add(pdfPTable);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
