INSERT INTO ias_problem (id)
SELECT id
from dis_problem
WHERE name = 'Ошибка работы ИАС';
