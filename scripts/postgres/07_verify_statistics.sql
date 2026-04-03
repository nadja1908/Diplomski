-- Provera nakon 07_rich_demo_seed.sql (docker compose exec -T postgres psql -U nais -d nais -f ...)

SELECT sp.sifra,
       (SELECT COUNT(*) FROM student s WHERE s.studijski_program_id = sp.id)       AS studenata,
       (SELECT COUNT(*) FROM predmet p WHERE p.studijski_program_id = sp.id)       AS predmeta,
       (SELECT COUNT(*) FROM ocena o JOIN student s ON s.id = o.student_id AND s.studijski_program_id = sp.id) AS ocena_redova
FROM studijski_program sp
ORDER BY sp.id;

SELECT COUNT(*) AS predmeta_sa_izlaskom_si
FROM (SELECT it.predmet_id
      FROM ocena o
               JOIN ispitni_termin it ON o.ispitni_termin_id = it.id
               JOIN predmet pr ON pr.id = it.predmet_id AND pr.studijski_program_id = 2
      GROUP BY it.predmet_id) x;
