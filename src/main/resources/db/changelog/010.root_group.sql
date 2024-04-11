INSERT INTO problem_group (parent_group_id, name, description)
VALUES (null, 'root group', '');

UPDATE problem_group
SET parent_group_id = (SELECT id FROM problem_group WHERE name = 'root group')
WHERE parent_group_id is NULL
  AND name != 'root group';
