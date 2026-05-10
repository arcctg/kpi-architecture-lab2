package com.flashcard.application.usecase.card;

import com.flashcard.application.dto.CardResult;
import com.flashcard.domain.error.AccessDeniedError;
import com.flashcard.domain.error.EntityNotFoundError;
import com.flashcard.domain.factory.CardFactory;
import com.flashcard.domain.model.Card;
import com.flashcard.domain.model.Deck;
import com.flashcard.domain.model.User;
import com.flashcard.domain.repository.CardRepository;
import com.flashcard.domain.repository.DeckRepository;
import com.flashcard.domain.repository.UserRepository;
import com.flashcard.domain.valueobject.CardDefinition;
import com.flashcard.domain.valueobject.CardTerm;
import com.flashcard.domain.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddCardUseCase {

    private final CardFactory cardFactory;
    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;

    public AddCardUseCase(CardFactory cardFactory,
                          CardRepository cardRepository,
                          DeckRepository deckRepository,
                          UserRepository userRepository) {
        this.cardFactory = cardFactory;
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CardResult execute(Long deckId, String term, String definition, String ownerEmail) {
        User owner = userRepository.findByEmail(new Email(ownerEmail))
                .orElseThrow(() -> new EntityNotFoundError("User not found"));

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new EntityNotFoundError("Deck not found"));

        if (!deck.isOwnedBy(owner.getId())) {
            throw new AccessDeniedError("You do not own this deck");
        }

        CardTerm cardTerm = new CardTerm(term);
        CardDefinition cardDefinition = new CardDefinition(definition);
        Card card = cardFactory.create(cardTerm, cardDefinition, deckId);
        Card saved = cardRepository.save(card);

        return CardResultMapper.toResult(saved);
    }
}
