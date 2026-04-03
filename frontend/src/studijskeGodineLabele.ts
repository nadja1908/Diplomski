/**
 * Referentna godina novog upisnog kruga (1. godina studija). Morati da se poklopi sa
 * `nais.academic.reference-intake-year` u RelationalDatabaseService.
 */
export const REFERENCE_INTAKE_YEAR = 2025

/** Generacije u šefovom UI (analitika, studenti, predmeti) — bez „2021 i ranije“. */
export const SHEF_DOPUSTENE_GENERACIJE_UPISA = [2025, 2024, 2023, 2022] as const

/** Za generacije posle 2021 važi strogi linearni model (bez ponavljanja godine). */
export function strogiLinearniModelZaGeneraciju(godinaUpisa: number | null | undefined): boolean {
  return godinaUpisa != null && godinaUpisa > 2021
}

/** Jedina očekivana godina kurikuluma za datu generaciju upisa (pri R = REFERENCE_INTAKE_YEAR). */
export function ocekivanaGodinaKurikulumaZaGeneraciju(godinaUpisa: number): 1 | 2 | 3 | 4 {
  const y = REFERENCE_INTAKE_YEAR - godinaUpisa + 1
  return Math.max(1, Math.min(4, y)) as 1 | 2 | 3 | 4
}

/**
 * Prikaz kurikulum godina (1–4 + više): red studija + generacija upisa.
 * Godina 1 = prva studijska godina, generacija 2025; …; 5+ = 2021 i ranije.
 */
export function naslovGodineStudija(kurikulumGodina: number): string {
  const map: Record<number, string> = {
    1: 'Prva godina — generacija upisa 2025',
    2: 'Druga godina — generacija upisa 2024',
    3: 'Treća godina — generacija upisa 2023',
    4: 'Četvrta godina — generacija upisa 2022',
  }
  if (map[kurikulumGodina]) return map[kurikulumGodina]
  if (kurikulumGodina >= 5)
    return 'Peta godina i dalje — generacija upisa 2021 i ranije'
  return `Godina studija: ${kurikulumGodina}`
}

/** Naslov godine kurikuluma za studentski portal — samo red studija, bez generacije upisa. */
export function naslovGodineKurikulumaZaStudenta(kurikulumGodina: number): string {
  const map: Record<number, string> = {
    1: 'Prva godina',
    2: 'Druga godina',
    3: 'Treća godina',
    4: 'Četvrta godina',
  }
  if (map[kurikulumGodina]) return map[kurikulumGodina]
  if (kurikulumGodina >= 5) return 'Peta godina i dalje'
  return `Godina studija: ${kurikulumGodina}`
}

/**
 * Kratka kalendarska oznaka iz procenjene godine (1–6), gde se 5/6 tretira kao "2021+ i dalje".
 * Koristi se u meta prikazu (npr. "god. 2022").
 */
export function kalendarskaGodinaZaProcenjenu(procenjenaGodina: number): string {
  if (procenjenaGodina >= 5) return '2021+'
  const map: Record<number, string> = {
    1: '2025',
    2: '2024',
    3: '2023',
    4: '2022',
  }
  return map[procenjenaGodina] ?? String(procenjenaGodina)
}

/** Red kartica u šef portalu — grupa po godini upisa. */
export const GODINE_UPISA_KARTICE = [2025, 2024, 2023, 2022, 2021] as const

export function naslovGodineUpisa(godinaUpisaBucket: number): string {
  if (godinaUpisaBucket <= 2021) return 'Generacija upisa 2021 i ranije'
  return `Generacija upisa ${godinaUpisaBucket}`
}

/** Generacija 2022–2025 u zasebnim kolonama; 2021 i starije u jednu. */
export function bucketGodinaUpisa(godinaUpisa: number): number {
  return godinaUpisa <= 2021 ? 2021 : godinaUpisa
}
