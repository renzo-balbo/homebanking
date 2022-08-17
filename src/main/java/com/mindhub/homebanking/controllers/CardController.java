package com.mindhub.homebanking.controllers;

import com.mindhub.homebanking.DTO.ClientDTO;
import com.mindhub.homebanking.models.*;
import com.mindhub.homebanking.repositories.AccountRepository;
import com.mindhub.homebanking.repositories.CardRepository;
import com.mindhub.homebanking.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class CardController {



    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CardRepository cardRepository;

    @RequestMapping(value = "/clients/current/cards", method = RequestMethod.POST)
    public ResponseEntity<Object>createCard(
            @RequestParam CardColor cardColor, @RequestParam CardType cardType,
            Authentication authentication
            ){

        Client client = clientRepository.findByEmail(authentication.getName());
        if (client.getCards().stream().filter(card -> card.getType().equals(cardType)).count() > 2){
            return new ResponseEntity<>("Clients can only have 3 cards of each type.", HttpStatus.FORBIDDEN);
        } else {
            int cvvMin =100;
            int cvvMax=999;
           int cvvRandomNumber = Math.round(Math.round(Math.random()*(cvvMax-cvvMin)+cvvMin));

           int minCardNumber=1000;
           int maxCardNumber=9999;
           String randomCardNumber=Math.round(Math.round(Math.random()*(maxCardNumber-minCardNumber)+minCardNumber))+"-"+Math.round(Math.round(Math.random()*(maxCardNumber-minCardNumber)+minCardNumber))+"-"+Math.round(Math.round(Math.random()*(maxCardNumber-minCardNumber)+minCardNumber))+"-"+Math.round(Math.round(Math.random()*(maxCardNumber-minCardNumber)+minCardNumber));
            while(cardRepository.findByNumber(randomCardNumber)!=null){
                randomCardNumber=Math.round(Math.round(Math.random()*(maxCardNumber-minCardNumber)+minCardNumber))+"-"+Math.round(Math.round(Math.random()*(maxCardNumber-minCardNumber)+minCardNumber))+"-"+Math.round(Math.round(Math.random()*(maxCardNumber-minCardNumber)+minCardNumber))+"-"+Math.round(Math.round(Math.random()*(maxCardNumber-minCardNumber)+minCardNumber));
            };

            Card card = new Card(client.getFirstName()+" "+client.getLastName(),cardColor,cardType,randomCardNumber,cvvRandomNumber, LocalDateTime.now(), LocalDateTime.now().plusYears(5),client);
            cardRepository.save(card);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
    }


}
