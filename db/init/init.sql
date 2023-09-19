drop table if exists user_table;
CREATE TABLE IF NOT EXISTS user_table
(
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(36) NOT NULL,
    surname VARCHAR(36) NOT NULL
);

ALTER TABLE user_table 
ALTER COLUMN earnings TYPE DECIMAL;

ALTER TABLE user_table 
DROP COLUMN earnings;

ALTER TABLE user_table 
ADD COLUMN age INT DEFAULT 18;

drop table if exists user_operation_table;
CREATE TABLE IF NOT EXISTS user_operation_table
(
    operation_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    amount DECIMAL NOT NULL,
    operation_from_user_id INT NOT NULL REFERENCES user_table(id),
    operation_to_user_id INT NOT NULL REFERENCES user_table(id)
);


drop table if exists user_incoming_operation_table;
drop table if exists user_outcoming_operation_table;

ALTER TABLE user_operation_table 
RENAME COLUMN operation_user_id TO operation_from_user_id;

ALTER TABLE user_operation_table
ADD COLUMN operation_to_user_id INT NOT NULL DEFAULT 4 REFERENCES user_table(id);

drop table if exists user_details_table;
CREATE TABLE IF NOT EXISTS user_details_table
(
    details_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    email VARCHAR(64) NOT NULL,
    details_user_id INT NOT NULL REFERENCES user_table(id)
);

drop table if exists user_doings_table;
CREATE TABLE IF NOT EXISTS user_doings_table
(
    doings_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    doing VARCHAR(64) NOT NULL,
    doings_user_id INT NOT NULL REFERENCES user_table(id)
);