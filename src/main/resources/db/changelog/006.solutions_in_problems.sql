BEGIN;

ALTER TABLE solution_step
    DROP CONSTRAINT fk_solutionstep_on_solution;
ALTER TABLE solution_step
    RENAME COLUMN solution_id TO problem_id;
ALTER TABLE solution_step
    ADD CONSTRAINT fk_solutionstep_on_problem FOREIGN KEY (problem_id) REFERENCES problem (id);

ALTER TABLE problem
    DROP CONSTRAINT fk_problem_on_solution;
ALTER TABLE problem
    DROP COLUMN solution_id;
ALTER TABLE problem
    ADD COLUMN solution_text VARCHAR(255);
ALTER TABLE problem
    ADD COLUMN solution_picture BYTEA;

DROP TABLE solution;

COMMIT;
