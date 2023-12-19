package com.example.cashcard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CashCardJsonTest {
    @Autowired
    private JacksonTester<CashCard> json;

    @Autowired
    private JacksonTester<List<CashCard>> jsonlist;

    private List<CashCard> cashCards;

    @BeforeEach
    void setUp() {
        cashCards = Arrays.asList(
                new CashCard(99L, 123.45),
                new CashCard(100L, 1.00),
                new CashCard(101L, 150.00)
        );
    }

    @Test
    void cashCardSerializationTest() throws IOException {
        CashCard cashCard = new CashCard(99L, 123.45);
        assertThat(json.write(cashCard)).isStrictlyEqualToJson("single.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id").isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount").isEqualTo(123.45);
    }

    @Test
    void cashCardDeserialzationTest() throws IOException {
        String expected = """
                {
                    "id": 1000,
                    "amount": 67.89
                }
                """;
        assertThat(json.parse(expected)).isEqualTo(new CashCard(1000L, 67.89));
        assertThat(json.parseObject(expected).id()).isEqualTo(1000);
        assertThat(json.parseObject(expected).amount()).isEqualTo(67.89);
    }

    @Test
    void cashCardListSerializationTest() throws IOException {
        assertThat(jsonlist.write(cashCards)).isStrictlyEqualToJson("list.json");
    }

    @Test
    void cashCardListDeserializationTest() throws IOException {
        String expected = """
                [
                   { "id": 99, "amount": 123.45 },
                   { "id": 100, "amount": 1.00 },
                   { "id": 101, "amount": 150.00 }
                ]
                """;

        assertThat(jsonlist.parse(expected)).isEqualTo(cashCards);
    }
}
