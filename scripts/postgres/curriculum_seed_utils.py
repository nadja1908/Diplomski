"""
Realistic curriculum placement for seed data: caps align with backend
CurriculumIntegrityService (max 45 / program, 12 / year, 6 / semester).
"""
from __future__ import annotations

from collections import defaultdict
from datetime import date

# Occupancy from scripts/postgres/02_data.sql (predmet rows only), per (kurikulum_godina, kurikulum_semestar).
BASE_02_KURIKULUM_COUNTS: dict[int, dict[tuple[int, int], int]] = {
    1: {(1, 1): 6, (1, 2): 5, (2, 1): 6, (2, 2): 5, (3, 1): 6, (3, 2): 5, (4, 1): 5, (4, 2): 4},
    2: {(1, 1): 6, (1, 2): 5, (2, 1): 6, (2, 2): 5, (3, 1): 5, (3, 2): 5, (4, 1): 4, (4, 2): 4},
    3: {(1, 1): 6, (1, 2): 5, (2, 1): 6, (2, 2): 5, (3, 1): 5, (3, 2): 5, (4, 1): 4, (4, 2): 4},
    4: {(1, 1): 6, (1, 2): 5, (2, 1): 6, (2, 2): 5, (3, 1): 5, (3, 2): 5, (4, 1): 4, (4, 2): 4},
    5: {(1, 1): 6, (1, 2): 5, (2, 1): 6, (2, 2): 5, (3, 1): 5, (3, 2): 5, (4, 1): 4, (4, 2): 4},
    6: {(1, 1): 6, (1, 2): 5, (2, 1): 6, (2, 2): 5, (3, 1): 5, (3, 2): 5, (4, 1): 4, (4, 2): 4},
}

MAX_SUBJECTS_PER_CURRICULUM_YEAR = 12
MAX_SUBJECTS_PER_CURRICULUM_SEMESTER = 6


def allocate_demo_x_kurikulum_slots(prog_id: int, n_new: int) -> list[tuple[int, int]]:
    """
    Next n_new (godina, semestar) slots for X* demo predmeti, after real 02_data layout.
    Greedy: fill later kurikulum years first (4→1), semestar 1 then 2; respects 12/year and 6/semester.
    """
    if n_new <= 0:
        return []
    base = BASE_02_KURIKULUM_COUNTS[prog_id]
    occ: dict[tuple[int, int], int] = defaultdict(int)
    for k, v in base.items():
        occ[k] = v
    out: list[tuple[int, int]] = []
    for _ in range(n_new):
        chosen: tuple[int, int] | None = None
        for y in (4, 3, 2, 1):
            y_tot = occ[(y, 1)] + occ[(y, 2)]
            if y_tot >= MAX_SUBJECTS_PER_CURRICULUM_YEAR:
                continue
            for s in (1, 2):
                if occ[(y, s)] >= MAX_SUBJECTS_PER_CURRICULUM_SEMESTER:
                    continue
                chosen = (y, s)
                break
            if chosen:
                break
        if chosen is None:
            raise RuntimeError(f"cannot place demo X module for prog {prog_id}: caps exceeded")
        occ[chosen] += 1
        out.append(chosen)
    return out


def procenjena_godina_studija_akademska(godina_upisa: int, on_day: date) -> int:
    """Godina studija 1..6; školska godina počinje oktobrom (usklađeno sa AcademicProgressionRules)."""
    cy, cm = on_day.year, on_day.month
    pocetak = cy if cm >= 10 else cy - 1
    g = pocetak - godina_upisa + 1
    return max(1, min(6, g))


def zavrseni_semestri_u_tekucoj_godini_studija(on_day: date) -> int:
    m = on_day.month
    if m >= 10 or m == 1:
        return 0
    if 2 <= m <= 6:
        return 1
    return 2


def ukupno_zavrsenih_semestara(godina_upisa: int, on_day: date) -> int:
    gs = procenjena_godina_studija_akademska(godina_upisa, on_day)
    u_godini = zavrseni_semestri_u_tekucoj_godini_studija(on_day)
    return 2 * (gs - 1) + u_godini


def student_moze_polagati_na_datum(
    godina_upisa: int, on_day: date, kurikulum_godina: int, kurikulum_semestar: int
) -> bool:
    potrebno = (kurikulum_godina - 1) * 2 + kurikulum_semestar
    return ukupno_zavrsenih_semestara(godina_upisa, on_day) >= potrebno


# Year 1–3: 8–12 subjects each; year 4: 6–10; total n <= 45.
_YLO = (8, 8, 8, 6)
_YHI = (12, 12, 12, 10)


def year_counts_for_program(n: int) -> list[int]:
    if n < 1 or n > 45:
        raise ValueError(f"n must be 1..45, got {n}")
    yc = list(_YLO)
    rem = n - sum(yc)
    if rem < 0:
        raise ValueError(f"n {n} below minimum footprint {sum(_YLO)}")
    steps = 0
    while rem > 0 and steps < 500:
        for j in range(4):
            if rem <= 0:
                break
            if yc[j] < _YHI[j]:
                yc[j] += 1
                rem -= 1
        steps += 1
    if rem != 0 or sum(yc) != n:
        raise RuntimeError(f"failed to distribute n={n}, yc={yc}, rem={rem}")
    return yc


def flat_slots(n: int) -> list[tuple[int, int]]:
    """For n subjects in order, return list of (kurikulum_godina, kurikulum_semestar)."""
    yc = year_counts_for_program(n)
    out: list[tuple[int, int]] = []
    for yi, cnt in enumerate(yc):
        y = yi + 1
        n1 = (cnt + 1) // 2
        for k in range(cnt):
            sem = 1 if k < n1 else 2
            out.append((y, sem))
    assert len(out) == n
    for y, sem in out:
        if not (1 <= y <= 4 and sem in (1, 2)):
            raise RuntimeError((y, sem))
    return out


__all__ = [
    "BASE_02_KURIKULUM_COUNTS",
    "allocate_demo_x_kurikulum_slots",
    "flat_slots",
    "procenjena_godina_studija_akademska",
    "student_moze_polagati_na_datum",
    "ukupno_zavrsenih_semestara",
    "year_counts_for_program",
    "zavrseni_semestri_u_tekucoj_godini_studija",
]
