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
  kratak_opis TEXT
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
