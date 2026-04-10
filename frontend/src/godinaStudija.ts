import type { CurriculumProgress, CurriculumSubject } from './studentTypes'
import { predstavniPoeniZaOcenu } from './poeniSkala'

function effectiveNajboljiPoeni(p: Pick<CurriculumSubject, 'najboljiPoeni' | 'najboljaOcena'>): number | null {
  if (p.najboljiPoeni != null) return p.najboljiPoeni
  if (p.najboljaOcena == null) return null
  return predstavniPoeniZaOcenu(p.najboljaOcena)
}

export function godinaStudijaBlokKurikuluma(redniBrojOdNule: number): number {
  if (redniBrojOdNule < 0) return 1
  const blokGodina = Math.min(4, Math.floor(redniBrojOdNule / 10) + 1)
  if (blokGodina === 1) return 4
  if (blokGodina === 4) return 1
  return blokGodina
}

function curriculumStatusForRow(
  gs: number,
  procenjenaGodina: number,
  najboljaOcena: number | null,
): CurriculumSubject['status'] {
  if (najboljaOcena != null) {
    return najboljaOcena >= 6 ? 'POLOZENO' : 'PALI'
  }
  return gs > procenjenaGodina ? 'KASNIJE' : 'BEZ_IZLAZAKA'
}

export function normalizeCurriculumFromApi(raw: CurriculumProgress): CurriculumProgress {
  const sorted = [...(raw.predmeti ?? [])].sort((a, b) => a.sifra.localeCompare(b.sifra, 'sr-Latn'))
  const procenjena = raw.procenjenaGodinaStudija

  const predmeti = sorted.map((p, i) => {
    if (typeof p.semestar === 'number' && p.status && typeof p.godinaStudija === 'number') {
      return { ...p, izlasci: p.izlasci ?? [], najboljiPoeni: effectiveNajboljiPoeni(p) }
    }
    const gs = godinaStudijaBlokKurikuluma(i)
    const status = curriculumStatusForRow(gs, procenjena, p.najboljaOcena)
    return {
      ...p,
      godinaStudija: gs,
      status,
      izlasci: p.izlasci ?? [],
      najboljiPoeni: effectiveNajboljiPoeni(p),
    }
  })

  let brojPolozenih = 0
  let brojNepolozenih = 0
  let brojBezIzlaska = 0
  let brojPredmetaKasnije = 0
  for (const row of predmeti) {
    switch (row.status) {
      case 'POLOZENO':
        brojPolozenih++
        break
      case 'PALI':
        brojNepolozenih++
        break
      case 'BEZ_IZLAZAKA':
        brojBezIzlaska++
        break
      case 'KASNIJE':
        brojPredmetaKasnije++
        break
      default:
        break
    }
  }

  return {
    ...raw,
    studijskiProgramSifra: raw.studijskiProgramSifra ?? '',
    studijskiProgramNaziv: raw.studijskiProgramNaziv ?? '',
    ukupnoPredmetaNaProgramu: raw.ukupnoPredmetaNaProgramu ?? predmeti.length,
    brojPolozenih,
    brojNepolozenih,
    brojBezIzlaska,
    brojPredmetaKasnije,
    predmeti,
  }
}
