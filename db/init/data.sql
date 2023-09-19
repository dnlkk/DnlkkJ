INSERT INTO user_table (name, surname)
VALUES ('dnlkk', 'fffff');

INSERT INTO user_table (name, surname) 
VALUES ('dnlkk2', 'fffff2');

INSERT INTO user_table  (name, surname)
VALUES ('anton', 'antonov');


INSERT INTO user_operation_table (amount, operation_user_id)
VALUES (120.2, 1);

INSERT INTO user_operation_table (amount, operation_user_id)
VALUES (-20.3, 1);

INSERT INTO user_operation_table (amount, operation_user_id)
VALUES (22, 2);

INSERT INTO user_operation_table (amount, operation_from_user_id, operation_to_user_id)
VALUES (103.3, 3, 5);

INSERT INTO user_operation_table (amount, operation_from_user_id, operation_to_user_id)
VALUES (22, 1, 6);

INSERT INTO user_operation_table (amount, operation_from_user_id, operation_to_user_id)
VALUES (25, 1, 3);


INSERT INTO user_details_table (email, details_user_id) 
VALUES ('ezzfvkoko@gmail.com', 2);

INSERT INTO user_details_table (email, details_user_id) 
VALUES ('ezzfvkoko2@gmail.com', 1);


INSERT INTO user_doings_table (doing, doings_user_id) 
VALUES ('do nothing', 2);

INSERT INTO user_doings_table (doing, doings_user_id) 
VALUES ('do nothing', 1);
INSERT INTO user_doings_table (doing, doings_user_id) 
VALUES ('sleep', 1);
INSERT INTO user_doings_table (doing, doings_user_id) 
VALUES ('eat', 1);
INSERT INTO user_doings_table (doing, doings_user_id) 
VALUES ('eat', 3);