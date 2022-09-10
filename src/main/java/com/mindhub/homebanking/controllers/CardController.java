package com.mindhub.homebanking.controllers;

import com.mindhub.homebanking.models.*;
import com.mindhub.homebanking.service.CardService;
import com.mindhub.homebanking.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CardController {
        @Autowired
    private ClientService clientService;

    @Autowired
    private CardService cardService;

    @PostMapping("/clients/current/cards")
    public ResponseEntity<Object>createCard(
            @RequestParam CardColor cardColor, @RequestParam CardType cardType,
            Authentication authentication
            ){

        Client client = clientService.getClientByEmail(authentication.getName());
        List<Card> activeCards = client.getCards().stream().filter(card -> card.isActive()==true).collect(Collectors.toList());
        if (activeCards.stream().filter(card -> card.getType().equals(cardType)).count() > 2){
            return new ResponseEntity<>("Clients can only have 3 cards of each type.", HttpStatus.FORBIDDEN);
        }
        if (activeCards.stream().filter(card -> card.getType().equals(cardType)).filter(card -> card.getColor().equals(cardColor)).count()>0){
            return new ResponseEntity<>("You can't have repeated colors for each type of card", HttpStatus.FORBIDDEN);
        }
            int cvvRandomNumber= getRandomNumber(100,999);
            String randomCardNumber = getRandomNumber(1000,9999)+"-"+getRandomNumber(1000,9999)+"-"+getRandomNumber(1000,9999)+"-"+getRandomNumber(1000,9999);
            while(cardService.getCardByNumber(randomCardNumber)!=null){
                randomCardNumber = getRandomNumber(1000,9999)+"-"+getRandomNumber(1000,9999)+"-"+getRandomNumber(1000,9999)+"-"+getRandomNumber(1000,9999);
            }

            Card card = new Card(client.getFirstName()+" "+client.getLastName(),cardColor,cardType,randomCardNumber,cvvRandomNumber, LocalDateTime.now(), LocalDateTime.now().plusYears(5), true, client);
            cardService.saveCard(card);
            return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @PatchMapping("/clients/current/cards")
    public ResponseEntity<Object>disableCard(
            @RequestParam String cardNumber, Authentication authentication){
        Client client = clientService.getClientByEmail(authentication.getName());
        Card card = cardService.getCardByNumber(cardNumber);
        if (cardNumber.isEmpty()){
            return new ResponseEntity<>("Please select a valid card to delete.", HttpStatus.FORBIDDEN);
        }
        if (!client.getCards().contains(card)){
            return  new ResponseEntity<>("You can't delete a card that isn't yours.", HttpStatus.FORBIDDEN);
        }
        card.setActive(false);
        cardService.saveCard(card);
        return new ResponseEntity<>("The card has been successfully disabled.", HttpStatus.CREATED);
    }

    public int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

}
