package com.flashcard.application.usecase.card;

import com.flashcard.application.dto.CardResult;
import com.flashcard.domain.model.Card;

class CardResultMapper {

    private CardResultMapper() {
    }

    static CardResult toResult(Card card) {
        return new CardResult(
                card.getId(),
                card.getTerm().value(),
                card.getDefinition().value(),
                card.getDeckId(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
