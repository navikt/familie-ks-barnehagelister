ALTER TABLE barnehagelister
    ADD COLUMN IF NOT EXISTS leverandor_org_nr VARCHAR default null;