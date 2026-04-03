import type { CurriculumProgress, CurriculumSubject } from './studentTypes'

/**
 * @deprecated Samo za starije odgovore API-ja bez polja godina/semestar; kurikulum sada dolazi iz baze.
 */
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

/**
 * Backend šalje godinu kurikuluma, semestar i status; samo sortira i preračunava brojače.
 * Fallback (stari backend): blokovi od 10 predmeta i procenjena godina kao ranije.
 */
export function normalizeCurriculumFromApi(raw: CurriculumProgress): CurriculumProgress {
  const sorted = [...(raw.predmeti ?? [])].sort((a, b) => a.sifra.localeCompare(b.sifra, 'sr-Latn'))
  const procenjena = raw.procenjenaGodinaStudija

  const predmeti = sorted.map((p, i) => {
    if (typeof p.semestar === 'number' && p.status && typeof p.godinaStudija === 'number') {
      return { ...p, izlasci: p.izlasci ?? [] }
    }
    const gs = godinaStudijaBlokKurikuluma(i)
    const status = curriculumStatusForRow(gs, procenjena, p.najboljaOcena)
    return {
      ...p,
      godinaStudija: gs,
      status,
      izlasci: p.izlasci ?? [],
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
