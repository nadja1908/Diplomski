-- Postavi sufiks broja indeksa (/YY) da tačno prati godina_upisa (npr. 2025 → …/25).
-- Bezbedno za ponavljanje ako su već usklađeni.

BEGIN;

UPDATE student
SET broj_indeksa = regexp_replace(
    broj_indeksa,
    '/[0-9]{2}$',
    '/' || substring(godina_upisa::text from 3 for 2)
)
WHERE broj_indeksa ~ '^RA [0-9]+/[0-9]{2}$'
  AND broj_indeksa !~ ('/' || substring(godina_upisa::text from 3 for 2) || '$');

COMMIT;
