package com.example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Optional;

@RestController()
@RequestMapping("/cashcards")
public class CashCardController {
    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable long requestedId, Principal principal) {
        Optional<CashCard> cashCardOptional = findCashCard(requestedId, principal);

        if (cashCardOptional.isPresent()) {
            return ResponseEntity.ok(cashCardOptional.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    private ResponseEntity<Void> createNew(
            @RequestBody CashCard newCashCardRequest,
            Principal principal
    ) throws URISyntaxException {
        CashCard newCashCardWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
        CashCard savedCashCard = cashCardRepository.save(newCashCardRequest);
        return ResponseEntity.created(new URI("/cashcards/" + savedCashCard.id())).build();
    }

    @GetMapping
    private ResponseEntity<Iterable<CashCard>> findAll(Principal principal, Pageable pageable) {
        Page<CashCard> page = cashCardRepository.findByOwner(
                principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                )
        );
        return ResponseEntity.ok(page.getContent());
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> update(
            Principal principal,
            @PathVariable Long requestedId,
            @RequestBody CashCard cashCardNeedsUpdate
    ) {
        Optional<CashCard> cashCardOptional = findCashCard(requestedId, principal);

        if (cashCardOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        cashCardRepository.save(new CashCard(
                cashCardOptional.get().id(),
                cashCardNeedsUpdate.amount(),
                principal.getName()
        ));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{requestedId}")
    private ResponseEntity<Void> delete(
            Principal principal,
            @PathVariable Long requestedId
    ) {
        Optional<CashCard> cashCardOptional = findCashCard(requestedId, principal);

        if (cashCardOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        cashCardRepository.deleteById(requestedId);
        return ResponseEntity.noContent().build();
    }

    private Optional<CashCard> findCashCard(Long id, Principal principal) {
        return cashCardRepository.findByIdAndOwner(id, principal.getName());
    }
}
