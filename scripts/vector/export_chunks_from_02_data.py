#!/usr/bin/env python3
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent
DATA_SQL = ROOT / "scripts" / "postgres" / "02_data.sql"
OUT_VECTOR = ROOT / "scripts" / "vector" / "chunks.jsonl"

PRED_ROW = re.compile(
    r"\((\d+),\s*'((?:''|[^'])*)'\s*,\s*'((?:''|[^'])*)'\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*,\s*'((?:''|[^'])*)'\s*,\s*(\d+)\s*,\s*(\d+)\s*\)"
)

SADR_ROW = re.compile(
    r"\((\d+),\s*'((?:''|[^'])*)'\s*,\s*'((?:''|[^'])*)'\s*,\s*'((?:''|[^'])*)'\s*,\s*'((?:''|[^'])*)'\)"
)


def sql_unquote(s: str) -> str:
    return s.replace("''", "'")


def find_insert_statement_end(text: str, start: int) -> int:
    i = start
    in_str = False
    n = len(text)
    while i < n:
        c = text[i]
        if in_str:
            if c == "'":
                if i + 1 < n and text[i + 1] == "'":
                    i += 2
                    continue
                in_str = False
            i += 1
            continue
        if c == "'":
            in_str = True
            i += 1
            continue
        if c == ";":
            return i
        i += 1
    return -1


def parse_blocks(text: str, table: str) -> list[str]:
    out: list[str] = []
    needle = f"INSERT INTO {table}"
    i = 0
    while True:
        start = text.find(needle, i)
        if start < 0:
            break
        semi = find_insert_statement_end(text, start)
        if semi < 0:
            break
        out.append(text[start:semi])
        i = semi + 1
    return out


def main() -> None:
    raw = DATA_SQL.read_text(encoding="utf-8")
    predmeti: dict[int, dict] = {}
    for block in parse_blocks(raw, "predmet"):
        for m in PRED_ROW.finditer(block):
            pid = int(m.group(1))
            predmeti[pid] = {
                "sifra": sql_unquote(m.group(2)),
                "naziv": sql_unquote(m.group(3)),
                "espb": int(m.group(4)),
                "kratak_opis": sql_unquote(m.group(7)),
            }
    sadrzaj: dict[int, dict[str, str]] = {}
    for block in parse_blocks(raw, "sadrzaj_predmeta"):
        for m in SADR_ROW.finditer(block):
            pid = int(m.group(1))
            sadrzaj[pid] = {
                "cilj": sql_unquote(m.group(2)),
                "ishodi_ucenja": sql_unquote(m.group(3)),
                "metode_nastave": sql_unquote(m.group(4)),
                "teme_kursa": sql_unquote(m.group(5)),
            }
    if not predmeti:
        raise SystemExit(f"No predmet rows parsed from {DATA_SQL}")

    chunks: list[dict] = []
    profesor = ""
    for pid in sorted(predmeti.keys()):
        p = predmeti[pid]
        sd = sadrzaj.get(
            pid,
            {"cilj": "", "ishodi_ucenja": "", "metode_nastave": "", "teme_kursa": ""},
        )
        cilj, ishod, metode, teme = (
            sd["cilj"],
            sd["ishodi_ucenja"],
            sd["metode_nastave"],
            sd["teme_kursa"],
        )
        nz, sif, espb, opis = p["naziv"], p["sifra"], p["espb"], p["kratak_opis"]
        embed_text = (
            f"{nz} ({sif}), {espb} ESPB. Predavač: {profesor or 'nije dodeljen u seed podacima'}. "
            f"Kratak opis: {opis} Cilj: {cilj} Ishodi učenja: {ishod} "
            f"Metode nastave: {metode} Sadržaj kursa: {teme}"
        )
        chunks.append(
            {
                "predmet_id": pid,
                "predmet_sifra": sif,
                "predmet_naziv": nz,
                "espb": espb,
                "profesor": profesor,
                "cilj": cilj,
                "ishodi_ucenja": ishod,
                "metode_nastave": metode,
                "teme_kursa": teme,
                "tip": "predmet",
                "text": embed_text,
            }
        )

    OUT_VECTOR.parent.mkdir(parents=True, exist_ok=True)
    with OUT_VECTOR.open("w", encoding="utf-8") as vf:
        for c in chunks:
            vf.write(json.dumps(c, ensure_ascii=False) + "\n")
    print(f"Written {len(chunks)} records -> {OUT_VECTOR}")
    missing_sd = sorted(set(predmeti.keys()) - set(sadrzaj.keys()))
    if missing_sd:
        print(f"Warning: no sadrzaj_predmeta for predmet_id: {missing_sd[:20]}{'…' if len(missing_sd) > 20 else ''}")


if __name__ == "__main__":
    main()
