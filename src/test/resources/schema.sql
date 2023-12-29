CREATE TABLE cash_card
(
    id     BIGINT NOT NULL,
    amount DOUBLE PRECISION,
    owner  VARCHAR(255),
    CONSTRAINT pk_cash_card PRIMARY KEY (id)
);