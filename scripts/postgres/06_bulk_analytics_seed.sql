-- Dodatni sintetički izlasci (tanak sloj). Glavni prošireni skup studenata je 07_rich_demo_seed.sql
-- (generiše se sa generate_07_rich_demo_seed.py). Ovaj fajl može ostati za dodatnu gustinu ocena.

WITH pairs AS (
  SELECT DISTINCT s.id AS sid, it.id AS tid
  FROM student s
  JOIN ispitni_termin it ON EXISTS (
    SELECT 1 FROM predmet pr
    WHERE pr.id = it.predmet_id AND pr.studijski_program_id = s.studijski_program_id
  )
  WHERE mod(abs(hashtext(s.id::text || ',' || it.id::text)), 4) = 0
)
INSERT INTO ocena (student_id, ispitni_termin_id, poeni, vrednost_ocene)
SELECT sid,
  tid,
  (20 + mod(abs(hashtext('p' || sid::text || tid::text)), 56))::int,
  CASE
    WHEN mod(abs(hashtext('g' || sid::text || tid::text)), 100) < 72 THEN 6 + mod(abs(hashtext('z' || sid::text || tid::text)), 5)
    ELSE 5
  END
FROM pairs p
WHERE NOT EXISTS (
  SELECT 1 FROM ocena o WHERE o.student_id = p.sid AND o.ispitni_termin_id = p.tid
);

SELECT setval(pg_get_serial_sequence('ocena', 'id'), COALESCE((SELECT MAX(id) FROM ocena), 1));
