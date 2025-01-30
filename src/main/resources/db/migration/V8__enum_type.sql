CREATE TYPE etterprosesseringfeil_type AS ENUM ('OVERLAPPING_PERIOD_WITHIN_SAME_LIST');

ALTER TABLE barnehageliste_valideringsfeil
    ADD COLUMN etterprosesseringfeiltype etterprosesseringfeil_type NOT NULL DEFAULT 'OVERLAPPING_PERIOD_WITHIN_SAME_LIST';

ALTER TABLE barnehageliste_valideringsfeil
    DROP COLUMN type;