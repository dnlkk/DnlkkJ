drop table if exists user_table;
CREATE TABLE IF NOT EXISTS user_table
(
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(36) NOT NULL,
    surname VARCHAR(36) NOT NULL
);