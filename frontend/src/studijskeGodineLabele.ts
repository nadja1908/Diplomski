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
