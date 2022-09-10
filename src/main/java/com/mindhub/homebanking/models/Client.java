package com.mindhub.homebanking.models;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;
    private String firstName, lastName, email;

    @OneToMany(mappedBy = "client", fetch = FetchType.EAGER)
    Set<Account> accounts = new HashSet<>();

    @OneToMany(mappedBy = "client", fetch = FetchType.EAGER)
    Set<ClientLoan> clientLoans = new HashSet<>();

    @OneToMany(mappedBy = "client", fetch = FetchType.EAGER)
    Set<Card> cards = new HashSet<>();

    private String password;

    private boolean verified;

    public Client(){}

    public Client(String first, String last, String mail, Account account, String password, boolean verified){
        this.firstName = first;
        this.lastName = last;
        this.email = mail;
        this.password = password;
        this.addAccount(account);
        this.verified = verified;
    }

    public Client (String first, String last, String mail, String password){
        this.firstName = first;
        this.lastName = last;
        this.email = mail;
        this.password = password;
        this.verified = false;
    }
    public Client (String first, String last, String mail, String password, boolean verified){
        this.firstName = first;
        this.lastName = last;
        this.email = mail;
        this.password = password;
        this.verified = verified;
    }

    public Client(String firstName, String lastName, String email, Set<Account> accounts, Set<ClientLoan> clientLoans, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.accounts = accounts;
        this.clientLoans = clientLoans;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public Set<Account> getAccounts(){return accounts;}
    public void addAccount(Account account){
        account.setClient(this);
        accounts.add(account);
    }

    public Set<ClientLoan> getLoans(){return clientLoans;}
    public void addLoan(ClientLoan clientLoan){
        clientLoan.setClient(this);
        clientLoans.add(clientLoan);
    }

    public Set<Card> getCards() {return cards;}
    public void addCard(Card card){
        card.setClient(this);
        cards.add(card);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
