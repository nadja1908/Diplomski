#!/usr/bin/env python3
"""
Generiše 07_rich_demo_seed.sql — bogat demo: novi predmeti po smerovima (X*), rokovi, termini,
~1000+ novih studenata (više generacija × smer), gusto ocenjivanje sa ponovnim pokušajima.

Broj X* demo predmeta po programu je ograničen tako da ukupno sa 02_data.sql ne prelazi ~45
predmeta po studijskom programu (realističan maksimum za osnovne studije).

  python scripts/postgres/generate_07_rich_demo_seed.py
"""
from __future__ import annotations

import pathlib
from datetime import date

from curriculum_seed_utils import allocate_demo_x_kurikulum_slots, student_moze_polagati_na_datum

OUT = pathlib.Path(__file__).resolve().parent / "07_rich_demo_seed.sql"

DEMO_LOZINKA = "$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q"

IMENA = (
    "Ana Marko Jelena Stefan Milica Nikola Ivana Luka Sara Uroš Teodora Vuk Hana Filip Maša "
    "Nemanja Katarina Petar Andrej Jovana"
).split()

# Broj predmeta u scripts/postgres/02_data.sql po studijskom programu (pre X* iz ovog fajla).
BROJ_PREDMETA_02_DATA = {
    1: 42,
    2: 40,
    3: 40,
    4: 40,
    5: 40,
    6: 40,
}
MAX_PREDMETA_PO_PROGRAMU = 45

# (studijski_program_id, katedra_id, email_stub, sifra_prefix) — broj X* redova računa se ispod.
_PROGRAM_SPECS = [
    (1, 1, "ri", "XRI"),
    (2, 1, "si", "XSI"),
    (3, 2, "aut", "XAU"),
    (4, 3, "ee", "XEE"),
    (5, 4, "rt", "XRT"),
    (6, 4, "tk", "XTK"),
]


def broj_demo_x_predmeta(prog_id: int) -> int:
    """Koliko X* predmeta sme da doda seed uz postojeći kurikulum iz 02_data.sql."""
    return max(0, MAX_PREDMETA_PO_PROGRAMU - BROJ_PREDMETA_02_DATA[prog_id])

# Demo studenti: svi programi uključujući RI (godina × generacija)
STUDENT_PROGRAMS = [
    (1, "ri", "RI"),
    (2, "si", "SI"),
    (3, "aut", "AUT"),
    (4, "ee", "EE"),
    (5, "rt", "RT"),
    (6, "tk", "TK"),
]

# Samo generacije 2022–2025 — u skladu sa linearnim modelom (bez starijih „zaostalih“ demo krugova).
YEARS = (2022, 2023, 2024, 2025)
STUDENTS_PER_PROGRAM_YEAR = 42

# Morati da se poklopi sa nais.academic.reference-intake-year u backendu.
REFERENCE_INTAKE_YEAR = 2025
# Kraj letnjeg ispita posle referentne godine — za whitelist X* u demo seedu (usklađeno sa napredovanjem).
DEMO_PROGRESSION_END = date(REFERENCE_INTAKE_YEAR + 1, 6, 30)

ISPITNI_ROKOVI = [
    (7, "Januarski ispitni rok", "2021/22", "zimski"),
    (8, "Junski ispitni rok", "2021/22", "letnji"),
    (9, "Januarski ispitni rok", "2022/23", "zimski"),
    (10, "Junski ispitni rok", "2022/23", "letnji"),
    (11, "Januarski ispitni rok", "2023/24", "zimski"),
    (12, "Junski ispitni rok", "2023/24", "letnji"),
    (13, "Januarski ispitni rok", "2024/25", "zimski"),
    (14, "Junski ispitni rok", "2024/25", "letnji"),
    (15, "Januarski ispitni rok", "2025/26", "zimski"),
    (16, "Junski ispitni rok", "2025/26", "letnji"),
]


def esc(s: str) -> str:
    return s.replace("'", "''")


def kurikulum_slots_for_demo_x(prog_id: int, prefix: str, nmod: int) -> dict[str, tuple[int, int]]:
    """(kurikulum_godina, kurikulum_semestar) za X* šifre, kao nastavak postojećeg kurikuluma programa."""
    if nmod <= 0:
        return {}
    slots = allocate_demo_x_kurikulum_slots(prog_id, nmod)
    out: dict[str, tuple[int, int]] = {}
    for i in range(1, nmod + 1):
        sifra = f"{prefix}{i:03d}"
        kg, ks = slots[i - 1]
        out[sifra] = (kg, ks)
    return out


def predmet_je_u_rasporedu(godina_upisa: int, ks: int, sem: int) -> bool:
    """Kalendar napredovanja (semestri završeni do leta posle referentne školske godine)."""
    return student_moze_polagati_na_datum(godina_upisa, DEMO_PROGRESSION_END, ks, sem)


def build_allowed_demo_rows(
    all_expand: list[tuple[int, int, str, str, int]],
) -> list[tuple[int, str, int]]:
    out: list[tuple[int, str, int]] = []
    for prog_id, _kad, _stub, prefix, nmod in all_expand:
        slots = kurikulum_slots_for_demo_x(prog_id, prefix, nmod)
        for year in YEARS:
            for sifra, (ks, sem) in slots.items():
                if predmet_je_u_rasporedu(year, ks, sem):
                    out.append((prog_id, sifra, year))
    return out


def allowed_values_sql(rows: list[tuple[int, str, int]]) -> str:
    return ",\n    ".join(f"({pid}, '{esc(sif)}', {y})" for pid, sif, y in rows)


def main() -> None:
    lines: list[str] = []
    w = lines.append

    w("-- Bogati demo podaci (NAIS). Učitava se posle 02_data.sql; 06 je opciono.")
    w("BEGIN;")
    w("")

    for rid, naziv, sg, tip in ISPITNI_ROKOVI:
        w(
            f"INSERT INTO ispitni_rok (id, naziv, skolska_godina, tip) "
            f"VALUES ({rid}, '{esc(naziv)}', '{sg}', '{tip}') ON CONFLICT (id) DO NOTHING;"
        )
    w("SELECT setval(pg_get_serial_sequence('ispitni_rok','id'), (SELECT MAX(id) FROM ispitni_rok));")
    w("")

    all_expand = [
        (pid, kid, stub, prefix, broj_demo_x_predmeta(pid))
        for (pid, kid, stub, prefix) in _PROGRAM_SPECS
    ]
    for prog_id, katedra_id, _stub, prefix, nmod in all_expand:
        slot_list = allocate_demo_x_kurikulum_slots(prog_id, nmod) if nmod > 0 else []
        for i in range(1, nmod + 1):
            sifra = f"{prefix}{i:03d}"
            naziv = f"Demo modul {prefix} {i} (program {prog_id})"
            espb = 5 + (i % 5)
            kg, ks = slot_list[i - 1]
            w(
                "INSERT INTO predmet (sifra, naziv, espb, studijski_program_id, katedra_id, kratak_opis, "
                f"kurikulum_godina, kurikulum_semestar) VALUES ("
                f"'{sifra}', '{esc(naziv)}', {espb}, {prog_id}, {katedra_id}, "
                f"'Automatski generisan demo predmet za statistiku i kurikulum.', {kg}, {ks}) "
                "ON CONFLICT (sifra) DO NOTHING;"
            )
    w("")
    w(
        "INSERT INTO sadrzaj_predmeta (predmet_id, cilj, ishodi_ucenja, metode_nastave, teme_kursa) "
        "SELECT p.id, "
        "'Cilj: usvojiti ključne koncepte modula kroz vežbe i ispit.', "
        "'Ishodi: primena znanja u projektnim zadacima.', "
        "'Predavanja, vežbe, laboratorija, ispit.', "
        "'Teme: generisan demo sadržaj za NAIS prototip.' "
        "FROM predmet p WHERE p.sifra LIKE 'X%' "
        "AND NOT EXISTS (SELECT 1 FROM sadrzaj_predmeta s WHERE s.predmet_id = p.id);"
    )
    w("")

    w(
        "INSERT INTO ispitni_termin (ispitni_rok_id, predmet_id, datum_vreme, sala) "
        "SELECT r.rok_id, p.id, r.dt, r.sala FROM predmet p "
        "CROSS JOIN LATERAL (VALUES "
        "(9,  timestamptz '2023-01-11 09:00:00+01', 'A-01'), "
        "(10, timestamptz '2023-06-12 09:00:00+02', 'A-02'), "
        "(11, timestamptz '2024-01-18 09:00:00+01', 'B-01'), "
        "(12, timestamptz '2024-06-20 09:00:00+02', 'B-02'), "
        "(13, timestamptz '2025-01-15 09:00:00+01', 'C-01'), "
        "(14, timestamptz '2025-06-22 09:00:00+02', 'C-02'), "
        "(15, timestamptz '2026-01-14 09:00:00+01', 'D-01'), "
        "(16, timestamptz '2026-06-18 09:00:00+02', 'D-02') "
        ") AS r(rok_id, dt, sala) WHERE NOT EXISTS ( "
        "SELECT 1 FROM ispitni_termin ex WHERE ex.predmet_id = p.id "
        "AND ex.ispitni_rok_id = r.rok_id AND ex.datum_vreme = r.dt);"
    )
    w("SELECT setval(pg_get_serial_sequence('ispitni_termin','id'), (SELECT MAX(id) FROM ispitni_termin));")
    w("")

    # Korisnici: email demo26_{stub}_{year}_{nn}@ftn.rs — jedinstveno
    w("-- Novi studenti (više generacija × smer iznad)")
    vals_k: list[str] = []
    n = 0
    indeks_by_slot: list[tuple[str, int, int, str]] = []
    for prog_id, stub, indeks_prefix in STUDENT_PROGRAMS:
        for year in YEARS:
            for seq in range(1, STUDENTS_PER_PROGRAM_YEAR + 1):
                n += 1
                email = f"demo26_{stub}_{year}_{seq:03d}@ftn.rs"
                ime = IMENA[seq % len(IMENA)]
                prez = f"Demo{n}"
                vals_k.append(
                    f"('{esc(email)}', '{DEMO_LOZINKA}', '{ime}', '{esc(prez)}', 'STUDENT')"
                )
                indeks = f"{indeks_prefix} Z{seq:04d}/{year % 100}"
                indeks_by_slot.append((email, prog_id, year, indeks))

    BATCH = 400
    for i in range(0, len(vals_k), BATCH):
        chunk = vals_k[i : i + BATCH]
        w("INSERT INTO korisnik (email, lozinka_hash, ime, prezime, uloga) VALUES ")
        w(",\n".join(chunk))
        w(" ON CONFLICT (email) DO NOTHING;")
    w("SELECT setval(pg_get_serial_sequence('korisnik','id'), (SELECT MAX(id) FROM korisnik));")
    w("")

    vals_s: list[str] = []
    for email, prog_id, year, indeks in indeks_by_slot:
        vals_s.append(
            f"((SELECT id FROM korisnik WHERE email = '{esc(email)}'), "
            f"'{esc(indeks)}', {prog_id}, {year})"
        )

    for i in range(0, len(vals_s), BATCH):
        chunk = vals_s[i : i + BATCH]
        w(
            "INSERT INTO student (korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) "
            "SELECT * FROM (VALUES "
            + ",\n".join(chunk)
            + ") AS v(kid, indeks, prog, y) WHERE kid IS NOT NULL "
            "ON CONFLICT (broj_indeksa) DO NOTHING;"
        )
    w("SELECT setval(pg_get_serial_sequence('student','id'), (SELECT MAX(id) FROM student));")
    w("")

    allowed = build_allowed_demo_rows(all_expand)
    vals_allowed = allowed_values_sql(allowed)

    w("-- Glavni sloj ocena za demo studente (linearni raspored + školske godine u skupu dozvoljenih)")
    w(
        f"WITH allowed_demo_linear(program_id, predmet_sifra, godina_upisa) AS (\n"
        f"  VALUES\n    {vals_allowed}\n)\n"
        "INSERT INTO ocena (student_id, ispitni_termin_id, poeni, vrednost_ocene) "
        "SELECT q.student_id, q.ispitni_termin_id, q.poeni, q.vrednost_ocene FROM ( "
        "  SELECT DISTINCT ON (s.id, it.id) s.id AS student_id, it.id AS ispitni_termin_id, "
        "    (18 + (abs(hashtext(concat_ws(':', s.id, it.id, 'p')))::bigint % 55))::int AS poeni, "
        "    CASE WHEN (abs(hashtext(concat_ws('g', s.id, it.predmet_id))) % 100) < "
        "      (38 + (abs(hashtext(it.predmet_id::text)) % 45)) "
        "      THEN 6 + (abs(hashtext(concat_ws('z', s.id, it.id))) % 5) ELSE 5 END AS vrednost_ocene "
        "  FROM student s "
        "  JOIN allowed_demo_linear a ON a.program_id = s.studijski_program_id "
        "    AND a.godina_upisa = s.godina_upisa "
        "  JOIN predmet pr ON pr.studijski_program_id = s.studijski_program_id "
        "    AND pr.sifra = a.predmet_sifra "
        "  JOIN ispitni_termin it ON it.predmet_id = pr.id "
        "  JOIN ispitni_rok ir ON ir.id = it.ispitni_rok_id "
        "  JOIN korisnik ku ON ku.id = s.korisnik_id AND ku.email LIKE 'demo26_%@ftn.rs' "
        "  WHERE split_part(ir.skolska_godina, '/', 1)::int >= s.godina_upisa "
        f"    AND split_part(ir.skolska_godina, '/', 1)::int <= {REFERENCE_INTAKE_YEAR} "
        "    AND nais_ocena_je_u_redu(s.godina_upisa, it.datum_vreme, pr.kurikulum_godina, pr.kurikulum_semestar) "
        "    AND (abs(hashtext(concat_ws('t', s.id, it.predmet_id, it.ispitni_rok_id))) % 10) < 7 "
        ") q WHERE NOT EXISTS ( "
        "  SELECT 1 FROM ocena o WHERE o.student_id = q.student_id "
        "  AND o.ispitni_termin_id = q.ispitni_termin_id);"
    )
    w("")

    w("-- Drugi pokušaj posle petice (isti predmet, kasniji termin) — uz linearni + školsku godinu")
    w(
        f"WITH allowed_demo_linear(program_id, predmet_sifra, godina_upisa) AS (\n"
        f"  VALUES\n    {vals_allowed}\n)\n"
        "INSERT INTO ocena (student_id, ispitni_termin_id, poeni, vrednost_ocene) "
        "SELECT q.student_id, it2.id, q.poeni2, q.oc2 FROM ( "
        "  SELECT o.student_id, it.predmet_id, s.studijski_program_id, s.godina_upisa, "
        "    (22 + (abs(hashtext(concat_ws('r', o.student_id, it.predmet_id))) % 50))::int AS poeni2, "
        "    CASE WHEN (abs(hashtext(concat_ws('q', o.student_id, it.predmet_id, 'retry'))) % 100) < 55 "
        "      THEN 6 + (abs(hashtext(concat_ws('v', o.student_id, min(it.id)::text))) % 5) ELSE 5 END AS oc2, "
        "    min(it.id) AS first_tid "
        "  FROM ocena o JOIN ispitni_termin it ON o.ispitni_termin_id = it.id "
        "  JOIN student s ON s.id = o.student_id "
        "  JOIN korisnik ku ON ku.id = s.korisnik_id AND ku.email LIKE 'demo26_%@ftn.rs' "
        "  WHERE o.vrednost_ocene = 5 "
        "    AND (abs(hashtext(concat_ws('m', o.student_id, it.predmet_id))) % 100) < 62 "
        "  GROUP BY o.student_id, it.predmet_id, s.studijski_program_id, s.godina_upisa "
        ") q "
        "JOIN predmet pr ON pr.id = q.predmet_id "
        "JOIN allowed_demo_linear a ON a.program_id = q.studijski_program_id "
        "  AND a.godina_upisa = q.godina_upisa AND a.predmet_sifra = pr.sifra "
        "JOIN ispitni_termin it2 ON it2.predmet_id = q.predmet_id AND it2.id > q.first_tid "
        "JOIN ispitni_rok ir2 ON ir2.id = it2.ispitni_rok_id "
        "WHERE split_part(ir2.skolska_godina, '/', 1)::int >= q.godina_upisa "
        f"  AND split_part(ir2.skolska_godina, '/', 1)::int <= {REFERENCE_INTAKE_YEAR} "
        "  AND nais_ocena_je_u_redu(q.godina_upisa, it2.datum_vreme, pr.kurikulum_godina, pr.kurikulum_semestar) "
        "AND NOT EXISTS (SELECT 1 FROM ocena o2 WHERE o2.student_id = q.student_id "
        "  AND o2.ispitni_termin_id = it2.id) "
        "AND it2.id = (SELECT t.id FROM ispitni_termin t "
        "  JOIN ispitni_rok irx ON irx.id = t.ispitni_rok_id "
        "  WHERE t.predmet_id = q.predmet_id AND t.id > q.first_tid "
        "    AND split_part(irx.skolska_godina, '/', 1)::int >= q.godina_upisa "
        f"    AND split_part(irx.skolska_godina, '/', 1)::int <= {REFERENCE_INTAKE_YEAR} "
        "  ORDER BY t.datum_vreme ASC LIMIT 1);"
    )
    w("")

    w("SELECT setval(pg_get_serial_sequence('ocena','id'), (SELECT MAX(id) FROM ocena));")
    w("COMMIT;")

    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote {OUT} ({OUT.stat().st_size // 1024} KiB), demo students: {len(vals_k)}")


if __name__ == "__main__":
    main()
