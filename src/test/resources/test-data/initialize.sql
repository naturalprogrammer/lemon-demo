TRUNCATE SCHEMA public AND COMMIT;
SET DATABASE REFERENTIAL INTEGRITY FALSE;

INSERT INTO usr(id, email, password, name, credentials_updated_at)
VALUES (1, 'admin@example.com', '{bcrypt}$2a$10$nzg1V.oO8HIgtny2HaaJdObxwZIzz9HvbFP8nW0BfBID4COq8AZMe', 'Admin 1', '2000-01-01 12:01:01');
INSERT INTO usr_role(user_id, role) VALUES (1, 'ADMIN');

INSERT INTO usr(id, email, password, name, credentials_updated_at)
VALUES (2, 'unverifiedadmin@example.com', '{bcrypt}$2a$10$nzg1V.oO8HIgtny2HaaJdObxwZIzz9HvbFP8nW0BfBID4COq8AZMe', 'Unverified Admin', '2000-01-01 12:01:01');
INSERT INTO usr_role(user_id, role) VALUES (2, 'ADMIN');
INSERT INTO usr_role(user_id, role) VALUES (2, 'UNVERIFIED');

INSERT INTO usr(id, email, password, name, credentials_updated_at)
VALUES (3, 'blockedadmin@example.com', '{bcrypt}$2a$10$nzg1V.oO8HIgtny2HaaJdObxwZIzz9HvbFP8nW0BfBID4COq8AZMe', 'Blocked Admin', '2000-01-01 12:01:01');
INSERT INTO usr_role(user_id, role) VALUES (3, 'ADMIN');
INSERT INTO usr_role(user_id, role) VALUES (3, 'BLOCKED');

INSERT INTO usr(id, email, password, name, credentials_updated_at)
VALUES (4, 'user1@example.com', '{bcrypt}$2a$10$nzg1V.oO8HIgtny2HaaJdObxwZIzz9HvbFP8nW0BfBID4COq8AZMe', 'User 1', '2000-01-01 12:01:01');

INSERT INTO usr(id, email, password, name, credentials_updated_at)
VALUES (5, 'unverifieduser@example.com', '{bcrypt}$2a$10$nzg1V.oO8HIgtny2HaaJdObxwZIzz9HvbFP8nW0BfBID4COq8AZMe', 'Unverified User', '2000-01-01 12:01:01');
INSERT INTO usr_role(user_id, role) VALUES (5, 'UNVERIFIED');

INSERT INTO usr(id, email, password, name, credentials_updated_at)
VALUES (6, 'blockeduser@example.com', '{bcrypt}$2a$10$nzg1V.oO8HIgtny2HaaJdObxwZIzz9HvbFP8nW0BfBID4COq8AZMe', 'Blocked User', '2000-01-01 12:01:01');
INSERT INTO usr_role(user_id, role) VALUES (6, 'BLOCKED');
