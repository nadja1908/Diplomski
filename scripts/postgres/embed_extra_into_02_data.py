from pathlib import Path

base = Path(__file__).parent
data = (base / "02_data.sql").read_text(encoding="utf-8")
ins1 = (base / "_gen_predmet_extra.sql").read_text(encoding="utf-8")
ins2 = (base / "_gen_sadrzaj_extra.sql").read_text(encoding="utf-8")
needle = (
    "-- Kurikulum: najviše 45 predmeta po programu; godine i semestar usklađeni sa kolonama kurikulum_*.\n"
    "INSERT INTO preduslov"
)
if needle not in data:
    raise SystemExit("needle missing — 02_data.sql changed?")
repl = (
    "-- Kurikulum: najviše 45 predmeta po programu; godine i semestar usklađeni sa kolonama kurikulum_*.\n\n"
    "-- Proširenje: ~40 predmeta po studijskom programu (SI, AUT, EE, RT, TK)\n"
    + ins1
    + "\n\n"
    + ins2
    + "\n\nINSERT INTO preduslov"
)
(base / "02_data.sql").write_text(data.replace(needle, repl, 1), encoding="utf-8")
print("embedded")
