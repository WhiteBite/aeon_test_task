--Add initial role
INSERT INTO role (ROLE_NAME) VALUES ('ROLE_USER');
INSERT INTO role (ROLE_NAME) VALUES ('ROLE_ADMIN');
--
INSERT INTO `user` (`user_id`, `created_at`, `updated_at`, `is_active`, `amount`, `currency`, `password`, `username`) VALUES
(1, '2021-04-11 06:26:08', '2021-04-11 06:26:08', b'1', '8.00', 'USD', '$2a$10$kgPZwr1fZMbj2PBS.s/7q.HmUErWbqkTbY.mPBdITUui8l9sHzGFK', 'dandan');
