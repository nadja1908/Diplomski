import type { HeadStudentRow } from './headTypes'
import { REFERENCE_INTAKE_YEAR } from './studijskeGodineLabele'

const MIN_SHEF_GEN = 2022

/** Parsiranje godine upisa iz API-ja ili sufiksa indeksa /YY. */
export function godinaUpisaIzStudenta(s: HeadStudentRow): number {
  if (typeof s.godinaUpisa === 'number' && !Number.isNaN(s.godinaUpisa)) return s.godinaUpisa
  const m = /\/(\d{2})$/.exec(s.brojIndeksa.trim())
  if (!m) return MIN_SHEF_GEN
  return 2000 + parseInt(m[1], 10)
}

export function studentJeUShefGeneracijama(s: HeadStudentRow): boolean {
  const g = godinaUpisaIzStudenta(s)
  return g >= MIN_SHEF_GEN && g <= REFERENCE_INTAKE_YEAR
}
