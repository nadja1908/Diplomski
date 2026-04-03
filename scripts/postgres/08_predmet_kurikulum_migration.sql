-- Optional: nadogradnja postojeće baze inicijalizovane starim 01_schema (bez kurikulum_* kolona).
-- Za kvalitet podataka preporučeno je ponovo učitati 01_schema + 02_data.sql.
ALTER TABLE predmet ADD COLUMN IF NOT EXISTS kurikulum_godina INT;
ALTER TABLE predmet ADD COLUMN IF NOT EXISTS kurikulum_semestar INT;
UPDATE predmet SET kurikulum_godina = 1 WHERE kurikulum_godina IS NULL;
UPDATE predmet SET kurikulum_semestar = 1 WHERE kurikulum_semestar IS NULL;
ALTER TABLE predmet ALTER COLUMN kurikulum_godina SET NOT NULL;
ALTER TABLE predmet ALTER COLUMN kurikulum_semestar SET NOT NULL;
ALTER TABLE predmet DROP CONSTRAINT IF EXISTS predmet_kurikulum_godina_check;
ALTER TABLE predmet ADD CONSTRAINT predmet_kurikulum_godina_check CHECK (kurikulum_godina BETWEEN 1 AND 4);
ALTER TABLE predmet DROP CONSTRAINT IF EXISTS predmet_kurikulum_semestar_check;
ALTER TABLE predmet ADD CONSTRAINT predmet_kurikulum_semestar_check CHECK (kurikulum_semestar IN (1, 2));
CREATE INDEX IF NOT EXISTS idx_predmet_kurikulum ON predmet(studijski_program_id, kurikulum_godina, kurikulum_semestar);
