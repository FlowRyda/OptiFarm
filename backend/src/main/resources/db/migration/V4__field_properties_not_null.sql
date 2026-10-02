-- Make name not nullable (if field exists)
ALTER TABLE IF EXISTS field
    ALTER COLUMN name SET NOT NULL;

-- Make area not nullable (if field exists)
ALTER TABLE IF EXISTS field
    ALTER COLUMN area SET NOT NULL;

-- Make boundaries not nullable (if field exists)
ALTER TABLE IF EXISTS field
    ALTER COLUMN boundaries SET NOT NULL;