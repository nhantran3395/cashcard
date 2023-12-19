package com.example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldReturnACashCardWhenDataIsSaved() {
        ResponseEntity<String> res = restTemplate.getForEntity("/cashcards/99", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext docContext = JsonPath.parse(res.getBody());
        Number id = docContext.read("$.id");
        assertThat(id).isEqualTo(99);
        Double amount = docContext.read("$.amount");
        assertThat(amount).isEqualTo(123.45);
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> res = restTemplate.getForEntity("/cashcards/2", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getBody()).isBlank();
    }

    @Test
    @DirtiesContext
    void shouldCreateANewCashCardWithValidPayload() {
        CashCard newCashCard = new CashCard(null, 10.2);
        ResponseEntity<Void> postRes = restTemplate.postForEntity("/cashcards", newCashCard, Void.class);
        URI location = postRes.getHeaders().getLocation();

        assertThat(postRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> getRes = restTemplate.getForEntity(location, String.class);
        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext docContext = JsonPath.parse(getRes.getBody());
        Number id = docContext.read("$.id");
        Double amount = docContext.read("$.amount");
        assertThat(id).isInstanceOf(Number.class).isNotNull();
        assertThat(amount).isEqualTo(newCashCard.amount());
    }

    @Test
    void shouldReturnAListOfCashCards() {
        ResponseEntity<String> res = restTemplate.getForEntity("/cashcards", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext docContext = JsonPath.parse(res.getBody());
        int cashCardCount = docContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        JSONArray ids = docContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

        JSONArray amounts = docContext.read("$..amount");
        assertThat(amounts).containsExactlyInAnyOrder(123.45, 1.0, 150.00);
    }

    @Test
    void shouldReturnAPageOfCashCards() {
        ResponseEntity<String> res = restTemplate.getForEntity("/cashcards?page=0&size=1", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext docContext = JsonPath.parse(res.getBody());
        int cashCardCount = docContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfCashCards() {
        ResponseEntity<String> res = restTemplate.getForEntity("/cashcards?page=0&sort=amount,desc", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext docContext = JsonPath.parse(res.getBody());
        int cashCardCount = docContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        double amount = docContext.read("$[0].amount");
        assertThat(amount).isEqualTo(150.00);
    }

    @Test
    void shouldReturnAPageOfCashCardsWithNoParameterAndDefaultSorting() {
        ResponseEntity<String> res = restTemplate.getForEntity("/cashcards", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext docContext = JsonPath.parse(res.getBody());
        int cashCardCount = docContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        JSONArray ids = docContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

        JSONArray amounts = docContext.read("$..amount");
        assertThat(amounts).containsExactly(1.0, 123.45, 150.00);
    }
}
