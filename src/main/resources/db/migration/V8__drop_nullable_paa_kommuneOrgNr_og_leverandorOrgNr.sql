ALTER TABLE barnehageliste
    ALTER COLUMN kommune_org_nr DROP NOT NULL;

ALTER TABLE barnehageliste
    ALTER COLUMN leverandor_org_nr DROP NOT NULL;