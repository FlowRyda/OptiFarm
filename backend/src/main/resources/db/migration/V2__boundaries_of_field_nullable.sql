-- Make boundaries nullable (if field exists)
ALTER TABLE IF EXISTS field
    ALTER COLUMN boundaries DROP NOT NULL;