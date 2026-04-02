-- Demo: godina_upisa 2019–2023 → 2021–2025 i sufiks indeksa /19–/23 → /21–/25 (usklađeno sa 02_data.sql).
-- Na bazi već učitanoj NOVIM 02_data.sql nema 2019–2023 ni /19 — migracija menja samo godinu_upisa (0 redova) i preskače indekse.
-- Staru bazu: pokrenuti npr.
--   docker compose exec -T postgres psql -U nais -d nais -f /docker-entrypoint-initdb.d/03_migrate_demo_godine.sql

BEGIN;

UPDATE student SET godina_upisa = CASE godina_upisa
  WHEN 2019 THEN 2021
  WHEN 2020 THEN 2022
  WHEN 2021 THEN 2023
  WHEN 2022 THEN 2024
  WHEN 2023 THEN 2025
  ELSE godina_upisa
END
WHERE godina_upisa IN (2019, 2020, 2021, 2022, 2023);

-- Samo ako postoji bar jedan stari indeks (/19); inače bi se na novom seedu pogrešno pomerile ispravne /21–/25 vrednosti.
UPDATE student SET broj_indeksa = regexp_replace(broj_indeksa, '/23$', '/25')
WHERE broj_indeksa ~ '/23$' AND EXISTS (SELECT 1 FROM student s2 WHERE s2.broj_indeksa ~ '/19$' LIMIT 1);
UPDATE student SET broj_indeksa = regexp_replace(broj_indeksa, '/22$', '/24')
WHERE broj_indeksa ~ '/22$' AND EXISTS (SELECT 1 FROM student s2 WHERE s2.broj_indeksa ~ '/19$' LIMIT 1);
UPDATE student SET broj_indeksa = regexp_replace(broj_indeksa, '/21$', '/23')
WHERE broj_indeksa ~ '/21$' AND EXISTS (SELECT 1 FROM student s2 WHERE s2.broj_indeksa ~ '/19$' LIMIT 1);
UPDATE student SET broj_indeksa = regexp_replace(broj_indeksa, '/20$', '/22')
WHERE broj_indeksa ~ '/20$' AND EXISTS (SELECT 1 FROM student s2 WHERE s2.broj_indeksa ~ '/19$' LIMIT 1);
UPDATE student SET broj_indeksa = regexp_replace(broj_indeksa, '/19$', '/21')
WHERE broj_indeksa ~ '/19$' AND EXISTS (SELECT 1 FROM student s2 WHERE s2.broj_indeksa ~ '/19$' LIMIT 1);

COMMIT;
