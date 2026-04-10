# Generates bulk predmet + sadrzaj for programs 2-6 (~40 each). Run: python gen_extra_predmeti.py
from pathlib import Path

from curriculum_subject_titles import AUT_NAMES, EE_NAMES, RT_NAMES, SI_NAMES, TK_NAMES

rows = []
sid = 55
for i in range(1, 36):
    rows.append(
        (
            sid,
            f"13Q{i:02d}",
            SI_NAMES[i - 1],
            6 + (i % 3),
            2,
            1,
            "Strukturni predmet softverskog inženjerstva, prema nastavnom planu studijskog programa.",
        )
    )
    sid += 1
for i in range(1, 39):
    rows.append(
        (
            sid,
            f"13U{i:02d}",
            AUT_NAMES[i - 1],
            6 + (i % 3),
            3,
            2,
            "Strukturni predmet automatike, prema nastavnom planu studijskog programa.",
        )
    )
    sid += 1
for i in range(1, 39):
    rows.append(
        (
            sid,
            f"13L{i:02d}",
            EE_NAMES[i - 1],
            6 + (i % 3),
            4,
            3,
            "Strukturni predmet elektrotehnike, prema nastavnom planu studijskog programa.",
        )
    )
    sid += 1
for i in range(1, 39):
    rows.append(
        (
            sid,
            f"13V{i:02d}",
            RT_NAMES[i - 1],
            6 + (i % 3),
            5,
            4,
            "Strukturni predmet računarske tehnike i informatike, prema nastavnom planu studijskog programa.",
        )
    )
    sid += 1
for i in range(1, 40):
    rows.append(
        (
            sid,
            f"13M{i:02d}",
            TK_NAMES[i - 1],
            6 + (i % 3),
            6,
            4,
            "Strukturni predmet master studija telekomunikacija, prema nastavnom planu.",
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
    f"({r[0]}, 'Cilj modula: ostvarivanje definisanih ishoda studijskog programa.', 'Ishodi po nastavnom planu.', 'Predavanja, vezbe, ispit.', 'Sadržaj kursa u skladu sa nastavnim planom i programom.')"
    for r in rows
)
(out_dir / "_gen_sadrzaj_extra.sql").write_text(
    "INSERT INTO sadrzaj_predmeta (predmet_id, cilj, ishodi_ucenja, metode_nastave, teme_kursa) VALUES\n"
    + srows
    + ";",
    encoding="utf-8",
)
print(len(rows), "predmeta, max id", rows[-1][0])
