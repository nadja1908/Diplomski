/**
 * Ispitni rokovi u prikazu: januar, februar, april, jun, jul, avgust (usklađeno sa FTN rasporedom).
 */
const MESECI_ROKA = [1, 2, 4, 6, 7, 8] as const

function simpleHash(s: string): number {
  let h = 0
  for (let i = 0; i < s.length; i++) {
    h = (Math.imul(31, h) + s.charCodeAt(i)) | 0
  }
  return Math.abs(h)
}

function daysInMonth(year: number, month1to12: number): number {
  return new Date(year, month1to12, 0).getDate()
}

function formatYMDClamped(year: number, month1to12: number, day: number): string {
  const dim = daysInMonth(year, month1to12)
  let d = day
  if (d > dim) d = dim
  if (d < 1) d = 1
  const mm = String(month1to12).padStart(2, '0')
  const dd = String(d).padStart(2, '0')
  return `${year}-${mm}-${dd}`
}

/**
 * Kalendar mesec (1–12) iz ISO datuma → najbliži „tipičan“ mesec ispitnog roka iz skupa MESECI_ROKA.
 */
function priblizanMesecRokaIzKalendara(kalendarMesec: number): (typeof MESECI_ROKA)[number] {
  const m = kalendarMesec
  if (m === 1 || m === 2) return m as (typeof MESECI_ROKA)[number]
  if (m === 3 || m === 4) return 4
  if (m === 5 || m === 6) return 6
  if (m === 7) return 7
  if (m === 8) return 8
  if (m === 9) return 8
  if (m >= 10) return 1
  return 4
}

export function nazivIspitnogRokaZaMesec(mesec: number): string {
  switch (mesec) {
    case 1:
      return 'Januarski ispitni rok'
    case 2:
      return 'Februarski ispitni rok'
    case 4:
      return 'Aprilski ispitni rok'
    case 6:
      return 'Junski ispitni rok'
    case 7:
      return 'Julski ispitni rok'
    case 8:
      return 'Avgustovski ispitni rok'
    default:
      return 'Aprilski ispitni rok'
  }
}

/**
 * Naziv roka i YYYY-MM-DD za prikaz izlaska: raznovrsni rokovi (ne samo april), stabilno po predmetu+datumu,
 * uz blagu varijaciju oko kalendarskog meseca da ostane smisleno.
 */
export function prikazRokaIDatumaZaIzlasak(iso: string, predmetSifra: string): { rok: string; datum: string } {
  if (!iso || iso.length < 10) {
    return { rok: '—', datum: iso?.slice(0, 10) ?? '' }
  }
  const y = parseInt(iso.slice(0, 4), 10)
  const calM = parseInt(iso.slice(5, 7), 10)
  const d = parseInt(iso.slice(8, 10), 10)
  if (!Number.isFinite(y) || !Number.isFinite(calM) || !Number.isFinite(d)) {
    return { rok: '—', datum: iso.slice(0, 10) }
  }

  const base = priblizanMesecRokaIzKalendara(calM)
  let idx0 = MESECI_ROKA.indexOf(base)
  if (idx0 < 0) idx0 = 2

  const h = simpleHash(`${predmetSifra}|${iso}`)
  const delta = (h % 3) - 1
  const idx = (idx0 + delta + MESECI_ROKA.length) % MESECI_ROKA.length
  const pickM = MESECI_ROKA[idx]
  const rok = nazivIspitnogRokaZaMesec(pickM)
  const datum = formatYMDClamped(y, pickM, d)
  return { rok, datum }
}

/**
 * Mesec (1–12) iz naziva ispitnog roka (parsiranje iz API-ja).
 */
export function mesecIzNazivaIspitnogRoka(naziv: string): number | null {
  const n = naziv.toLowerCase()
  if (n.includes('januar')) return 1
  if (n.includes('februar')) return 2
  if (n.includes('mart')) return 3
  if (n.includes('april')) return 4
  if (n.includes('maj')) return 5
  if (n.includes('jun')) return 6
  if (n.includes('jul')) return 7
  if (n.includes('avgust')) return 8
  if (n.includes('septembar') || n.includes('septemb')) return 9
  if (n.includes('oktobar')) return 10
  if (n.includes('novembar')) return 11
  if (n.includes('decembar')) return 12
  return null
}

/**
 * Prikaz datuma YYYY-MM-DD: mesec usklađen sa prosleđenim nazivom roka.
 */
export function formatDatumIspitaSaRokom(iso: string, ispitniRok: string): string {
  if (!iso || iso.length < 10) return iso.slice(0, 10)
  const mRok = mesecIzNazivaIspitnogRoka(ispitniRok)
  if (mRok == null) return iso.slice(0, 10)
  const y = parseInt(iso.slice(0, 4), 10)
  let d = parseInt(iso.slice(8, 10), 10)
  if (!Number.isFinite(y) || !Number.isFinite(d)) return iso.slice(0, 10)
  return formatYMDClamped(y, mRok, d)
}
