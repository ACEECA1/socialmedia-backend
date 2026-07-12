INSERT INTO roles (name) VALUES ('USER'), ('MODERATOR'), ('ADMIN');

INSERT INTO permissions (name) VALUES 
('POST_DELETE_ANY'), 
('USER_BAN'), 
('MESSAGE_DELETE_ANY'), 
('ROLE_ASSIGN');

-- Assuming auto-increment assigns IDs starting from 1 sequentially:
-- Roles: USER=1, MODERATOR=2, ADMIN=3
-- Permissions: POST_DELETE_ANY=1, USER_BAN=2, MESSAGE_DELETE_ANY=3, ROLE_ASSIGN=4

-- Admin gets all four permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES 
(3, 1),
(3, 2),
(3, 3),
(3, 4);

-- Moderator gets POST_DELETE_ANY (1) and MESSAGE_DELETE_ANY (3)
INSERT INTO role_permissions (role_id, permission_id) VALUES 
(2, 1),
(2, 3);
