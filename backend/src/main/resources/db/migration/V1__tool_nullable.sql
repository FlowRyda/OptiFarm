-- Make image nullable (if tool exists)
ALTER TABLE IF EXISTS tool
ALTER COLUMN image DROP NOT NULL;

-- Change type to bytea, keeping existing values
ALTER TABLE IF EXISTS tool
ALTER COLUMN image TYPE bytea
    USING image::bytea;