-- Briše evidentirane ocene koje ne mogu postojati po pravilu napredovanja
-- (datum ispita vs. godina upisa vs. kurikulum_godina / kurikulum_semestar predmeta).
-- Funkcije su u 01_schema.sql (nais_ocena_je_u_redu).
-- Generacije iz 02_data.sql (godina_upisa <= 2021) ne diramo — datumi ispita nisu strogo modelovani.

DELETE FROM ocena o
USING ispitni_termin it, predmet p, student s
WHERE o.ispitni_termin_id = it.id
  AND it.predmet_id = p.id
  AND o.student_id = s.id
  AND s.godina_upisa >= 2022
  AND NOT nais_ocena_je_u_redu(s.godina_upisa, it.datum_vreme, p.kurikulum_godina, p.kurikulum_semestar);
