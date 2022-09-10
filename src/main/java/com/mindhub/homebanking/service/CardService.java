package com.mindhub.homebanking.service;

import com.mindhub.homebanking.models.Card;

public interface CardService {
    public Card getCardByNumber(String number);

    public void saveCard(Card card);
}
