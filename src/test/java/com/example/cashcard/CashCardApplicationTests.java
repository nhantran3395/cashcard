package com.example.cashcard;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests {
    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void shouldReturnACashCardWhenDataIsSaved() {
        ResponseEntity<String> res = restTemplate.getForEntity("/cashcards/100", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        DocumentContext docContext = JsonPath.parse(res.getBody());
        Number id = docContext.read("$.id");
        assertThat(id).isEqualTo(100);
        Double amount = docContext.read("$.amount");
        assertThat(amount).isEqualTo(10.2);
    }

    @Test
    void shouldNotReturnACashCardWithAnUnknownId() {
        ResponseEntity<String> res = restTemplate.getForEntity("/cashcards/2", String.class);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(res.getBody()).isBlank();
    }
}
