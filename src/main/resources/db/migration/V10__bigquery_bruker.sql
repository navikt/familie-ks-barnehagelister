DO $$
    BEGIN
        CREATE USER "bigquerybruker";
    EXCEPTION WHEN DUPLICATE_OBJECT THEN
        RAISE NOTICE 'Oppretter ikke rollen bigquerybruker -- den finnes allerede';
    END
$$;

-- Gir BigQuery-brukeren tilgang til skjemaet og lesetilgang til alle eksisterende tabeller.
GRANT USAGE ON SCHEMA public TO "bigquerybruker";
GRANT SELECT ON ALL TABLES IN SCHEMA public TO "bigquerybruker";

-- Gjelder fremtidige tabeller opprettet av rollen som kjører migreringen.
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO "bigquerybruker";
