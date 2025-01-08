CREATE TABLE barnehagebarn
(
    id                       UUID PRIMARY KEY,
    ident                    VARCHAR                                      NOT NULL,
    fom                      DATE                                         NOT NULL,
    tom                      DATE,
    antall_timer_i_barnehage NUMERIC,
    endringstype             VARCHAR                                      NOT NULL,
    kommune_navm             VARCHAR                                      NOT NULL,
    kommune_nr               VARCHAR                                      NOT NULL,
    arkiv_referanse          UUID                                         NOT NULL,
    organisasjonsnummer      VARCHAR                                      NOT NULL,
    -- Base entitet felter
    versjon                  BIGINT       DEFAULT 0                       NOT NULL,
    opprettet_av             VARCHAR      DEFAULT 'VL'::CHARACTER VARYING NOT NULL,
    opprettet_tid            TIMESTAMP(3) DEFAULT LOCALTIMESTAMP          NOT NULL,
    endret_av                VARCHAR,
    endret_tid               TIMESTAMP(3)
);