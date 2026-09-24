-- Insert roles
INSERT INTO role_ (name) VALUES ('user');
INSERT INTO role_ (name) VALUES ('admin');

-- Insert users (password: Test1234=)
INSERT INTO user_ (username, password, role_id, is_enable, birthday) 
VALUES ('user', '$2a$10$slYQmyNdGzin7olVN3p5Be7DkH0B3z9b8x1ynVLW2AFAIiUHLezga', 1, true, NULL);

INSERT INTO user_ (username, password, role_id, is_enable, birthday) 
VALUES ('admin', '$2a$10$slYQmyNdGzin7olVN3p5Be7DkH0B3z9b8x1ynVLW2AFAIiUHLezga', 2, true, NULL);

-- Insert games
INSERT INTO game (name, release_year, price, is_enable) VALUES ('Devil may cry', 2001, 5000, true);
INSERT INTO game (name, release_year, price, is_enable) VALUES ('Onimusha', 2001, 3000, true);
INSERT INTO game (name, release_year, price, is_enable) VALUES ('Resident evil', 1996, 10000, true);
INSERT INTO game (name, release_year, price, is_enable) VALUES ('League of legend', 2009, 1000000, true);
