CREATE SEQUENCE task_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS task (
                                    id            BIGINT       DEFAULT NEXTVAL('task_seq'::REGCLASS) PRIMARY KEY,
                                    payload       text                                                 NOT NULL,
                                    status        varchar(15)  DEFAULT 'UBEHANDLET'::character varying NOT NULL,
                                    versjon       bigint       DEFAULT 0,
                                    opprettet_tid timestamp(3) DEFAULT LOCALTIMESTAMP,
                                    type          varchar(100)                                         NOT NULL,
                                    metadata      varchar(4000),
                                    trigger_tid   timestamp    DEFAULT LOCALTIMESTAMP,
                                    avvikstype    varchar(50)
);

ALTER SEQUENCE task_seq OWNED BY task.id;

CREATE INDEX task_status_idx
    ON task (status);

CREATE SEQUENCE task_logg_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS task_logg (
                                         id            BIGINT       DEFAULT NEXTVAL('task_logg_seq'::REGCLASS) PRIMARY KEY,
                                         task_id       BIGINT REFERENCES task,
                                         type          varchar(15)  NOT NULL,
                                         node          varchar(100) NOT NULL,
                                         opprettet_tid timestamp(3) DEFAULT LOCALTIMESTAMP,
                                         melding       text,
                                         endret_av     varchar(100) DEFAULT 'VL'::character varying
);

CREATE INDEX task_logg_task_id_idx
    ON task_logg (task_id);

