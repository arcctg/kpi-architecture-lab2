package com.flashcard.presentation.controller;

import com.flashcard.application.dto.DeckResult;
import com.flashcard.application.usecase.deck.*;
import com.flashcard.domain.model.DomainPage;
import com.flashcard.presentation.dto.request.CreateDeckRequest;
import com.flashcard.presentation.dto.request.UpdateDeckRequest;
import com.flashcard.presentation.dto.response.DeckResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/decks")
public class DeckController {

    private final CreateDeckUseCase createDeckUseCase;
    private final GetDeckUseCase getDeckUseCase;
    private final ListDecksUseCase listDecksUseCase;
    private final UpdateDeckUseCase updateDeckUseCase;
    private final DeleteDeckUseCase deleteDeckUseCase;

    public DeckController(CreateDeckUseCase createDeckUseCase,
                          GetDeckUseCase getDeckUseCase,
                          ListDecksUseCase listDecksUseCase,
                          UpdateDeckUseCase updateDeckUseCase,
                          DeleteDeckUseCase deleteDeckUseCase) {
        this.createDeckUseCase = createDeckUseCase;
        this.getDeckUseCase = getDeckUseCase;
        this.listDecksUseCase = listDecksUseCase;
        this.updateDeckUseCase = updateDeckUseCase;
        this.deleteDeckUseCase = deleteDeckUseCase;
    }

    @GetMapping
    public ResponseEntity<DomainPage<DeckResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        DomainPage<DeckResult> results = listDecksUseCase.execute(auth.getName(), page, size);
        DomainPage<DeckResponse> response = new DomainPage<>(
                results.content().stream().map(DeckResponse::from).toList(),
                results.page(), results.size(), results.totalElements(), results.totalPages()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<DeckResponse> create(@Valid @RequestBody CreateDeckRequest request,
                                               Authentication auth) {
        DeckResult result = createDeckUseCase.execute(
                request.title(), request.description(), auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(DeckResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeckResponse> get(@PathVariable Long id, Authentication auth) {
        DeckResult result = getDeckUseCase.execute(id, auth.getName());
        return ResponseEntity.ok(DeckResponse.from(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeckResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateDeckRequest request,
                                               Authentication auth) {
        DeckResult result = updateDeckUseCase.execute(
                id, request.title(), request.description(), auth.getName());
        return ResponseEntity.ok(DeckResponse.from(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        deleteDeckUseCase.execute(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
