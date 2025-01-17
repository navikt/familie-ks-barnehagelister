ALTER TABLE barnehageliste
    ADD COLUMN IF NOT EXISTS kommune_org_nr VARCHAR default null;