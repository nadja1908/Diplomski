#!/usr/bin/env python3
"""
Generates PostgreSQL seed SQL and vector chunks.
  pip install bcrypt

Run from NAIS-projekat folder:
  python scripts/generate_seed_data.py
"""
from __future__ import annotations

import json
import random
from datetime import datetime, timedelta, timezone
from pathlib import Path

try:
    import bcrypt
except ImportError:
    raise SystemExit("pip install bcrypt")

ROOT = Path(__file__).resolve().parent.parent
OUT_SQL_SCHEMA = ROOT / "scripts" / "postgres" / "01_schema.sql"
OUT_SQL_DATA = ROOT / "scripts" / "postgres" / "02_data.sql"
OUT_VECTOR = ROOT / "scripts" / "vector" / "chunks.jsonl"

random.seed(42)

EMAIL_DOMAIN = "ftn.rs"


def h(pw: str) -> str:
    return bcrypt.hashpw(pw.encode(), bcrypt.gensalt(rounds=10)).decode()


def esc(s: str) -> str:
    return s.replace("'", "''")


def main():
    OUT_SQL_SCHEMA.parent.mkdir(parents=True, exist_ok=True)
    OUT_VECTOR.parent.mkdir(parents=True, exist_ok=True)

    OUT_SQL_SCHEMA.write_text(
        """-- NAIS relational schema
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
""",
        encoding="utf-8",
    )

    katedre = [
        ("KiI", "Katedra za računarstvo i informatiku"),
        ("AU", "Katedra za automatiku"),
        ("EL", "Katedra za elektroniku"),
        ("TK", "Katedra za telekomunikacije"),
    ]
    programi = [
        (1, "RI", "Računarska i informaciona tehnologija", "Osnovne akademske studije"),
        (1, "SI", "Softversko inženjerstvo", "Osnovne akademske studije"),
        (2, "AUT", "Automatika i upravljanje sistemima", "Osnovne akademske studije"),
        (3, "EE", "Elektroenergetika", "Osnovne akademske studije"),
        (4, "RT", "Radiokomunikacije", "Osnovne akademske studije"),
        (4, "TK", "Telekomunikacije", "Master akademske studije"),
    ]

    predlozi = [
        ("13S001", "Matematika 1", 8, 1, 1, "Algebra, realne funkcije i diferencijalni račun.", 0.15),
        ("13S002", "Programiranje 1", 8, 1, 1, "Uvod u algoritme, strukture podataka i Python/C.", 0.25),
        ("13S021", "Diskretne strukture", 6, 1, 1, "Relacije, kombinatorika i osnovi teorije grafova.", 0.35),
        ("13S031", "Objektno orijentisano programiranje", 8, 1, 1, "SOLID, OOP dizajn i praksa: Java, C++, Python.", 0.3),
        ("13S032", "Strukture i algoritmi", 8, 1, 1, "Analiza složenosti, pretrage, sortiranje, grafovi.", 0.55),
        ("13S033", "Operativni sistemi", 8, 1, 1, "Procesi, niti, memorija i fajl sistemi (UNIX/Windows).", 0.45),
        ("13S041", "Baze podataka", 8, 1, 1, "SQL, normalizacija, transakcije; NoSQL uvod.", 0.4),
        ("13S042", "Računarske mreže", 8, 1, 1, "TCP/IP, HTTP, rute, bežične mreže.", 0.35),
        ("13S051", "Paralelno programiranje", 6, 1, 1, "OpenMP, CUDA, raspodela posla i metrike performansi.", 0.6),
        ("13S052", "Verifikacija softvera", 6, 1, 1, "Testiranje, formalne metode, property testing.", 0.5),
        ("13S053", "Mašinsko učenje", 8, 2, 1, "Regresija, klasifikacija, drveća odluke, neuronske mreže.", 0.55),
        ("13A010", "Servo sistemi", 6, 3, 2, "Modelovanje, diskretizacija i digitalno upravljanje.", 0.42),
        ("13A020", "Upravljanje procesima", 6, 3, 2, "PID, neizvesnost i primena u industriji.", 0.48),
        ("13E011", "Električna merenja", 6, 4, 3, "Senzori, analogni i digitalni signali, greške merenja.", 0.32),
        ("13E022", "Električne mašine", 8, 4, 3, "Transformatori i asinhroni motori.", 0.38),
        ("13T011", "Digitalna komunikacija", 8, 5, 4, "Simboli, šuma, modulacije, OFDM osnove.", 0.44),
        ("13T012", "Antene i prostiranje", 6, 5, 4, "Link budžet, polarizacija, antenski nizovi.", 0.36),
        ("13T013", "Bežične senzorske mreže", 6, 6, 4, "MAC protokoli, rutiranje i energetska efikasnost.", 0.52),
        ("13S061", "Distribuirani sist", 8, 2, 1, "Replikacija, konsenzus, striming platforme.", 0.58),
        ("13S062", "Sigurnost informacija", 6, 1, 1, "Kriptografija, OAuth, OWASP, bezbednost mreža.", 0.47),
        ("13S063", "Web tehnologije", 6, 2, 1, "HTML, REST, React, mikroservisi.", 0.33),
        ("13S071", "Kompajleri", 8, 2, 1, "Leksička i sintaksna analiza, optimizacije, LLVM uvod.", 0.65),
        ("13S072", "Cloud computing", 6, 2, 1, "Virtuelizacija, Docker, Kubernetes, skaliranje.", 0.41),
        ("13S073", "Informacioni sistemi", 6, 1, 1, "ER modeli, integracija, BI i veliki podaci.", 0.34),
    ]

    lines: list[str] = []
    chunks: list[dict] = []

    def w(stmt: str) -> None:
        lines.append(stmt + "\n")

    hpw_student = h("student123")
    hpw_sef = h("sef123")
    hpw_prof = h("prof123")

    uid = 0

    w("COPY katedra (id, sifra, naziv) FROM stdin;")
    for i, (sif, nz) in enumerate(katedre, start=1):
        w(f"{i}\t{sif}\t{nz}")
    w("\\.")
    w("SELECT setval(pg_get_serial_sequence('katedra','id'), (SELECT MAX(id) FROM katedra));")

    w("COPY studijski_program (id, sifra, naziv, stepen, katedra_id) FROM stdin;")
    for i, (kat, sif, nz, st) in enumerate(programi, start=1):
        w(f"{i}\t{sif}\t{nz}\t{st}\t{kat}")
    w("\\.")
    w(
        "SELECT setval(pg_get_serial_sequence('studijski_program','id'), (SELECT MAX(id) FROM studijski_program));"
    )

    heads = []
    for i, (ksif, _) in enumerate(katedre):
        uid += 1
        email = f"sef.{ksif.lower()}@{EMAIL_DOMAIN}"
        ime = ["Marko", "Ana", "Jovan", "Milica"][i % 4]
        prez = ["Petrović", "Nikolić", "Jovanović", "Stanković"][i % 4]
        w(
            f"INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES "
            f"({uid}, '{email}', '{hpw_sef}', '{ime}', '{prez}', 'SEF_KATEDRE');"
        )
        heads.append((uid, i + 1))

    prof_uids: list[tuple[int, int]] = []
    zvanja = ["docent", "vanredni profesor", "redovni profesor"]
    for j in range(16):
        uid += 1
        kat = j % 4 + 1
        email = f"prof{j + 1:02d}@{EMAIL_DOMAIN}"
        imena = ("Milan", "Jelena", "Stefan", "Ivana", "Nikola", "Tamara")
        prezimena = ("Ilić", "Đorđević", "Popović", "Đurić", "Kostić", "Marković")
        w(
            f"INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES "
            f"({uid}, '{email}', '{hpw_prof}', '{imena[j % 6]}', '{prezimena[j % 6]}', 'PROFESOR');"
        )
        prof_uids.append((uid, kat))

    # students 120
    student_rows: list[tuple[int, int, str, int, int]] = []
    for s in range(120):
        uid += 1
        prog = s % 6 + 1
        god = 2019 + (s % 5)
        indeks = f"RA {s + 1:03d}/{god % 100:02d}"
        email = f"student{s + 1:03d}@{EMAIL_DOMAIN}"
        ime = random.choice(
            ["Luka", "Sara", "Filip", "Teodora", "Vuk", "Maša", "Uroš", "Hana"]
        )
        prez = random.choice(
            ["Simić", "Pavić", "Ristić", "Čukić", "Milić", "Bogdanović", "Radović", "Tomić"]
        )
        sid = s + 1
        w(
            f"INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES "
            f"({uid}, '{email}', '{hpw_student}', '{ime}', '{prez}', 'STUDENT');"
        )
        w(
            f"INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES "
            f"({sid}, {uid}, '{indeks}', {prog}, {god});"
        )
        student_rows.append((sid, uid, indeks, prog, god))

    w("SELECT setval(pg_get_serial_sequence('korisnik','id'), (SELECT MAX(id) FROM korisnik));")
    w("SELECT setval(pg_get_serial_sequence('student','id'), (SELECT MAX(id) FROM student));")

    for hid, kid in heads:
        w(f"INSERT INTO sef_katedre (korisnik_id, katedra_id) VALUES ({hid}, {kid});")

    for j, (puid, kat) in enumerate(prof_uids, start=1):
        w(
            f"INSERT INTO profesor (id, korisnik_id, katedra_id, zvanje) VALUES "
            f"({j}, {puid}, {kat}, '{zvanja[j % 3]}');"
        )
    w("SELECT setval(pg_get_serial_sequence('profesor','id'), (SELECT MAX(id) FROM profesor));")

    pid = 0
    predmeti: list[
        tuple[int, str, str, int, int, int, str, float]
    ] = []
    for row in predlozi:
        sif, nz, espb, prog, kat, opis, diff = row
        pid += 1
        predmeti.append((pid, sif, nz, espb, prog, kat, opis, diff))
        w(
            f"INSERT INTO predmet (id, sifra, naziv, espb, studijski_program_id, katedra_id, kratak_opis) VALUES "
            f"({pid}, '{sif}', '{esc(nz)}', {espb}, {prog}, {kat}, '{esc(opis)}');"
        )

        cilj = (
            f"Cilj predmeta {nz} je da student savlada teorijske i praktične osnove "
            f"neophodne za dalji studij i inženjersku praksu u oblasti koju predmet pokriva."
        )
        ishod = (
            "Student je sposoban da primeni ključne koncepte kurseva u projektnim zadacima; "
            "analizira probleme i bira odgovarajuće algoritme i alate; dokumentuje rešenja."
        )
        metode = (
            "Predavanja, vežbe, individualni i timski domaći zadaci, laboratorijske vežbe, "
            "kolokvijumi i završni ispit. Akcenat na aktivnom učenju i feedback-u."
        )
        teme = (
            f"Tok kursa obuhvata osnovne teme iz {nz.lower()}, uključujući pregled literature, "
            "rad u razvojnom okruženju i vežbe iz tipskih zadataka iz_ispitne prakse."
        )
        w(
            f"INSERT INTO sadrzaj_predmeta (predmet_id, cilj, ishodi_ucenja, metode_nastave, teme_kursa) VALUES "
            f"({pid}, '{esc(cilj)}', '{esc(ishod)}', '{esc(metode)}', '{esc(teme)}');"
        )

        chunks.append(
            {
                "predmet_id": pid,
                "predmet_sifra": sif,
                "predmet_naziv": nz,
                "tip": "opis",
                "text": f"{nz} ({sif}): {opis}",
            }
        )
        chunks.append(
            {
                "predmet_id": pid,
                "predmet_sifra": sif,
                "predmet_naziv": nz,
                "tip": "cilj",
                "text": cilj,
            }
        )
        chunks.append(
            {
                "predmet_id": pid,
                "predmet_sifra": sif,
                "predmet_naziv": nz,
                "tip": "ishodi",
                "text": ishod,
            }
        )
        chunks.append(
            {
                "predmet_id": pid,
                "predmet_sifra": sif,
                "predmet_naziv": nz,
                "tip": "metode",
                "text": metode,
            }
        )
        chunks.append(
            {
                "predmet_id": pid,
                "predmet_sifra": sif,
                "predmet_naziv": nz,
                "tip": "teme",
                "text": teme,
            }
        )

    for j in range(len(predmeti) - 1):
        a = predmeti[j][0]
        b = predmeti[j + 1][0]
        w(f"INSERT INTO preduslov (predmet_id, preduslov_predmet_id) VALUES ({b}, {a});")
    w("SELECT setval(pg_get_serial_sequence('preduslov','id'), COALESCE((SELECT MAX(id) FROM preduslov), 1));")

    rokovi = [
        (1, "Januarski ispitni rok", "2023/24", "zimski"),
        (2, "Aprilski ispitni rok", "2023/24", "prolećni"),
        (3, "Junski ispitni rok", "2023/24", "letnji"),
        (4, "Septembarski rok", "2024/25", "letnji"),
        (5, "Januarski ispitni rok", "2024/25", "zimski"),
        (6, "Aprilski ispitni rok", "2024/25", "prolećni"),
    ]
    for rid, nz, sg, tip in rokovi:
        w(
            f"INSERT INTO ispitni_rok (id, naziv, skolska_godina, tip) VALUES ({rid}, '{nz}', '{sg}', '{tip}');"
        )
    w("SELECT setval(pg_get_serial_sequence('ispitni_rok','id'), (SELECT MAX(id) FROM ispitni_rok));")

    def diff_val(p):
        return p[7] if len(p) > 7 else 0.4

    def grade_from_diff(diff: float) -> int:
        if random.random() < diff * 0.45:
            return random.choice([5, 5, 6])
        return int(random.choices([6, 7, 8, 9, 10], weights=[8, 18, 28, 28, 18])[0])

    tid = 0
    termini: list[tuple[int, int]] = []
    base = datetime(2024, 1, 10, tzinfo=timezone.utc)
    sale = ["A-101", "A-102", "B-201", "RC-05", "AMP-1"]
    for rid in range(1, 7):
        for p in predmeti:
            pidm, sif = p[0], p[1]
            if random.random() < 0.85:
                tid += 1
                dt = base + timedelta(days=rid * 18 + hash(sif) % 11, hours=(tid % 5) + 9)
                saleterm = sale[tid % len(sale)]
                w(
                    f"INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) "
                    f"VALUES ({tid}, {rid}, {pidm}, '{dt.isoformat()}', '{saleterm}');"
                )
                termini.append((tid, pidm))

    w(
        "SELECT setval(pg_get_serial_sequence('ispitni_termin','id'), (SELECT MAX(id) FROM ispitni_termin));"
    )

    oid = 0
    for sid, _, _, prog, _ in student_rows:
        preds = [p for p in predmeti if p[4] == prog]
        random.shuffle(preds)
        take = min(len(preds), random.randint(14, 22))
        for p in preds[:take]:
            pidm = p[0]
            diff = p[7]
            cands = [t for t in termini if t[1] == pidm]
            if not cands:
                continue
            term = random.choice(cands)
            oid += 1
            oc = grade_from_diff(diff)
            poeni = 20 + (oc - 5) * 9 + random.randint(-3, 4)
            poeni = max(0, min(100, poeni))
            w(
                f"INSERT INTO ocena (id, student_id, ispitni_termin_id, poeni, vrednost_ocene) "
                f"VALUES ({oid}, {sid}, {term[0]}, {poeni}, {oc});"
            )

    w("SELECT setval(pg_get_serial_sequence('predmet','id'), (SELECT MAX(id) FROM predmet));")
    w("SELECT setval(pg_get_serial_sequence('ocena','id'), (SELECT MAX(id) FROM ocena));")

    OUT_SQL_DATA.write_text("".join(lines), encoding="utf-8")

    with OUT_VECTOR.open("w", encoding="utf-8") as vf:
        for c in chunks:
            vf.write(json.dumps(c, ensure_ascii=False) + "\n")

    print("Written:", OUT_SQL_SCHEMA)
    print("Written:", OUT_SQL_DATA, "lines", len(lines))
    print("Written:", OUT_VECTOR, "records", len(chunks))


if __name__ == "__main__":
    main()
