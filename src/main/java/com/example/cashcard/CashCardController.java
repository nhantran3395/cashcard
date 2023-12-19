package com.example.cashcard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@RestController()
@RequestMapping("/cashcards")
public class CashCardController {
    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable long requestedId) {
        Optional<CashCard> cashCardOptional = cashCardRepository.findById(requestedId);

        if (cashCardOptional.isPresent()) {
            return ResponseEntity.ok(cashCardOptional.get());
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    private ResponseEntity<Void> createNew(@RequestBody CashCard newCashCardRequest) throws URISyntaxException {
        CashCard savedCashCard = cashCardRepository.save(newCashCardRequest);
        return ResponseEntity.created(new URI("/cashcards/" + savedCashCard.id())).build();
    }
}
