#!/usr/bin/env python3
"""
Generiše 11_academic_demo_seed.sql — briše sve studente i ocene, učitava isključivo
generacije 2022–2025 (30–50 studenata × program × godina), ocene sa prolazima i padovima,
retake-ovima, i ≥36 ESPB položenih predmeta po završenoj kurikulum godini.

Pokretanje (iz repo korena ili scripts/postgres):
  python scripts/postgres/generate_11_academic_demo_seed.py

Zavisi od: curriculum_seed_utils.py, 02_data.sql (parsiranje predmeta).
"""
from __future__ import annotations

import pathlib
import re
from collections import defaultdict
from datetime import date, timedelta

from curriculum_seed_utils import student_moze_polagati_na_datum, ukupno_zavrsenih_semestara

ROOT = pathlib.Path(__file__).resolve().parent
DATA_02 = ROOT / "02_data.sql"
OUT = ROOT / "11_academic_demo_seed.sql"

DEMO_LOZINKA = "$2b$10$8e5XJN5da5YxOCRoJXcPGOfZLuv3/sC2gSxw3hH1DJmiOOZnZd30q"
AS_OF = date(2026, 4, 3)
MIN_ECTS_PER_COMPLETED_YEAR = 36
STUDENTS_PER_PROGRAM_YEAR = 45
YEARS = (2022, 2023, 2024, 2025)
PROGRAMS = [
    (1, "ri", "RI"),
    (2, "si", "SI"),
    (3, "aut", "AUT"),
    (4, "ee", "EE"),
    (5, "rt", "RT"),
    (6, "tk", "TK"),
]
STUB_BY_PROGRAM_ID: dict[int, str] = {pid: stub for pid, stub, _ in PROGRAMS}
IMENA = (
    "Ana Marko Jelena Stefan Milica Nikola Ivana Luka Sara Uroš Teodora Vuk Hana Filip Maša "
    "Nemanja Katarina Petar Andrej Jovana Dušan Milena"
).split()

# Postojeći rok iz 02_data (FK); datum_vreme određuje stvarni trenutak ispita.
ISPITNI_ROK_ID = 6

TERMIN_ID_START = 500_000
SALE = "A-101"


def esc(s: str) -> str:
    return s.replace("'", "''")


def parse_predmet_from_02(text: str) -> list[dict]:
    """Parsira INSERT INTO predmet ... VALUES (id, 'sifra', 'naziv', espb, prog, kat, 'opis', kg, ks),"""
    rows: list[dict] = []
    pat = re.compile(
        r"\((\d+),\s*'([^']+)',\s*'(?:[^']|'')*',\s*(\d+),\s*(\d+),\s*\d+,\s*'(?:[^']|'')*',\s*(\d+),\s*(\d+)\)"
    )
    for m in pat.finditer(text):
        pid, sifra, espb, prog_id, kg, ks = m.groups()
        rows.append(
            {
                "id": int(pid),
                "sifra": sifra,
                "espb": int(espb),
                "studijski_program_id": int(prog_id),
                "kurikulum_godina": int(kg),
                "kurikulum_semestar": int(ks),
            }
        )
    by_key = {(r["studijski_program_id"], r["sifra"]): r for r in rows}
    return list(by_key.values())


def parse_ui_students_from_02(text: str) -> list[dict]:
    """student001@ftn.rs … iz 02_data: ime, prezime, indeks, program, godina_upisa (po korisnik_id)."""
    kid_kor: dict[int, tuple[str, str, str]] = {}
    pat_k = re.compile(
        r"INSERT INTO korisnik \(id, email, lozinka_hash, ime, prezime, uloga\) "
        r"VALUES \((\d+), '(student\d+@ftn\.rs)', '[^']+', '((?:[^']|'')*)', '((?:[^']|'')*)', 'STUDENT'\);"
    )
    for m in pat_k.finditer(text):
        kid = int(m.group(1))
        email = m.group(2)
        ime = m.group(3).replace("''", "'")
        prez = m.group(4).replace("''", "'")
        kid_kor[kid] = (email, ime, prez)

    kid_stu: dict[int, tuple[str, int, int]] = {}
    pat_s = re.compile(
        r"INSERT INTO student \(id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa\) "
        r"VALUES \(\d+, (\d+), '((?:[^']|'')*)', (\d+), (\d+)\);"
    )
    for m in pat_s.finditer(text):
        kid = int(m.group(1))
        bi = m.group(2).replace("''", "'")
        pid = int(m.group(3))
        gu = int(m.group(4))
        kid_stu[kid] = (bi, pid, gu)

    rows: list[dict] = []
    for kid, st in kid_stu.items():
        if kid not in kid_kor:
            continue
        email, ime, prez = kid_kor[kid]
        bi, pid, gu = st
        rows.append(
            {
                "email": email,
                "ime": ime,
                "prezime": prez,
                "broj_indeksa": bi,
                "studijski_program_id": pid,
                "godina_upisa": gu,
            }
        )

    def _n(em: str) -> int:
        return int(em.split("@")[0].removeprefix("student"))

    rows.sort(key=lambda r: _n(r["email"]))
    return rows


def grade_source_demo_email(program_id: int, godina_upisa: int) -> str:
    """Izvor ocena: prvi demo student istog programa; 2021 itd. → 2025 jer demo nema te godine."""
    stub = STUB_BY_PROGRAM_ID[program_id]
    gy = godina_upisa if godina_upisa in YEARS else 2025
    return f"naisademo_{stub}_{gy}_001@ftn.rs"


def parse_x_predmet_from_07(text: str) -> list[dict]:
    rows = []
    pat = re.compile(
        r"VALUES\s*\(\s*'([^']+)'\s*,\s*'(?:[^']|'')*'\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*\d+\s*,\s*'(?:[^']|'')*'\s*,\s*(\d+)\s*,\s*(\d+)\s*\)"
    )
    for m in pat.finditer(text):
        sifra, espb, prog_id, kg, ks = m.groups()
        if not sifra.startswith("X"):
            continue
        rows.append(
            {
                "sifra": sifra,
                "espb": int(espb),
                "studijski_program_id": int(prog_id),
                "kurikulum_godina": int(kg),
                "kurikulum_semestar": int(ks),
            }
        )
    return rows


def subjects_by_program(all_subjects: list[dict]) -> dict[int, list[dict]]:
    m: dict[int, list[dict]] = defaultdict(list)
    for s in all_subjects:
        m[s["studijski_program_id"]].append(s)
    for pid in m:
        m[pid].sort(key=lambda x: (x["kurikulum_godina"], x["kurikulum_semestar"], x["sifra"]))
    return m


def completed_curriculum_years(gu: int, as_of: date) -> int:
    sem = ukupno_zavrsenih_semestara(gu, as_of)
    return min(sem // 2, 4)


def max_allowed_potrebno_sem(gu: int, as_of: date) -> int:
    """Maksimalno (kg-1)*2+ks koje student sme na as_of."""
    sem = ukupno_zavrsenih_semestara(gu, as_of)
    return sem


def predmet_potrebno_sem(p: dict) -> int:
    kg, ks = p["kurikulum_godina"], p["kurikulum_semestar"]
    return (kg - 1) * 2 + ks


def first_eligible_date(gu: int, kg: int, ks: int, d_start: date, d_end: date) -> date | None:
    d = d_start
    while d <= d_end:
        if student_moze_polagati_na_datum(gu, d, kg, ks):
            return d
        d += timedelta(days=4)
    return None


def next_eligible_after(gu: int, kg: int, ks: int, after: date, d_end: date) -> date | None:
    return first_eligible_date(gu, kg, ks, after + timedelta(days=1), d_end)


def pick_subjects_for_ects(pool: list[dict], min_sum: int) -> list[dict]:
    """Grčki: sortiraj po ESPB opadajuće, uzmi dok suma < min_sum."""
    pool = [p for p in pool]
    pool.sort(key=lambda x: -x["espb"])
    out: list[dict] = []
    s = 0
    for p in pool:
        if s >= min_sum:
            break
        out.append(p)
        s += p["espb"]
    if s < min_sum:
        raise RuntimeError(f"Nedovoljno ESPB u pool-u: {s} < {min_sum}, predmeta={len(pool)}")
    return out


def poeni_za_ocenu(ocena: int) -> int:
    if ocena == 5:
        return 18 + (ocena * 3) % 15
    return min(100, 30 + ocena * 7 + (ocena % 4) * 3)


def main() -> None:
    text02 = DATA_02.read_text(encoding="utf-8")
    ui_students = parse_ui_students_from_02(text02)
    if not ui_students:
        raise RuntimeError("parse_ui_students_from_02: nema redova student*@ftn.rs u 02_data.sql")
    subj = parse_predmet_from_02(text02)
    path07 = ROOT / "07_rich_demo_seed.sql"
    if path07.exists():
        subj.extend(parse_x_predmet_from_07(path07.read_text(encoding="utf-8")))
    by_prog = subjects_by_program(subj)

    for pid, lst in by_prog.items():
        for kg in range(1, 5):
            tot = sum(p["espb"] for p in lst if p["kurikulum_godina"] == kg)
            if tot < MIN_ECTS_PER_COMPLETED_YEAR:
                raise RuntimeError(f"Program {pid} kg={kg} ukupno ESPB={tot} < {MIN_ECTS_PER_COMPLETED_YEAR}")

    termin_id = TERMIN_ID_START
    termini_sql: list[str] = []
    term_key_to_id: dict[tuple, int] = {}
    tid_to_sifra: dict[int, str] = {}
    ocena_sql: list[str] = []

    def alloc_termin(prog_id: int, sifra: str, on_day: date) -> int:
        nonlocal termin_id
        key = (prog_id, sifra, on_day.toordinal())
        if key in term_key_to_id:
            return term_key_to_id[key]
        tid = termin_id
        termin_id += 1
        term_key_to_id[key] = tid
        tid_to_sifra[tid] = sifra
        dt = f"{on_day.isoformat()}T10:00:00+01:00"
        termini_sql.append(
            f"({tid}, {ISPITNI_ROK_ID}, "
            f"(SELECT id FROM predmet WHERE studijski_program_id = {prog_id} AND sifra = '{esc(sifra)}' LIMIT 1), "
            f"'{dt}'::timestamptz, '{SALE}')"
        )
        return tid

    korisnik_rows: list[str] = []
    student_rows: list[str] = []
    kid = 21
    sid = 1
    slot_index: list[tuple[int, int, int, str, str]] = []

    for prog_id, stub, indeks_prefix in PROGRAMS:
        for gu in YEARS:
            for seq in range(1, STUDENTS_PER_PROGRAM_YEAR + 1):
                email = f"naisademo_{stub}_{gu}_{seq:03d}@ftn.rs"
                ime = IMENA[seq % len(IMENA)]
                prez = f"Demo{sid}"
                korisnik_rows.append(
                    f"({kid}, '{esc(email)}', '{DEMO_LOZINKA}', '{ime}', '{esc(prez)}', 'STUDENT')"
                )
                indeks = f"{indeks_prefix} A{seq:04d}/{gu % 100}"
                student_rows.append(f"({sid}, {kid}, '{esc(indeks)}', {prog_id}, {gu})")
                slot_index.append((sid, kid, prog_id, gu, email))
                kid += 1
                sid += 1

    rng_state: dict[int, int] = {}

    def rng(student_sid: int, salt: str) -> int:
        h = hash((student_sid, salt)) & 0xFFFFFFFF
        rng_state[student_sid] = (rng_state.get(student_sid, 0) + h) % 10007
        return rng_state[student_sid]

    for student_sid, _k, prog_id, gu, _email in slot_index:
        subs = by_prog[prog_id]
        max_need = max_allowed_potrebno_sem(gu, AS_OF)
        completed_y = completed_curriculum_years(gu, AS_OF)

        allowed = [p for p in subs if predmet_potrebno_sem(p) <= max_need]
        if not allowed:
            raise RuntimeError(f"nema dozvoljenih predmeta sid={student_sid} gu={gu}")

        used_termin_pairs: set[tuple[int, int]] = set()
        planned: list[tuple[int, int, int, date]] = []

        def add_ocena(tid: int, ocena: int) -> None:
            if (student_sid, tid) in used_termin_pairs:
                return
            used_termin_pairs.add((student_sid, tid))
            ocena_sql.append(
                f"({student_sid}, {tid}, {poeni_za_ocenu(ocena)}, {ocena})"
            )

        must_pass_sifre: set[str] = set()
        must_fail_extra: list[tuple[str, date]] = []

        for kg_need in range(1, completed_y + 1):
            pool_kg = [p for p in subs if p["kurikulum_godina"] == kg_need]
            chosen = pick_subjects_for_ects(pool_kg, MIN_ECTS_PER_COMPLETED_YEAR)
            for p in chosen:
                must_pass_sifre.add(p["sifra"])

        skill = rng(student_sid, "skill") % 100

        for sifra in sorted(must_pass_sifre):
            p = next(x for x in subs if x["sifra"] == sifra)
            kg, ks = p["kurikulum_godina"], p["kurikulum_semestar"]
            d0 = date(gu, 10, 1)
            d1 = AS_OF
            d_pass = first_eligible_date(gu, kg, ks, d0, d1)
            if d_pass is None:
                raise RuntimeError(f"no eligible pass date {sifra} gu={gu}")
            want_fail = skill < 35 and (rng(student_sid, sifra + "f") % 3 == 0)
            if want_fail:
                d_fail = first_eligible_date(gu, kg, ks, d0, d_pass - timedelta(days=14))
                if d_fail:
                    tid_f = alloc_termin(prog_id, sifra, d_fail)
                    planned.append((tid_f, 5, student_sid, d_fail))
                    d2 = next_eligible_after(gu, kg, ks, d_fail, d1)
                    if d2 and d2 <= d_pass:
                        d_pass = d2
            tid_p = alloc_termin(prog_id, sifra, d_pass)
            oc_val = 8 if skill > 70 else (7 if skill > 45 else 6 + rng(student_sid, sifra + "g") % 3)
            planned.append((tid_p, oc_val, student_sid, d_pass))

        extra_pool = [p for p in allowed if p["sifra"] not in must_pass_sifre]
        extra_pool.sort(key=lambda x: x["sifra"])
        for j, p in enumerate(extra_pool):
            r = rng(student_sid, f"ex{j}")
            if r % 5 == 0:
                continue
            kg, ks = p["kurikulum_godina"], p["kurikulum_semestar"]
            d_pass = first_eligible_date(gu, kg, ks, date(gu, 10, 1), AS_OF)
            if d_pass is None:
                continue
            if r % 4 == 0:
                d_fail = first_eligible_date(gu, kg, ks, date(gu, 10, 1), d_pass - timedelta(days=10))
                if d_fail:
                    tid_f = alloc_termin(prog_id, p["sifra"], d_fail)
                    planned.append((tid_f, 5, student_sid, d_fail))
            if r % 7 != 1:
                tid_p = alloc_termin(prog_id, p["sifra"], d_pass)
                planned.append((tid_p, 6 + (r % 5), student_sid, d_pass))
            else:
                must_fail_extra.append((p["sifra"], d_pass))

        for sifra, d_pass in must_fail_extra[:1]:
            p = next(x for x in subs if x["sifra"] == sifra)
            kg, ks = p["kurikulum_godina"], p["kurikulum_semestar"]
            d_fail = first_eligible_date(
                gu, kg, ks, date(gu, 10, 1), min(d_pass - timedelta(days=5), AS_OF)
            )
            if d_fail:
                tid_f = alloc_termin(prog_id, sifra, d_fail)
                planned.append((tid_f, 5, student_sid, d_fail))

        has_fail = any(o == 5 for _, o, _, _ in planned)
        if not has_fail and allowed:
            p = allowed[rng(student_sid, "forcefail") % len(allowed)]
            kg, ks = p["kurikulum_godina"], p["kurikulum_semestar"]
            d_fail = first_eligible_date(gu, kg, ks, date(gu, 10, 1), AS_OF)
            if d_fail:
                tid_f = alloc_termin(prog_id, p["sifra"], d_fail)
                planned.append((tid_f, 5, student_sid, d_fail))

        for tid, ocena, _sid, _d in sorted(planned, key=lambda x: (x[3], x[0])):
            add_ocena(tid, ocena)

        sifra_to_p = {p["sifra"]: p for p in subs}
        passed_by_kg: dict[int, set[str]] = defaultdict(set)
        has_pass = False
        has_fail = False
        for tid, ocena, _, _ in planned:
            sf = tid_to_sifra.get(tid)
            if sf is None or sf not in sifra_to_p:
                continue
            pk = sifra_to_p[sf]["kurikulum_godina"]
            if ocena >= 6:
                has_pass = True
                passed_by_kg[pk].add(sf)
            else:
                has_fail = True
        for kg_need in range(1, completed_y + 1):
            tot = sum(sifra_to_p[s]["espb"] for s in passed_by_kg[kg_need])
            if tot < MIN_ECTS_PER_COMPLETED_YEAR:
                raise RuntimeError(
                    f"ECTS kg={kg_need}={tot} < {MIN_ECTS_PER_COMPLETED_YEAR} "
                    f"sid={student_sid} gu={gu} prog={prog_id}"
                )
        if not has_pass or not has_fail:
            raise RuntimeError(f"student sid={student_sid} missing pass or fail (pass={has_pass} fail={has_fail})")

    lines: list[str] = []
    w = lines.append
    w("-- Demo akademski seed: samo generacije 2022–2025, guste ocene, ≥36 ESPB po završenoj kurikulum godini.")
    w("-- Generisano: generate_11_academic_demo_seed.py")
    w("BEGIN;")
    w("")
    w("DELETE FROM ocena;")
    w(
        f"DELETE FROM ispitni_termin WHERE id >= {TERMIN_ID_START}; "
        "-- ponovno pokretanje skripte: inače INSERT istih id (npr. 500000) puca na pkey"
    )
    w("DELETE FROM student;")
    w("DELETE FROM korisnik WHERE uloga = 'STUDENT';")
    w("")
    w("INSERT INTO korisnik (id, email, lozinka_hash, ime, prezime, uloga) VALUES")
    w(",\n".join(korisnik_rows) + ";")
    w("")
    w("INSERT INTO student (id, korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) VALUES")
    w(",\n".join(student_rows) + ";")
    w("")
    w(
        "INSERT INTO ispitni_termin (id, ispitni_rok_id, predmet_id, datum_vreme, sala) VALUES\n"
        + ",\n".join(termini_sql)
        + ";"
    )
    w("")
    BATCH_O = 800
    for i in range(0, len(ocena_sql), BATCH_O):
        chunk = ocena_sql[i : i + BATCH_O]
        w("INSERT INTO ocena (student_id, ispitni_termin_id, poeni, vrednost_ocene) VALUES")
        w(",\n".join(chunk) + ";")
    w("")
    w(
        "-- UI nalozi student001… iz 02_data / student123 (isti hash); indeks i program kao u seed-u. "
        "Ocene: kopija od naisademo_{program}_{godina}_001; godina upisa van 2022–2025 → izvor 2025."
    )
    kor_vals = [
        f"('{esc(u['email'])}', '{DEMO_LOZINKA}', '{esc(u['ime'])}', '{esc(u['prezime'])}', 'STUDENT')"
        for u in ui_students
    ]
    w("INSERT INTO korisnik (email, lozinka_hash, ime, prezime, uloga) VALUES")
    w(",\n".join(kor_vals))
    w(
        "ON CONFLICT (email) DO UPDATE SET "
        "lozinka_hash = EXCLUDED.lozinka_hash, ime = EXCLUDED.ime, prezime = EXCLUDED.prezime;"
    )
    for u in ui_students:
        em = esc(u["email"])
        bi = esc(u["broj_indeksa"])
        pid = u["studijski_program_id"]
        gu = u["godina_upisa"]
        src = grade_source_demo_email(pid, gu)
        w(
            "INSERT INTO student (korisnik_id, broj_indeksa, studijski_program_id, godina_upisa) "
            f"SELECT k.id, '{bi}', {pid}, {gu} FROM korisnik k WHERE k.email = '{em}' "
            "AND NOT EXISTS (SELECT 1 FROM student s WHERE s.korisnik_id = k.id);"
        )
        w(
            "INSERT INTO ocena (student_id, ispitni_termin_id, poeni, vrednost_ocene) "
            "SELECT s_doc.id, o.ispitni_termin_id, o.poeni, o.vrednost_ocene "
            "FROM ocena o "
            "JOIN student s_src ON s_src.id = ( "
            "  SELECT s.id FROM student s "
            "  JOIN korisnik k ON k.id = s.korisnik_id "
            f"  WHERE k.email = '{esc(src)}' LIMIT 1 "
            ") "
            "JOIN student s_doc ON s_doc.korisnik_id = ( "
            f"  SELECT id FROM korisnik WHERE email = '{em}' LIMIT 1 "
            ") "
            "WHERE o.student_id = s_src.id "
            "AND NOT EXISTS ( "
            "  SELECT 1 FROM ocena ex "
            "  WHERE ex.student_id = s_doc.id AND ex.ispitni_termin_id = o.ispitni_termin_id "
            ");"
        )
    w("")
    w("SELECT setval(pg_get_serial_sequence('korisnik','id'), (SELECT MAX(id) FROM korisnik));")
    w("SELECT setval(pg_get_serial_sequence('student','id'), (SELECT MAX(id) FROM student));")
    w("SELECT setval(pg_get_serial_sequence('ispitni_termin','id'), (SELECT MAX(id) FROM ispitni_termin));")
    w("SELECT setval(pg_get_serial_sequence('ocena','id'), (SELECT MAX(id) FROM ocena));")
    w("COMMIT;")

    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(
        f"Wrote {OUT} ({OUT.stat().st_size // 1024} KiB), "
        f"students={len(student_rows)}, termini={len(termini_sql)}, ocena={len(ocena_sql)}"
    )


if __name__ == "__main__":
    main()
