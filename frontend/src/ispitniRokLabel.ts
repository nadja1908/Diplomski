/** Šest ispitnih rokova u kalendarskom redosledu (indeks 0..5 u ključu R01..R06). */
const ROK_NAZIVI = [
  'Januarski',
  'Februarski',
  'Aprilski',
  'Junski',
  'Avgustovski',
  'Oktobarski',
] as const

/**
 * Ključ iz backenda nakon agregacije po rokovima: `2025-R02` = Februarski 2025.
 * Stari ključevi `YYYY-MM` (kalendarski mesec) i dalje se mapiraju na odgovarajući rok.
 */
export function formatPassFailTrendLabel(mesec: string): string {
  const rok = mesec.match(/^(\d{4})-R(\d{2})$/)
  if (rok) {
    const godina = rok[1]
    const idx = Number(rok[2], 10)
    const naziv = ROK_NAZIVI[idx - 1]
    return naziv ? `${naziv} ${godina}` : mesec
  }
  const kal = mesec.match(/^(\d{4})-(\d{2})$/)
  if (kal) {
    const y = Number(kal[1], 10)
    const m = Number(kal[2], 10)
    const mapirano = mapKalendarskiMesecNaRok(y, m)
    return mapirano ? `${mapirano.naziv} ${mapirano.godina}` : mesec
  }
  return mesec
}

function mapKalendarskiMesecNaRok(
  godina: number,
  mesec: number,
): { naziv: string; godina: number } | null {
  if (mesec === 12) return { naziv: ROK_NAZIVI[0], godina: godina + 1 }
  if (mesec === 1) return { naziv: ROK_NAZIVI[0], godina }
  if (mesec === 2) return { naziv: ROK_NAZIVI[1], godina }
  if (mesec === 3 || mesec === 4) return { naziv: ROK_NAZIVI[2], godina }
  if (mesec >= 5 && mesec <= 7) return { naziv: ROK_NAZIVI[3], godina }
  if (mesec === 8) return { naziv: ROK_NAZIVI[4], godina }
  if (mesec >= 9 && mesec <= 11) return { naziv: ROK_NAZIVI[5], godina }
  return null
}
