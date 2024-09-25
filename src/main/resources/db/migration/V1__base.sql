CREATE TABLE barnehagelister
(
    id            UUID PRIMARY KEY,
    raw_json      jsonb,
    status        VARCHAR                             NOT NULL,
    opprettet_tid TIMESTAMP(3) DEFAULT localtimestamp NOT NULL,
    ferdig_tid    TIMESTAMP(3)
);