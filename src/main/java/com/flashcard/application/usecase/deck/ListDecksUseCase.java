package com.flashcard.application.usecase.deck;

import com.flashcard.application.dto.DeckResult;
import com.flashcard.domain.error.EntityNotFoundError;
import com.flashcard.domain.model.Deck;
import com.flashcard.domain.model.DomainPage;
import com.flashcard.domain.model.User;
import com.flashcard.domain.repository.DeckRepository;
import com.flashcard.domain.repository.UserRepository;
import com.flashcard.domain.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListDecksUseCase {

    private final DeckRepository deckRepository;
    private final UserRepository userRepository;

    public ListDecksUseCase(DeckRepository deckRepository, UserRepository userRepository) {
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public DomainPage<DeckResult> execute(String ownerEmail, int page, int size) {
        User owner = userRepository.findByEmail(new Email(ownerEmail))
                .orElseThrow(() -> new EntityNotFoundError("User not found"));

        DomainPage<Deck> decks = deckRepository.findByOwnerId(owner.getId(), page, size);

        return new DomainPage<>(
                decks.content().stream().map(CreateDeckUseCase::toResult).toList(),
                decks.page(),
                decks.size(),
                decks.totalElements(),
                decks.totalPages()
        );
    }
}
