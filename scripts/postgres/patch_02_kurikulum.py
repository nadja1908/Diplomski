#!/usr/bin/env python3
"""
Adds kurikulum_godina, kurikulum_semestar to all predmet rows in 02_data.sql
using realistic per-program counts (sorted by šifra).

Run from repo:
  python scripts/postgres/patch_02_kurikulum.py

No subjects removed — current file has at most 42 per program (≤ 45).
"""
from __future__ import annotations

import pathlib
import re
from collections import defaultdict

from curriculum_seed_utils import flat_slots

ROOT = pathlib.Path(__file__).resolve().parent
DATA_SQL = ROOT / "02_data.sql"

PRED_ROW = re.compile(
    r"\((\d+),\s*'((?:''|[^'])*)'\s*,\s*'((?:''|[^'])*)'\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*'((?:''|[^'])*)'"
    r"(?:\s*,\s*(\d+)\s*,\s*(\d+))?\s*\)"
)
SADR_ROW = re.compile(
    r"\((\d+),\s*'((?:''|[^'])*)'\s*,\s*'((?:''|[^'])*)'\s*,\s*'((?:''|[^'])*)'\s*,\s*'((?:''|[^'])*)'\)"
)


def sql_str(s: str) -> str:
    return "'" + s.replace("'", "''") + "'"


def parse_predmets(block: str) -> list[dict]:
    rows = []
    for m in PRED_ROW.finditer(block):
        rows.append(
            {
                "id": int(m.group(1)),
                "sifra": m.group(2).replace("''", "'"),
                "naziv": m.group(3).replace("''", "'"),
                "espb": int(m.group(4)),
                "prog": int(m.group(5)),
                "kad": int(m.group(6)),
                "opis": m.group(7).replace("''", "'"),
            }
        )
    by_id = {r["id"]: r for r in rows}
    if len(by_id) != len(rows):
        raise SystemExit("duplicate predmet id")
    return rows


def parse_sadrzaj(block: str) -> dict[int, dict]:
    out = {}
    for m in SADR_ROW.finditer(block):
        pid = int(m.group(1))
        out[pid] = {
            "cilj": m.group(2).replace("''", "'"),
            "ishodi": m.group(3).replace("''", "'"),
            "metode": m.group(4).replace("''", "'"),
            "teme": m.group(5).replace("''", "'"),
        }
    return out


def assign_curriculum(rows: list[dict]) -> dict[int, tuple[int, int]]:
    by_prog: dict[int, list[dict]] = defaultdict(list)
    for r in rows:
        by_prog[r["prog"]].append(r)
    slots_by_id: dict[int, tuple[int, int]] = {}
    for prog in sorted(by_prog):
        grp = sorted(by_prog[prog], key=lambda x: x["sifra"])
        n = len(grp)
        if n > 45:
            raise SystemExit(f"program {prog} has {n} subjects — run trimmer first")
        slots = flat_slots(n)
        for i, r in enumerate(grp):
            slots_by_id[r["id"]] = slots[i]
    return slots_by_id


def render_curriculum_block(rows: list[dict], sadrzaj: dict[int, dict], slots: dict[int, tuple[int, int]]) -> str:
    lines = []
    insert_hdr = (
        "INSERT INTO predmet (id, sifra, naziv, espb, studijski_program_id, katedra_id, "
        "kratak_opis, kurikulum_godina, kurikulum_semestar) VALUES "
    )
    s_hdr = "INSERT INTO sadrzaj_predmeta (predmet_id, cilj, ishodi_ucenja, metode_nastave, teme_kursa) VALUES "

    rows_sorted = sorted(rows, key=lambda r: (r["prog"], r["sifra"]))
    batch_p: list[str] = []
    batch_s: list[str] = []
    cur_prog = None

    def flush():
        nonlocal batch_p, batch_s
        if batch_p:
            lines.append(insert_hdr + "\n" + ",\n".join(batch_p) + ";")
            lines.append(s_hdr + "\n" + ",\n".join(batch_s) + ";")
            batch_p = []
            batch_s = []

    for r in rows_sorted:
        kg, ks = slots[r["id"]]
        if cur_prog is not None and r["prog"] != cur_prog:
            flush()
        cur_prog = r["prog"]
        paren = (
            f"({r['id']}, {sql_str(r['sifra'])}, {sql_str(r['naziv'])}, {r['espb']}, "
            f"{r['prog']}, {r['kad']}, {sql_str(r['opis'])}, {kg}, {ks})"
        )
        batch_p.append(paren)
        s = sadrzaj[r["id"]]
        sp = (
            f"({r['id']}, {sql_str(s['cilj'])}, {sql_str(s['ishodi'])}, "
            f"{sql_str(s['metode'])}, {sql_str(s['teme'])})"
        )
        batch_s.append(sp)
    flush()

    lines.append("")
    lines.append(
        "-- Demo kurikulum: ≤45 predmeta po programu; godine i semestar usklađeni sa kolonama kurikulum_*."
    )
    return "\n".join(lines) + "\n"


def main() -> None:
    text = DATA_SQL.read_text(encoding="utf-8")
    lines = text.splitlines(keepends=True)
    # Find curriculum region: first INSERT predmet through line before INSERT preduslov
    start = next(i for i, ln in enumerate(lines) if "INSERT INTO predmet" in ln)
    end = next(i for i, ln in enumerate(lines) if ln.strip().startswith("INSERT INTO preduslov"))
    head = "".join(lines[:start])
    tail = "".join(lines[end:])
    block = "".join(lines[start:end])

    rows = parse_predmets(block)
    sadrzaj = parse_sadrzaj(block)
    if set(r["id"] for r in rows) != set(sadrzaj.keys()):
        missing_p = set(sadrzaj) - set(r["id"] for r in rows)
        missing_s = set(r["id"] for r in rows) - set(sadrzaj)
        raise SystemExit(f"sadrzaj/predmet mismatch: {missing_p=} {missing_s=}")
    slots = assign_curriculum(rows)
    new_mid = render_curriculum_block(rows, sadrzaj, slots)
    DATA_SQL.write_text(head + new_mid + tail, encoding="utf-8")
    print(f"Updated {DATA_SQL} ({len(rows)} predmet rows)")


if __name__ == "__main__":
    main()
