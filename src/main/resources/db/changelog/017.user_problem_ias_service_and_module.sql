ALTER TABLE user_problem
    ADD ias_service_id BIGINT;

ALTER TABLE user_problem
    ADD ias_module_id BIGINT;

ALTER TABLE user_problem
    ADD file_id VARCHAR(255);
