/** Odgovori Columnar (Cassandra) servisa — JSON camelCase kao u Spring record-ima. */

export type ColumnarSubjectStat = {
  predmetId: number
  nazivPredmeta: string
  ukupnoPolaganja: number
  polozeno: number
  pali: number
  prosecnaOcena: number | null
  brojOcena: number
  sifra: string | null
}

export type PassFailMonthPoint = {
  mesec: string
  polozeno: number
  pali: number
}

export type PassFailTrend = {
  meseci: PassFailMonthPoint[]
  /** Godine koje u podacima imaju bar jedan ispitni rok (opadajuće, sa backenda). */
  dostupneGodine?: number[]
  /** Godina čiji se trend prikazuje (poslednja sa podacima ako nije poslat ?godina). */
  izabranaGodina?: number | null
}

export type PerformanceOverview = {
  ukupnoPolozeno: number
  ukupnoPali: number
  najlaksiPredmet: ColumnarSubjectStat | null
  najteziPredmet: ColumnarSubjectStat | null
  brojPredmetaUKatedri: number
}
