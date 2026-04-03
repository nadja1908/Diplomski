-- Posle 09_prune_invalid_ocena.sql: sprečava nove / izmenjene ocene van pravila napredovanja.

CREATE OR REPLACE FUNCTION nais_trg_ocena_before_ins_upd() RETURNS trigger AS $$
DECLARE
  gu int;
  kg int;
  ks int;
  ts timestamptz;
BEGIN
  SELECT s.godina_upisa, p.kurikulum_godina, p.kurikulum_semestar, it.datum_vreme
    INTO gu, kg, ks, ts
  FROM student s
  JOIN ispitni_termin it ON it.id = NEW.ispitni_termin_id
  JOIN predmet p ON p.id = it.predmet_id
  WHERE s.id = NEW.student_id;
  IF gu IS NULL THEN
    RAISE EXCEPTION 'student_id % nije pronađen', NEW.student_id;
  END IF;
  IF gu <= 2021 THEN
    RETURN NEW;
  END IF;
  IF NOT nais_ocena_je_u_redu(gu, ts, kg, ks) THEN
    RAISE EXCEPTION
      USING MESSAGE = format(
        'Ocena nije u skladu sa napredovanjem: student_id=%, ispitni_termin_id=% (potrebno je više završenih semestara za ovaj predmet).',
        NEW.student_id, NEW.ispitni_termin_id);
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_ocena_napredak ON ocena;
CREATE TRIGGER trg_ocena_napredak
  BEFORE INSERT OR UPDATE OF student_id, ispitni_termin_id ON ocena
  FOR EACH ROW EXECUTE PROCEDURE nais_trg_ocena_before_ins_upd();
