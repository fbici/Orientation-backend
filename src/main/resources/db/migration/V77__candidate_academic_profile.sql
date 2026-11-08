-- F3.1: self-declared academic data. Official verification remains independent.
ALTER TABLE candidates ADD COLUMN academic_data jsonb;
ALTER TABLE candidates ADD COLUMN academic_confirmed_at timestamptz;
