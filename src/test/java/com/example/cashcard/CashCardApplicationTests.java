package com.example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/99", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext docContext = JsonPath.parse(res.getBody());
        Number id = docContext.read("$.id");
        assertThat(id).isEqualTo(99);
        Double amount = docContext.read("$.amount");
        assertThat(amount).isEqualTo(123.45);
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/2", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getBody()).isBlank();
    }

    @Test
    @DirtiesContext
    void shouldCreateANewCashCardWithValidPayload() {
        CashCard newCashCard = new CashCard(null, 10.2, "sarah1");
        ResponseEntity<Void> postRes = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .postForEntity("/cashcards", newCashCard, Void.class);
        URI location = postRes.getHeaders().getLocation();

        assertThat(postRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> getRes = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity(location, String.class);
        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext docContext = JsonPath.parse(getRes.getBody());
        Number id = docContext.read("$.id");
        Double amount = docContext.read("$.amount");
        assertThat(id).isInstanceOf(Number.class).isNotNull();
        assertThat(amount).isEqualTo(newCashCard.amount());
    }

    @Test
    void shouldReturnAListOfCashCards() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards", String.class);

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
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards?page=0&size=1", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext docContext = JsonPath.parse(res.getBody());
        int cashCardCount = docContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(1);
    }

    @Test
    void shouldReturnASortedPageOfCashCards() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards?page=0&sort=amount,desc", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext docContext = JsonPath.parse(res.getBody());
        int cashCardCount = docContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        double amount = docContext.read("$[0].amount");
        assertThat(amount).isEqualTo(150.00);
    }

    @Test
    void shouldReturnAPageOfCashCardsWithNoParameterAndDefaultSorting() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext docContext = JsonPath.parse(res.getBody());
        int cashCardCount = docContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        JSONArray ids = docContext.read("$..id");
        assertThat(ids).containsExactlyInAnyOrder(99, 100, 101);

        JSONArray amounts = docContext.read("$..amount");
        assertThat(amounts).containsExactly(1.0, 123.45, 150.00);
    }

    @Test
    @DirtiesContext
    void shouldUpdateAnExistingCashCard() {
        CashCard cashCardNeedsUpdate = new CashCard(null, 120.00, "sarah1");
        HttpEntity<CashCard> request = new HttpEntity<>(cashCardNeedsUpdate);
        ResponseEntity<Void> putRes = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);
        assertThat(putRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getRes = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/99", String.class);
        DocumentContext docContext = JsonPath.parse(getRes.getBody());
        int id = docContext.read("$.id");
        assertThat(id).isEqualTo(99);
        double amount = docContext.read("$.amount");
        assertThat(amount).isEqualTo(120.00);
    }

    @Test
    void shouldReturnNotFoundWhenUpdateOnACardThatDoesNotExist() {
        CashCard cashCardNeedsUpdate = new CashCard(null, 15.00, "sarah1");
        HttpEntity<CashCard> request = new HttpEntity<>(cashCardNeedsUpdate);
        ResponseEntity<Void> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards/97", HttpMethod.PUT, request, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundWhenUpdateOnACardThatUserDoesNotOwn() {
        CashCard cashCardNeedsUpdate = new CashCard(null, 15.00, "sarah1");
        HttpEntity<CashCard> request = new HttpEntity<>(cashCardNeedsUpdate);
        ResponseEntity<Void> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .exchange("/cashcards/102", HttpMethod.PUT, request, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() {
        ResponseEntity<String> res = restTemplate
                .getForEntity("/cashcards/99", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldReturnForbiddenForUnauthorizedUser() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("hanks", "def456")
                .getForEntity("/cashcards/99", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturnNotFoundWhenUserTryingToAccessResourceOfOtherUser() {
        ResponseEntity<String> res = restTemplate
                .withBasicAuth("sarah1", "abc123")
                .getForEntity("/cashcards/102", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
