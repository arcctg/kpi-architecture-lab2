package com.flashcard.application.usecase.deck;

import com.flashcard.application.dto.DeckResult;
import com.flashcard.domain.error.AccessDeniedError;
import com.flashcard.domain.error.DuplicateError;
import com.flashcard.domain.error.EntityNotFoundError;
import com.flashcard.domain.model.Deck;
import com.flashcard.domain.model.User;
import com.flashcard.domain.repository.DeckRepository;
import com.flashcard.domain.repository.UserRepository;
import com.flashcard.domain.valueobject.DeckTitle;
import com.flashcard.domain.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateDeckUseCase {

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;

    public UpdateDeckUseCase(DeckRepository deckRepository, UserRepository userRepository) {
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DeckResult execute(Long deckId, String title, String description, String ownerEmail) {
        User owner = userRepository.findByEmail(new Email(ownerEmail))
                .orElseThrow(() -> new EntityNotFoundError("User not found"));

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new EntityNotFoundError("Deck not found"));

        if (!deck.isOwnedBy(owner.getId())) {
            throw new AccessDeniedError("You do not own this deck");
        }

        DeckTitle newTitle = new DeckTitle(title);

        if (!deck.getTitle().equals(newTitle)
                && deckRepository.existsByTitleAndOwnerId(newTitle, owner.getId())) {
            throw new DuplicateError("Deck with title '" + title + "' already exists");
        }

        deck.updateTitle(newTitle);
        deck.updateDescription(description);

        Deck saved = deckRepository.save(deck);
        return CreateDeckUseCase.toResult(saved);
    }
}
