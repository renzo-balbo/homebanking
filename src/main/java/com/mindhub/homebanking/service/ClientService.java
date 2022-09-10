package com.mindhub.homebanking.service;

import com.mindhub.homebanking.models.Client;

import java.util.List;

public interface ClientService {
    public List<Client> getClients();

    public Client getClientById(Long id);

    public Client getClientByEmail(String email);

    public void saveClient(Client client);

    public void sendVerificationEmail(String toEmail, String subject, String body);
}
