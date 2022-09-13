package com.mindhub.homebanking;

import com.mindhub.homebanking.models.Client;
import com.mindhub.homebanking.models.Loan;
import com.mindhub.homebanking.repositories.ClientRepository;
import com.mindhub.homebanking.repositories.LoanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.util.List;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ClientRepositoriesTest {

    @Autowired
    private ClientRepository clientRepository;

    @Test
    public  void existLoans(){
        List<Client> client = clientRepository.findAll();
        assertThat(client,is(not(empty())));
    }

    @Test
    public void existPersonalLoan(){
        List<Client> client = clientRepository.findAll();
        assertThat(client, hasItem(hasProperty("firstName",is("Melba"))));
    }

}