/**
 * Kad API nema bodove — sredina opsega za ocenu (6→51–60 … 10→91–100, 5→0–50).
 */
export function predstavniPoeniZaOcenu(ocena: number): number | null {
  switch (ocena) {
    case 5:
      return 45
    case 6:
      return 56
    case 7:
      return 66
    case 8:
      return 76
    case 9:
      return 86
    case 10:
      return 96
    default:
      return null
  }
}

/**
 * Uobičajena skala: 51–60 → šest, 61–70 → sedam, … (ispitni bodovi 0–100).
 * Ispod 51 tretira se kao pet (nepoloženo).
 */
export function opisOceneIzBodova(poeni: number): string {
  const p = Math.floor(poeni)
  if (p < 51) {
    return 'Pet'
  }
  if (p <= 60) {
    return 'Šest'
  }
  if (p <= 70) {
    return 'Sedam'
  }
  if (p <= 80) {
    return 'Osam'
  }
  if (p <= 90) {
    return 'Devet'
  }
  return 'Deset'
}
