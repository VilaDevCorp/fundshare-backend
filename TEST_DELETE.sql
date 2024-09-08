DELETE FROM groups WHERE name='Italy trip' AND created_by= (SELECT id FROM users WHERE username='johntesting');
DELETE FROM groups WHERE name='Bachelor party' AND created_by= (SELECT id FROM users WHERE username='marytester');

DELETE FROM groups WHERE created_by=(SELECT id FROM users WHERE username='johntesting');
DELETE FROM groups WHERE created_by=(SELECT id FROM users WHERE username='marytester');
DELETE FROM groups WHERE created_by=(SELECT id FROM users WHERE username='andrewtest');
DELETE FROM groups WHERE created_by=(SELECT id FROM users WHERE username='sarahruns');
DELETE FROM groups WHERE created_by=(SELECT id FROM users WHERE username='danieldev');
DELETE FROM groups WHERE created_by=(SELECT id FROM users WHERE username='emilytest');
DELETE FROM groups WHERE created_by=(SELECT id FROM users WHERE username='bentesting');

DELETE FROM users WHERE username='johntesting';
DELETE FROM users WHERE username='marytester';
DELETE FROM users WHERE username='andrewtest';
DELETE FROM users WHERE username='sarahruns';
DELETE FROM users WHERE username='danieldev';
DELETE FROM users WHERE username='emilytest';
DELETE FROM users WHERE username='bentesting';