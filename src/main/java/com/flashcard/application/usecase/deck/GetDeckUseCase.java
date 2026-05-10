package com.flashcard.application.usecase.deck;

import com.flashcard.application.dto.DeckResult;
import com.flashcard.domain.error.AccessDeniedError;
import com.flashcard.domain.error.EntityNotFoundError;
import com.flashcard.domain.model.Deck;
import com.flashcard.domain.model.User;
import com.flashcard.domain.repository.DeckRepository;
import com.flashcard.domain.repository.UserRepository;
import com.flashcard.domain.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetDeckUseCase {

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;

    public GetDeckUseCase(DeckRepository deckRepository, UserRepository userRepository) {
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DeckResult execute(Long deckId, String ownerEmail) {
        User owner = userRepository.findByEmail(new Email(ownerEmail))
                .orElseThrow(() -> new EntityNotFoundError("User not found"));

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new EntityNotFoundError("Deck not found"));

        if (!deck.isOwnedBy(owner.getId())) {
            throw new AccessDeniedError("You do not own this deck");
        }

        return CreateDeckUseCase.toResult(deck);
    }
}
