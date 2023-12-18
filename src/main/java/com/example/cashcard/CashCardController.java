package com.example.cashcard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/cashcards")
public class CashCardController {
    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable long requestedId) {
        if (requestedId == 100L) {
            CashCard cashCard = new CashCard(100L, 10.2);
            return ResponseEntity.ok(cashCard);
        }

        return ResponseEntity.notFound().build();
    }
}
