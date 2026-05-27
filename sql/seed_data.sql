-- ============================================================
-- Compass - Seed Data (demo / development use only)
-- Run AFTER schema.sql
-- ============================================================
USE compass_db;

-- ─── Additional sample complaints ────────────────────────────
-- Passwords are seeded by DataSeeder.java at startup.
-- student1 user_id = 3 (set by DataSeeder); adjust if different.

INSERT IGNORE INTO complaints
    (student_id, title, description, category, priority, status,
     location_id, assigned_department_id)
VALUES
(3, 'Broken water fountain near library',
 'The main water fountain on the ground floor of the library has been out of order for two weeks.',
 'Facilities', 'HIGH', 'SUBMITTED', 2, 2),

(3, 'Poor lighting in car park B',
 'Several light fittings in car park B are broken, creating a safety hazard at night.',
 'Security', 'HIGH', 'ASSIGNED', 5, 3),

(3, 'Leaking roof in science lab',
 'Water drips from the roof in Lab 204 during rain, damaging equipment.',
 'Facilities', 'CRITICAL', 'IN_PROGRESS', 3, 2),

(3, 'Wi-Fi dead zone in dormitory block C',
 'Students in block C have no Wi-Fi access for the past three days.',
 'IT', 'HIGH', 'UNDER_REVIEW', NULL, 5),

(3, 'Missing textbooks in library',
 'At least 15 copies of "Data Structures" (ISBN 978-0-13-468599-1) are unaccounted for.',
 'Academic', 'MEDIUM', 'RESOLVED', 2, 4),

(3, 'Noisy construction disrupting exams',
 'Heavy machinery outside the exam hall is audible during assessments.',
 'Facilities', 'CRITICAL', 'RESOLVED', 4, 2);

-- ─── Sample transfers ────────────────────────────────────────
-- Assumes admin user_id = 1
INSERT IGNORE INTO complaint_transfers
    (complaint_id, from_department_id, to_department_id, reason, transferred_by)
SELECT c.complaint_id, 1, 3,
       'Escalated to Security due to safety risk', 1
FROM   complaints c
WHERE  c.title = 'Poor lighting in car park B'
LIMIT 1;
