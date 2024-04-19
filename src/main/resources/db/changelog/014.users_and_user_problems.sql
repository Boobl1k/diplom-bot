CREATE TABLE user_problem
(
    id                BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    user_id           BIGINT,
    sent              BOOLEAN                                 NOT NULL,
    short_description VARCHAR(255),
    dis_problem_id    BIGINT,
    problem_case_id   BIGINT,
    details           VARCHAR(255),
    ticket_id         BIGINT,
    CONSTRAINT pk_userproblem PRIMARY KEY (id)
);

ALTER TABLE user_problem
    ADD CONSTRAINT FK_USERPROBLEM_ON_DIS_PROBLEM FOREIGN KEY (dis_problem_id) REFERENCES dis_problem (id);

ALTER TABLE user_problem
    ADD CONSTRAINT FK_USERPROBLEM_ON_PROBLEM_CASE FOREIGN KEY (problem_case_id) REFERENCES problem (id);



CREATE TABLE users
(
    id                 BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    chat_id            BIGINT                                  NOT NULL,
    state              SMALLINT,
    phone              VARCHAR(255),
    name               VARCHAR(255),
    address            VARCHAR(255),
    office_number      VARCHAR(255),
    current_problem_id BIGINT,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_a8e6d3f9c23fb12c43a339504 UNIQUE (chat_id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_CURRENT_PROBLEM FOREIGN KEY (current_problem_id) REFERENCES user_problem (id);

ALTER TABLE user_problem
    ADD CONSTRAINT FK_USERPROBLEM_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);


CREATE TABLE users_sent_problems
(
    user_id          BIGINT NOT NULL,
    sent_problems_id BIGINT NOT NULL
);

ALTER TABLE users_sent_problems
    ADD CONSTRAINT uc_users_sent_problems_sentproblems UNIQUE (sent_problems_id);

ALTER TABLE users_sent_problems
    ADD CONSTRAINT fk_usesenpro_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users_sent_problems
    ADD CONSTRAINT fk_usesenpro_on_user_problem FOREIGN KEY (sent_problems_id) REFERENCES user_problem (id);