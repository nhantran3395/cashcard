package com.example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainersConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "insert.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "delete.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CashCardApplicationTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    TestJwtCreator jwtCreator;

    private HttpHeaders createJwtBearerAuthenticationHeader(String username) {
        String token = jwtCreator.create(username);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        return headers;
    }

    @Test
    void shouldReturnCashCard() {
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        ResponseEntity<String> res = restTemplate
                .exchange("/cashcards/99", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext docContext = JsonPath.parse(res.getBody());
        Number id = docContext.read("$.id");
        assertThat(id).isEqualTo(99);
        Double amount = docContext.read("$.amount");
        assertThat(amount).isEqualTo(123.45);
    }

    @Test
    void shouldReturnNotFoundForNonExistingCashCard() {
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        ResponseEntity<String> res = restTemplate
                .exchange("/cashcards/2", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getBody()).isBlank();
    }

    @Test
    @DirtiesContext
    void shouldCreateNewCashCard() {
        CashCard payload = new CashCard(null, 10.2, "sarah1");
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        ResponseEntity<Void> postRes = restTemplate
                .exchange("/cashcards", HttpMethod.POST, new HttpEntity<>(payload, headers), Void.class);
        URI location = postRes.getHeaders().getLocation();

        assertThat(postRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> getRes = restTemplate
                .exchange(location, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext docContext = JsonPath.parse(getRes.getBody());
        Number id = docContext.read("$.id");
        Double amount = docContext.read("$.amount");
        assertThat(id).isInstanceOf(Number.class).isNotNull();
        assertThat(amount).isEqualTo(payload.getAmount());
    }

    @Test
    void shouldReturnListOfCashCards() {
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        ResponseEntity<String> res = restTemplate
                .exchange("/cashcards", HttpMethod.GET, new HttpEntity<>(headers), String.class);

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
    void shouldReturnPageOfCashCards() {
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        ResponseEntity<String> res = restTemplate
                .exchange("/cashcards?page=0&size=1", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext docContext = JsonPath.parse(res.getBody());
        int cashCardCount = docContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(1);
    }

    @Test
    void shouldReturnSortedPageOfCashCards() {
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        ResponseEntity<String> res = restTemplate
                .exchange("/cashcards?page=0&sort=amount,desc", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);

        DocumentContext docContext = JsonPath.parse(res.getBody());
        int cashCardCount = docContext.read("$.length()");
        assertThat(cashCardCount).isEqualTo(3);

        double amount = docContext.read("$[0].amount");
        assertThat(amount).isEqualTo(150.00);
    }

    @Test
    void shouldReturnPageOfCashCardsWhenCalledWithNoParamAndDefaultSorting() {
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        ResponseEntity<String> res = restTemplate
                .exchange("/cashcards", HttpMethod.GET, new HttpEntity<>(headers), String.class);

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
        CashCard payload = new CashCard(null, 120.00, "sarah1");
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        HttpEntity<CashCard> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> putRes = restTemplate
                .exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);
        assertThat(putRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> getRes = restTemplate
                .exchange("/cashcards/99", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        DocumentContext docContext = JsonPath.parse(getRes.getBody());
        int id = docContext.read("$.id");
        assertThat(id).isEqualTo(99);
        double amount = docContext.read("$.amount");
        assertThat(amount).isEqualTo(120.00);
    }

    @Test
    void shouldReturnNotFoundWhenUpdateNonExistingCashCard() {
        CashCard payload = new CashCard(null, 15.00, "sarah1");
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        HttpEntity<CashCard> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> res = restTemplate
                .exchange("/cashcards/97", HttpMethod.PUT, request, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundWhenUpdateCashCardThatUserDoesNotOwn() {
        CashCard payload = new CashCard(null, 15.00, "sarah1");
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        HttpEntity<CashCard> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Void> res = restTemplate
                .exchange("/cashcards/102", HttpMethod.PUT, request, Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DirtiesContext
    void shouldDeleteAnExistingCashCard() {
        HttpHeaders headers = createJwtBearerAuthenticationHeader("kumar2");
        ResponseEntity<Void> deleteRes = restTemplate
                .exchange("/cashcards/102", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(deleteRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Void> getRes = restTemplate
                .exchange("/cashcards/102", HttpMethod.GET, new HttpEntity<>(headers), Void.class);
        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundWhenDeleteNonExistingCashCard() {
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        ResponseEntity<Void> res = restTemplate
                .exchange("/cashcards/96", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldReturnNotFoundWhenDeleteCashCardThatUserDoesNotOwn() {
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        ResponseEntity<Void> res = restTemplate
                .exchange("/cashcards/102", HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldNotDeleteWhenDeleteCashCardThatUserDoesNotOwn() {
        HttpHeaders user1AuthHeaders = createJwtBearerAuthenticationHeader("sarah1");
        restTemplate
                .exchange("/cashcards/102", HttpMethod.DELETE, new HttpEntity<>(user1AuthHeaders), Void.class);

        HttpHeaders user2AuthHeaders = createJwtBearerAuthenticationHeader("kumar2");
        ResponseEntity<String> getRes = restTemplate
                .exchange("/cashcards/102", HttpMethod.GET, new HttpEntity<>(user2AuthHeaders), String.class);

        assertThat(getRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext docContext = JsonPath.parse(getRes.getBody());
        int id = docContext.read("$.id");
        assertThat(id).isEqualTo(102);
        double amount = docContext.read("$.amount");
        assertThat(amount).isEqualTo(200.00);
    }

    @Test
    void shouldReturnNotFoundWhenUserAccessOtherUserCashCard() {
        HttpHeaders headers = createJwtBearerAuthenticationHeader("sarah1");
        ResponseEntity<String> res = restTemplate
                .exchange("/cashcards/102", HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
