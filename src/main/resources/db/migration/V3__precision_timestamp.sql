ALTER TABLE barnehagelister
    ALTER COLUMN opprettet_tid TYPE TIMESTAMP USING opprettet_tid::TIMESTAMP(3),
    ALTER COLUMN ferdig_tid TYPE TIMESTAMP USING ferdig_tid::TIMESTAMP(3);