export const REFERENCE_INTAKE_YEAR = 2025

export const SHEF_DOPUSTENE_GENERACIJE_UPISA = [2025, 2024, 2023, 2022] as const

export function strogiLinearniModelZaGeneraciju(godinaUpisa: number | null | undefined): boolean {
  return godinaUpisa != null && godinaUpisa > 2021
}

export function ocekivanaGodinaKurikulumaZaGeneraciju(godinaUpisa: number): 1 | 2 | 3 | 4 {
  const y = REFERENCE_INTAKE_YEAR - godinaUpisa + 1
  return Math.max(1, Math.min(4, y)) as 1 | 2 | 3 | 4
}

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

export const GODINE_UPISA_KARTICE = [2025, 2024, 2023, 2022, 2021] as const

export function naslovGodineUpisa(godinaUpisaBucket: number): string {
  if (godinaUpisaBucket <= 2021) return 'Generacija upisa 2021 i ranije'
  return `Generacija upisa ${godinaUpisaBucket}`
}

export function bucketGodinaUpisa(godinaUpisa: number): number {
  return godinaUpisa <= 2021 ? 2021 : godinaUpisa
}
