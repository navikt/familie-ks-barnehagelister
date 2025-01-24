CREATE TABLE barnehageliste_valideringsfeil
(
    id                          UUID PRIMARY KEY,
    fk_barnehageliste_id        UUID references barnehageliste (id)             NOT NULL,
    type                        VARCHAR                                         NOT NULL,
    feilinfo                    VARCHAR                                         NOT NULL,
    ident                       VARCHAR                                         NOT NULL,
    opprettet_tid               TIMESTAMP(3) DEFAULT LOCALTIMESTAMP             NOT NULL
);
