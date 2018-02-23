TRUNCATE SCHEMA public AND COMMIT;
SET DATABASE REFERENTIAL INTEGRITY FALSE;

INSERT INTO usr(id, email, password, name, credentials_updated_at)
VALUES (1, 'admin@example.com', '{bcrypt}$2a$10$nzg1V.oO8HIgtny2HaaJdObxwZIzz9HvbFP8nW0BfBID4COq8AZMe', 'Admin 1', '2000-01-01 12:01:01');

INSERT INTO usr_role(user_id, role)
VALUES (1, 'ADMIN');