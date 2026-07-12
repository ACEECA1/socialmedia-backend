ALTER TABLE users 
ADD COLUMN verification_code VARCHAR(10),
ADD COLUMN verification_expires_at TIMESTAMP NULL;
