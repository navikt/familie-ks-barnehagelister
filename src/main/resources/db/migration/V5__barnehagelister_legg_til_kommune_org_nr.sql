ALTER TABLE barnehagelister
    ADD COLUMN IF NOT EXISTS kommune_org_nr VARCHAR default null;