ALTER TABLE solution_step
    DROP COLUMN picture;
ALTER TABLE solution_step
    ADD COLUMN picture BYTEA;
