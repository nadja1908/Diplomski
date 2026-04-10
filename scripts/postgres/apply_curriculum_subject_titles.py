#!/usr/bin/env python3
"""
Zamenjuje generičke nazive tipa 'SI - modul 01' u 02_data.sql stvarnim nazivima predmeta.

  python scripts/postgres/apply_curriculum_subject_titles.py
"""
from __future__ import annotations

from pathlib import Path

from curriculum_subject_titles import AUT_NAMES, EE_NAMES, RT_NAMES, SI_NAMES, TK_NAMES


def esc_sql(s: str) -> str:
    return s.replace("'", "''")


def apply_block(label: str, names: list[str], text: str) -> str:
    for i, name in enumerate(names, start=1):
        old = f"'{label} - modul {i:02d}'"
        new = "'" + esc_sql(name) + "'"
        c = text.count(old)
        if c != 1:
            raise SystemExit(f"{label} {i:02d}: expected 1× {old!r}, found {c}")
        text = text.replace(old, new, 1)
    print(f"OK {label}: {len(names)} predmeta")
    return text


def main() -> None:
    root = Path(__file__).resolve().parent
    path = root / "02_data.sql"
    text = path.read_text(encoding="utf-8")
    assert len(SI_NAMES) == 35
    assert len(AUT_NAMES) == 38
    assert len(EE_NAMES) == 38
    assert len(RT_NAMES) == 38
    assert len(TK_NAMES) == 39
    text = apply_block("SI", SI_NAMES, text)
    text = apply_block("AUT", AUT_NAMES, text)
    text = apply_block("EE", EE_NAMES, text)
    text = apply_block("RT", RT_NAMES, text)
    text = apply_block("TK", TK_NAMES, text)
    if " - modul " in text:
        raise SystemExit("ostali ' - modul ' u fajlu — proveri ručno")
    path.write_text(text, encoding="utf-8", newline="\n")
    print(f"Wrote {path}")


if __name__ == "__main__":
    main()
