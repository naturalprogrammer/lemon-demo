TRUNCATE SCHEMA public AND COMMIT;
SET DATABASE REFERENTIAL INTEGRITY FALSE;

INSERT INTO usr(id, email, password, name, credentials_updated_at, version)
VALUES (101, 'admin@example.com', '{bcrypt}$2a$10$nzg1V.oO8HIgtny2HaaJdObxwZIzz9HvbFP8nW0BfBID4COq8AZMe', 'Admin 1', '2000-01-01 12:01:01', 1);
INSERT INTO usr_role(user_id, role) VALUES (101, 'ADMIN');

INSERT INTO usr(id, email, password, name, credentials_updated_at, version)
VALUES (102, 'unverifiedadmin@example.com', '{bcrypt}$2a$10$nzg1V.oO8HIgtny2HaaJdObxwZIzz9HvbFP8nW0BfBID4COq8AZMe', 'Unverified Admin', '2000-01-01 12:01:01', 1);
INSERT INTO usr_role(user_id, role) VALUES (102, 'ADMIN');
INSERT INTO usr_role(user_id, role) VALUES (102, 'UNVERIFIED');

INSERT INTO usr(id, email, password, name, credentials_updated_at, version)
VALUES (103, 'blockedadmin@example.com', '{bcrypt}$2a$10$nzg1V.oO8HIgtny2HaaJdObxwZIzz9HvbFP8nW0BfBID4COq8AZMe', 'Blocked Admin', '2000-01-01 12:01:01', 1);
INSERT INTO usr_role(user_id, role) VALUES (103, 'ADMIN');
INSERT INTO usr_role(user_id, role) VALUES (103, 'BLOCKED');

INSERT INTO usr(id, email, password, name, credentials_updated_at, version)
VALUES (104, 'user@example.com', '{bcrypt}$2a$10$YYqgZ6j8uOncPaUouDM8QOyOzgU875GFcLDSAW7u9kQxXexeCXYpi', 'User', '2000-01-01 12:01:01', 1);

INSERT INTO usr(id, email, password, name, credentials_updated_at, version)
VALUES (105, 'unverifieduser@example.com', '{bcrypt}$2a$10$YYqgZ6j8uOncPaUouDM8QOyOzgU875GFcLDSAW7u9kQxXexeCXYpi', 'Unverified User', '2000-01-01 12:01:01', 1);
INSERT INTO usr_role(user_id, role) VALUES (105, 'UNVERIFIED');

INSERT INTO usr(id, email, password, name, credentials_updated_at, version)
VALUES (106, 'blockeduser@example.com', '{bcrypt}$2a$10$YYqgZ6j8uOncPaUouDM8QOyOzgU875GFcLDSAW7u9kQxXexeCXYpi', 'Blocked User', '2000-01-01 12:01:01', 1);
INSERT INTO usr_role(user_id, role) VALUES (106, 'BLOCKED');
