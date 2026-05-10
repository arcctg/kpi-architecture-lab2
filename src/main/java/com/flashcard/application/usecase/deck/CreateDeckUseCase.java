package com.flashcard.application.usecase.deck;

import com.flashcard.application.dto.DeckResult;
import com.flashcard.domain.factory.DeckFactory;
import com.flashcard.domain.model.Deck;
import com.flashcard.domain.model.User;
import com.flashcard.domain.error.EntityNotFoundError;
import com.flashcard.domain.repository.DeckRepository;
import com.flashcard.domain.repository.UserRepository;
import com.flashcard.domain.valueobject.DeckTitle;
import com.flashcard.domain.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateDeckUseCase {

    private final DeckFactory deckFactory;
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;

    public CreateDeckUseCase(DeckFactory deckFactory,
                             DeckRepository deckRepository,
                             UserRepository userRepository) {
        this.deckFactory = deckFactory;
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DeckResult execute(String title, String description, String ownerEmail) {
        User owner = userRepository.findByEmail(new Email(ownerEmail))
                .orElseThrow(() -> new EntityNotFoundError("User not found"));

        DeckTitle deckTitle = new DeckTitle(title);
        Deck deck = deckFactory.create(deckTitle, description, owner.getId());
        Deck saved = deckRepository.save(deck);

        return toResult(saved);
    }

    static DeckResult toResult(Deck deck) {
        return new DeckResult(
                deck.getId(),
                deck.getTitle().value(),
                deck.getDescription(),
                deck.getCardCount(),
                deck.getCreatedAt(),
                deck.getUpdatedAt()
        );
    }
}
