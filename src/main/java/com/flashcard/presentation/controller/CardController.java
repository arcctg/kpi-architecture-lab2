package com.flashcard.presentation.controller;

import com.flashcard.application.dto.CardResult;
import com.flashcard.application.usecase.card.*;
import com.flashcard.domain.model.DomainPage;
import com.flashcard.presentation.dto.request.CreateCardRequest;
import com.flashcard.presentation.dto.request.UpdateCardRequest;
import com.flashcard.presentation.dto.response.CardResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/decks/{deckId}/cards")
public class CardController {

    private final AddCardUseCase addCardUseCase;
    private final GetCardUseCase getCardUseCase;
    private final ListCardsUseCase listCardsUseCase;
    private final UpdateCardUseCase updateCardUseCase;
    private final DeleteCardUseCase deleteCardUseCase;
    private final GetRandomCardUseCase getRandomCardUseCase;

    public CardController(AddCardUseCase addCardUseCase,
                          GetCardUseCase getCardUseCase,
                          ListCardsUseCase listCardsUseCase,
                          UpdateCardUseCase updateCardUseCase,
                          DeleteCardUseCase deleteCardUseCase,
                          GetRandomCardUseCase getRandomCardUseCase) {
        this.addCardUseCase = addCardUseCase;
        this.getCardUseCase = getCardUseCase;
        this.listCardsUseCase = listCardsUseCase;
        this.updateCardUseCase = updateCardUseCase;
        this.deleteCardUseCase = deleteCardUseCase;
        this.getRandomCardUseCase = getRandomCardUseCase;
    }

    @GetMapping
    public ResponseEntity<DomainPage<CardResponse>> list(
            @PathVariable Long deckId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            Authentication auth) {
        DomainPage<CardResult> results = listCardsUseCase.execute(
                deckId, auth.getName(), search, page, size);
        DomainPage<CardResponse> response = new DomainPage<>(
                results.content().stream().map(CardResponse::from).toList(),
                results.page(), results.size(), results.totalElements(), results.totalPages()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CardResponse> create(@PathVariable Long deckId,
                                               @Valid @RequestBody CreateCardRequest request,
                                               Authentication auth) {
        CardResult result = addCardUseCase.execute(
                deckId, request.term(), request.definition(), auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(CardResponse.from(result));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponse> get(@PathVariable Long deckId,
                                            @PathVariable Long cardId,
                                            Authentication auth) {
        CardResult result = getCardUseCase.execute(deckId, cardId, auth.getName());
        return ResponseEntity.ok(CardResponse.from(result));
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CardResponse> update(@PathVariable Long deckId,
                                               @PathVariable Long cardId,
                                               @Valid @RequestBody UpdateCardRequest request,
                                               Authentication auth) {
        CardResult result = updateCardUseCase.execute(
                deckId, cardId, request.term(), request.definition(), auth.getName());
        return ResponseEntity.ok(CardResponse.from(result));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> delete(@PathVariable Long deckId,
                                       @PathVariable Long cardId,
                                       Authentication auth) {
        deleteCardUseCase.execute(deckId, cardId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/random")
    public ResponseEntity<CardResponse> random(@PathVariable Long deckId, Authentication auth) {
        Optional<CardResult> result = getRandomCardUseCase.execute(deckId, auth.getName());
        return result.map(r -> ResponseEntity.ok(CardResponse.from(r)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
