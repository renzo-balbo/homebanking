package com.mindhub.homebanking;

import com.mindhub.homebanking.models.*;
import com.mindhub.homebanking.repositories.*;
import net.minidev.json.writer.ArraysMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.mindhub.homebanking.models.CardColor.*;
import static com.mindhub.homebanking.models.TransactionType.*;

@SpringBootApplication
public class HomebankingApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomebankingApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(ClientRepository clientRepository, AccountRepository accountRepository, TransactionRepository transactionRepository, LoanRepository loanRepository, ClientLoanRepository clientLoanRepository, CardRepository cardRepository) {
		return (args) -> {

			Account accountMelba = new Account("VIN001", LocalDateTime.now(), 5000.00);
			Account accountMelba2 = new Account("VIN002", LocalDateTime.now().plusDays(1), 7500.00);
			Account accountRenzo1 = new Account("VIN003", LocalDateTime.now(), 420.00);
			Client clientMelba = new Client("Melba", "Morel", "melbitalaturraka@hotmail.uk", accountMelba, passwordEncoder.encode("clave123"));
			Client clientRenzo = new Client("Renzo", "Balbo", "renzo.balbo@outlook.com.ar",accountRenzo1, passwordEncoder.encode("clave123"));
			Client admin = new Client("admin", "admin", "admin@rlbp.com.ar", passwordEncoder.encode("admin"));
			clientMelba.addAccount(accountMelba2);
			clientRepository.save(clientMelba);
			accountRepository.save(accountMelba);
			accountRepository.save(accountMelba2);
			clientRepository.save(clientRenzo);
			accountRepository.save(accountRenzo1);
			clientRepository.save(admin);
			Transaction transactionTest1 = new Transaction(CREDIT, 200.00, "Testing transaction 1", LocalDateTime.now(), accountMelba);
			Transaction transactionTest2 = new Transaction(DEBIT, -420.00, "Testing Transaction 2", LocalDateTime.now(), accountMelba2);
			Transaction transactionTest3 = new Transaction(CREDIT, 1234.00, "Testing Transaction 3", LocalDateTime.now().plusDays(2),accountRenzo1);
			Transaction transactionTest4 = new Transaction(CREDIT, 357.00, "Testing Transaction 4", LocalDateTime.now());
			Transaction transactionTest5 = new Transaction (DEBIT, -99.99, "Testing Transaction 5", LocalDateTime.now(), accountMelba);
			Transaction transactionTest6 = new Transaction (DEBIT, -700.00, "Testing Transaction 6", LocalDateTime.now(), accountMelba);
			Transaction transactionTest7 = new Transaction (DEBIT, -3.50, "Testing Transaction 7", LocalDateTime.now(), accountMelba2);

			accountRenzo1.addTransaction(transactionTest4);

			Loan personalLoan = new Loan("Personal Loan", 100000.00, Arrays.asList(6, 12, 24, 36));
			Loan mortgageLoan = new Loan ("Mortgage Loan", 500000.00, Arrays.asList(12, 24, 36, 48, 60));
			Loan automobileLoan = new Loan ("Automobile Loan", 300000.00, Arrays.asList(6, 12, 24, 36));
			ClientLoan prestamoMelba = new ClientLoan(400000.00, 60, clientMelba, mortgageLoan);
			ClientLoan prestamoMelba2 = new ClientLoan(50000.00, 12, clientMelba, personalLoan);
			ClientLoan prestamoRenzo = new ClientLoan(100000.00, 24, clientRenzo, personalLoan);
			ClientLoan prestamoRenzo2 = new ClientLoan(200000.00, 36, clientRenzo, automobileLoan);


			loanRepository.save(personalLoan);
			loanRepository.save(automobileLoan);
			loanRepository.save(mortgageLoan);
			clientLoanRepository.save(prestamoMelba);
			clientLoanRepository.save(prestamoMelba2);
			clientLoanRepository.save(prestamoRenzo);
			clientLoanRepository.save(prestamoRenzo2);
			transactionRepository.save(transactionTest1);
			transactionRepository.save(transactionTest2);
			transactionRepository.save(transactionTest3);
			transactionRepository.save(transactionTest4);
			transactionRepository.save(transactionTest5);
			transactionRepository.save(transactionTest6);
			transactionRepository.save(transactionTest7);

			Card cardMelba = new Card("Melba Morel", GOLD, CardType.DEBIT, "4045-5000-6891-1023", 792, LocalDateTime.now(), LocalDateTime.now().plusYears(5), clientMelba);
			Card cardMelba2 = new Card("Melba Morel", TITANIUM, CardType.CREDIT, "4045-7000-5561-0032", 635, LocalDateTime.now(), LocalDateTime.now().plusYears(5), clientMelba);
			Card cardRenzo = new Card("Renzo Balbo", SILVER, CardType.CREDIT, "4045-5000-6983-1170", 304, LocalDateTime.now(), LocalDateTime.now().plusYears(5), clientRenzo);

			cardRepository.save(cardMelba);
			cardRepository.save(cardMelba2);
			cardRepository.save(cardRenzo);
		};
	}

	@Autowired
	private PasswordEncoder passwordEncoder;


}
