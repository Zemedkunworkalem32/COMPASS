USE compass_db;

INSERT IGNORE INTO complaints
    (student_id, title, description, category, status,
     location_id, assigned_department_id)
VALUES
(3, 'Broken water fountain near library',
 'The main water fountain on the ground floor of the library has been out of order for two weeks.',
 'Facilities', 'SUBMITTED', 2, 2),

(3, 'Poor lighting in car park B',
 'Several light fittings in car park B are broken, creating a safety hazard at night.',
 'Security', 'ASSIGNED', 5, 3),

(3, 'Leaking roof in science lab',
 'Water drips from the roof in Lab 204 during rain, damaging equipment.',
 'Facilities', 'IN_PROGRESS', 3, 2),

(3, 'Wi-Fi dead zone in dormitory block C',
 'Students in block C have no Wi-Fi access for the past three days.',
 'IT', 'UNDER_REVIEW', NULL, 5),

(3, 'Missing textbooks in library',
 'At least 15 copies of "Data Structures" are unaccounted for.',
 'Academic', 'RESOLVED', 2, 4),

(3, 'Noisy construction disrupting exams',
 'Heavy machinery outside the exam hall is audible during assessments.',
 'Facilities', 'RESOLVED', 4, 2);
