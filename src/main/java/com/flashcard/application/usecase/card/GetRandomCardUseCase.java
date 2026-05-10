package com.flashcard.application.usecase.card;

import com.flashcard.application.dto.CardResult;
import com.flashcard.domain.error.AccessDeniedError;
import com.flashcard.domain.error.EntityNotFoundError;
import com.flashcard.domain.model.Card;
import com.flashcard.domain.model.Deck;
import com.flashcard.domain.model.DomainPage;
import com.flashcard.domain.model.User;
import com.flashcard.domain.repository.CardRepository;
import com.flashcard.domain.repository.DeckRepository;
import com.flashcard.domain.repository.UserRepository;
import com.flashcard.domain.valueobject.Email;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GetRandomCardUseCase {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final UserRepository userRepository;

    public GetRandomCardUseCase(CardRepository cardRepository,
                                DeckRepository deckRepository,
                                UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.deckRepository = deckRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<CardResult> execute(Long deckId, String ownerEmail) {
        User owner = userRepository.findByEmail(new Email(ownerEmail))
                .orElseThrow(() -> new EntityNotFoundError("User not found"));

        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new EntityNotFoundError("Deck not found"));

        if (!deck.isOwnedBy(owner.getId())) {
            throw new AccessDeniedError("You do not own this deck");
        }

        long count = cardRepository.countByDeckId(deckId);
        if (count == 0) {
            return Optional.empty();
        }

        int randomIndex = ThreadLocalRandom.current().nextInt((int) count);
        DomainPage<Card> page = cardRepository.findByDeckId(deckId, randomIndex, 1);
        Card card = page.content().getFirst();

        return Optional.of(CardResultMapper.toResult(card));
    }
}
