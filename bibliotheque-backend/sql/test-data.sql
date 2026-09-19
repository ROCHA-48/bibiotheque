-- Insérer les rôles
INSERT INTO role (role_id, role_name) VALUES (1, 'Admin'), (2, 'User'), (3, 'BIBLIOTHECAIRE'), (4, 'ADHERENT');

-- Réinitialiser la séquence des rôles
SELECT setval('role_role_id_seq', (SELECT MAX(role_id) FROM role));

-- Insérer un admin (mot de passe: admin123)
INSERT INTO users (user_id, username, name, password)
VALUES (1, 'admin', 'Admin', '$2b$10$ksmPv6is/4dRhE8s77eg2OJUs.ECoLBr1b/SXMr/Adepg1Or/3NdS');

-- Lier le role Admin à l'utilisateur
INSERT INTO user_role (user_id, role_id) VALUES (1, 1);

-- Insérer un adhérent (mot de passe: user123)
INSERT INTO users (user_id, username, name, password)
VALUES (2, 'jean', 'Jean Dupont', '$2b$10$vQh5wV9d9Q6GJWsMq7azR.4UIX84JpBOQAvDndxoVLquUHkzfJciW');

INSERT INTO user_role (user_id, role_id) VALUES (2, 4);

-- Insérer un bibliothécaire (mot de passe: biblio123)
INSERT INTO users (user_id, username, name, password)
VALUES (3, 'bibliothecaire', 'Marie Bibliothécaire', '$2b$10$rL8Fj0aVwDFLrNwqvcIIWOZjXW0AbnspK7FcqIzjd.LGwGgeMvVWy');

INSERT INTO user_role (user_id, role_id) VALUES (3, 3);

-- Insérer un premier adhérent (mot de passe: adherent123)
INSERT INTO users (user_id, username, name, password)
VALUES (4, 'adherent1', 'Adherent Un', '$2b$10$0wCxaCjxfKp1s2jc2gThY.opqaO./J16rSYZLtvYRKX8lvq6kpJeq');

INSERT INTO user_role (user_id, role_id) VALUES (4, 4);

-- Insérer un deuxième adhérent (mot de passe: adherent456)
INSERT INTO users (user_id, username, name, password)
VALUES (5, 'adherent2', 'Adherent Deux', '$2b$10$12/8O03RVz0n42Zu97AKL.7Z10eIxu60X6n1ob8u9jSH1OnGWMmi2');

INSERT INTO user_role (user_id, role_id) VALUES (5, 4);

-- Réinitialiser la séquence des utilisateurs
SELECT setval('users_user_id_seq', (SELECT MAX(user_id) FROM users));

-- Insérer des livres
INSERT INTO books (book_name, book_author, book_genre, no_of_copies) VALUES
('Le Petit Prince', 'Antoine de Saint-Exupéry', 'Conte', 0),
('L''Étranger', 'Albert Camus', 'Roman', 2),
('Les Misérables', 'Victor Hugo', 'Roman', 0),
('Candide', 'Voltaire', 'Roman', 3);

-- Réinitialiser la séquence des livres
SELECT setval('books_book_id_seq', (SELECT MAX(book_id) FROM books));

-- Insérer des réservations de démonstration (1 par adhérent, sur des livres à 0 exemplaire)
-- Prérequis du brief : chaque adhérent doit avoir au moins une réservation à son nom.
INSERT INTO reservation (reservation_id, book_id, user_id, statut, date_reservation, date_expiration)
VALUES
  (1, 1, 4, 'EN_ATTENTE', NOW(), NOW() + INTERVAL '7 days'),   -- adherent1 attend 'Le Petit Prince'
  (2, 3, 5, 'EN_ATTENTE', NOW(), NOW() + INTERVAL '7 days');   -- adherent2 attend 'Les Misérables'

-- Réinitialiser la séquence des réservations
SELECT setval('reservation_reservation_id_seq', (SELECT MAX(reservation_id) FROM reservation));