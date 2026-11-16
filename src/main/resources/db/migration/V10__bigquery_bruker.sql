DO $$
    BEGIN
        CREATE USER "bigqueryuser";
    EXCEPTION WHEN DUPLICATE_OBJECT THEN
        RAISE NOTICE 'Oppretter ikke rollen bigqueryuser -- den finnes allerede';
    END
$$;

-- Gir BigQuery-brukeren tilgang til skjemaet og lesetilgang til alle eksisterende tabeller.
GRANT USAGE ON SCHEMA public TO "bigqueryuser";
GRANT SELECT ON ALL TABLES IN SCHEMA public TO "bigqueryuser";

-- Gjelder fremtidige tabeller opprettet av rollen som kjører migreringen.
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO "bigqueryuser";
