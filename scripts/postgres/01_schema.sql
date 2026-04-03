-- NAIS relational schema
CREATE TABLE korisnik (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  lozinka_hash VARCHAR(255) NOT NULL,
  ime VARCHAR(100) NOT NULL,
  prezime VARCHAR(100) NOT NULL,
  uloga VARCHAR(30) NOT NULL CHECK (uloga IN ('STUDENT','SEF_KATEDRE','PROFESOR'))
);

CREATE TABLE katedra (
  id BIGSERIAL PRIMARY KEY,
  sifra VARCHAR(20) NOT NULL UNIQUE,
  naziv VARCHAR(255) NOT NULL
);

CREATE TABLE studijski_program (
  id BIGSERIAL PRIMARY KEY,
  sifra VARCHAR(20) NOT NULL UNIQUE,
  naziv VARCHAR(255) NOT NULL,
  stepen VARCHAR(50) NOT NULL,
  katedra_id BIGINT NOT NULL REFERENCES katedra(id)
);

CREATE TABLE profesor (
  id BIGSERIAL PRIMARY KEY,
  korisnik_id BIGINT NOT NULL UNIQUE REFERENCES korisnik(id),
  katedra_id BIGINT NOT NULL REFERENCES katedra(id),
  zvanje VARCHAR(100)
);

CREATE TABLE student (
  id BIGSERIAL PRIMARY KEY,
  korisnik_id BIGINT NOT NULL UNIQUE REFERENCES korisnik(id),
  broj_indeksa VARCHAR(30) NOT NULL UNIQUE,
  studijski_program_id BIGINT NOT NULL REFERENCES studijski_program(id),
  godina_upisa INT NOT NULL
);

CREATE TABLE sef_katedre (
  korisnik_id BIGINT PRIMARY KEY REFERENCES korisnik(id),
  katedra_id BIGINT NOT NULL REFERENCES katedra(id)
);

CREATE TABLE predmet (
  id BIGSERIAL PRIMARY KEY,
  sifra VARCHAR(20) NOT NULL UNIQUE,
  naziv VARCHAR(255) NOT NULL,
  espb INT NOT NULL,
  studijski_program_id BIGINT NOT NULL REFERENCES studijski_program(id),
  katedra_id BIGINT NOT NULL REFERENCES katedra(id),
  kratak_opis TEXT,
  kurikulum_godina INT NOT NULL CHECK (kurikulum_godina BETWEEN 1 AND 4),
  kurikulum_semestar INT NOT NULL CHECK (kurikulum_semestar IN (1, 2))
);

CREATE TABLE sadrzaj_predmeta (
  predmet_id BIGINT PRIMARY KEY REFERENCES predmet(id) ON DELETE CASCADE,
  cilj TEXT,
  ishodi_ucenja TEXT,
  metode_nastave TEXT,
  teme_kursa TEXT
);

CREATE TABLE preduslov (
  id BIGSERIAL PRIMARY KEY,
  predmet_id BIGINT NOT NULL REFERENCES predmet(id),
  preduslov_predmet_id BIGINT NOT NULL REFERENCES predmet(id),
  UNIQUE (predmet_id, preduslov_predmet_id)
);

CREATE TABLE ispitni_rok (
  id BIGSERIAL PRIMARY KEY,
  naziv VARCHAR(100) NOT NULL,
  skolska_godina VARCHAR(20) NOT NULL,
  tip VARCHAR(50) NOT NULL
);

CREATE TABLE ispitni_termin (
  id BIGSERIAL PRIMARY KEY,
  ispitni_rok_id BIGINT NOT NULL REFERENCES ispitni_rok(id),
  predmet_id BIGINT NOT NULL REFERENCES predmet(id),
  datum_vreme TIMESTAMPTZ NOT NULL,
  sala VARCHAR(50)
);

CREATE TABLE ocena (
  id BIGSERIAL PRIMARY KEY,
  student_id BIGINT NOT NULL REFERENCES student(id),
  ispitni_termin_id BIGINT NOT NULL REFERENCES ispitni_termin(id),
  poeni INT,
  vrednost_ocene INT NOT NULL CHECK (vrednost_ocene BETWEEN 5 AND 10),
  UNIQUE(student_id, ispitni_termin_id)
);

CREATE INDEX idx_ocena_student ON ocena(student_id);
CREATE INDEX idx_ocena_termin ON ocena(ispitni_termin_id);
CREATE INDEX idx_ispitni_termin_predmet ON ispitni_termin(predmet_id);
CREATE INDEX idx_student_program ON student(studijski_program_id);
CREATE INDEX idx_predmet_program ON predmet(studijski_program_id);
CREATE INDEX idx_predmet_katedra ON predmet(katedra_id);
CREATE INDEX idx_predmet_kurikulum ON predmet(studijski_program_id, kurikulum_godina, kurikulum_semestar);

-- Napredovanje: da li je na datum ispita (Europe/Belgrade) student završio dovoljno semestara za predmet (kg, ks).
CREATE OR REPLACE FUNCTION nais_datum_ispita_bg(exam_ts timestamptz) RETURNS date AS $$
  SELECT (exam_ts AT TIME ZONE 'Europe/Belgrade')::date;
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION nais_procenjena_godina_studija(gu int, d date) RETURNS int AS $$
DECLARE
  cy int := EXTRACT(YEAR FROM d)::int;
  cm int := EXTRACT(MONTH FROM d)::int;
  pocetak int;
  g int;
BEGIN
  IF cm >= 10 THEN pocetak := cy; ELSE pocetak := cy - 1; END IF;
  g := pocetak - gu + 1;
  RETURN LEAST(GREATEST(g, 1), 6);
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION nais_zavrseni_sem_u_godini(d date) RETURNS int AS $$
DECLARE m int := EXTRACT(MONTH FROM d)::int;
BEGIN
  IF m >= 10 OR m = 1 THEN RETURN 0; END IF;
  IF m BETWEEN 2 AND 6 THEN RETURN 1; END IF;
  RETURN 2;
END;
$$ LANGUAGE plpgsql IMMUTABLE;

CREATE OR REPLACE FUNCTION nais_ukupno_zavrsenih_semestara(gu int, d date) RETURNS int AS $$
  SELECT 2 * (nais_procenjena_godina_studija(gu, d) - 1) + nais_zavrseni_sem_u_godini(d);
$$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION nais_ocena_je_u_redu(gu int, exam_ts timestamptz, kg int, ks int) RETURNS boolean AS $$
  SELECT nais_ukupno_zavrsenih_semestara(gu, nais_datum_ispita_bg(exam_ts)) >= (kg - 1) * 2 + ks;
$$ LANGUAGE SQL IMMUTABLE;
