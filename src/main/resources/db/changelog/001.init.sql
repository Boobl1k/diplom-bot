CREATE TABLE problem_group
(
    id              BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    parent_group_id BIGINT,
    name            VARCHAR(255),
    description     VARCHAR(255),
    CONSTRAINT pk_problemgroup PRIMARY KEY (id)
);

ALTER TABLE problem_group
    ADD CONSTRAINT FK_PROBLEMGROUP_ON_PARENT_GROUP FOREIGN KEY (parent_group_id) REFERENCES problem_group (id);


CREATE TABLE dis_problem
(
    id               BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    problem_group_id BIGINT,
    name             VARCHAR(255),
    description      VARCHAR(255),
    dis_problem_id   INTEGER                                 NOT NULL,
    CONSTRAINT pk_disproblem PRIMARY KEY (id)
);

ALTER TABLE dis_problem
    ADD CONSTRAINT FK_DISPROBLEM_ON_PROBLEM_GROUP FOREIGN KEY (problem_group_id) REFERENCES problem_group (id);

CREATE TABLE solution
(
    id      BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    text    VARCHAR(255),
    picture VARCHAR(255),
    CONSTRAINT pk_solution PRIMARY KEY (id)
);

CREATE TABLE solution_step
(
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    step_number INTEGER                                 NOT NULL,
    text        VARCHAR(255),
    picture     VARCHAR(255),
    solution_id BIGINT,
    CONSTRAINT pk_solutionstep PRIMARY KEY (id)
);

ALTER TABLE solution_step
    ADD CONSTRAINT FK_SOLUTIONSTEP_ON_SOLUTION FOREIGN KEY (solution_id) REFERENCES solution (id);

CREATE TABLE problem
(
    id             BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    dis_problem_id BIGINT,
    solution_id    BIGINT,
    condition      VARCHAR(255),
    CONSTRAINT pk_problem PRIMARY KEY (id)
);

ALTER TABLE problem
    ADD CONSTRAINT FK_PROBLEM_ON_DIS_PROBLEM FOREIGN KEY (dis_problem_id) REFERENCES dis_problem (id);

ALTER TABLE problem
    ADD CONSTRAINT FK_PROBLEM_ON_SOLUTION FOREIGN KEY (solution_id) REFERENCES solution (id);
