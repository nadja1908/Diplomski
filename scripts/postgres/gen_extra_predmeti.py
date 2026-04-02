# Generates bulk predmet + sadrzaj for programs 2-6 (~40 each). Run: python gen_extra_predmeti.py
from pathlib import Path

rows = []
sid = 55
for i in range(1, 36):
    rows.append(
        (
            sid,
            f"13Q{i:02d}",
            f"SI - modul {i:02d}",
            6 + (i % 3),
            2,
            1,
            "Strukturni predmet programa SI (demo baza).",
        )
    )
    sid += 1
for i in range(1, 39):
    rows.append(
        (
            sid,
            f"13U{i:02d}",
            f"AUT - modul {i:02d}",
            6 + (i % 3),
            3,
            2,
            "Strukturni predmet programa AUT (demo baza).",
        )
    )
    sid += 1
for i in range(1, 39):
    rows.append(
        (
            sid,
            f"13L{i:02d}",
            f"EE - modul {i:02d}",
            6 + (i % 3),
            4,
            3,
            "Strukturni predmet programa EE (demo baza).",
        )
    )
    sid += 1
for i in range(1, 39):
    rows.append(
        (
            sid,
            f"13V{i:02d}",
            f"RT - modul {i:02d}",
            6 + (i % 3),
            5,
            4,
            "Strukturni predmet programa RT (demo baza).",
        )
    )
    sid += 1
for i in range(1, 40):
    rows.append(
        (
            sid,
            f"13M{i:02d}",
            f"TK - modul {i:02d}",
            6 + (i % 3),
            6,
            4,
            "Strukturni predmet programa TK master (demo baza).",
        )
    )
    sid += 1

out_dir = Path(__file__).parent
pred_lines = [
    "INSERT INTO predmet (id, sifra, naziv, espb, studijski_program_id, katedra_id, kratak_opis) VALUES"
]
pred_lines.append(
    ",\n".join(
        f"({r[0]}, '{r[1]}', '{r[2]}', {r[3]}, {r[4]}, {r[5]}, '{r[6]}')" for r in rows
    )
    + ";"
)
(out_dir / "_gen_predmet_extra.sql").write_text("\n".join(pred_lines), encoding="utf-8")

srows = ",\n".join(
    f"({r[0]}, 'Cilj modula (demo): ishodi studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Kurikulum demo tekst.')"
    for r in rows
)
(out_dir / "_gen_sadrzaj_extra.sql").write_text(
    "INSERT INTO sadrzaj_predmeta (predmet_id, cilj, ishodi_ucenja, metode_nastave, teme_kursa) VALUES\n"
    + srows
    + ";",
    encoding="utf-8",
)
print(len(rows), "predmeta, max id", rows[-1][0])
