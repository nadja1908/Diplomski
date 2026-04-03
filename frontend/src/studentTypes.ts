/** Oblik JSON-a sa Spring (Java record) endpointa za studenta. */

export type StudentProfile = {
  ime: string
  prezime: string
  email: string
  brojIndeksa: string
  studijskiProgramNaziv: string
  studijskiProgramSifra: string
  katedraNaziv: string
  godinaUpisa: number
}

export type SubjectGrade = {
  predmetSifra: string
  predmetNaziv: string
  espb: number
  ocena: number
  poeni: number | null
  datumIspita: string
  ispitniRok: string
}

export type Gpa = {
  /** Prosek ponderisan ESPB bodovima (najbolja ocena po predmetu, samo položeno 6–10). */
  prosekNaEspb: number | null
  /** Aritmetička sredina najboljih ocena po položenom predmetu (6–10). */
  prosekAritmetickiPolozenihPredmeta?: number | null
  zbirEspbPolozenih: number
  /** Evidentirani izlasci (redovi u ocena), posle pravila napredovanja. */
  ukupnoIspita: number
  brojPolozenihPredmeta: number
  ukupnoPredmetaNaProgramu: number
}

export type SubjectStat = {
  predmetId: number
  nazivPredmeta: string
  /** Šifra predmeta (kad je u JSON-u; Cassandrina statistika po programu). */
  sifra?: string | null
  ukupnoPolaganja: number
  polozeno: number
  pali: number
  prosecnaOcena: number | null
  brojOcena: number
}

/** Jedan ispitni izlazak (termin) iz evidencije. */
export type CurriculumAttempt = {
  datumIspita: string
  ispitniRok: string
  ocena: number
  poeni: number | null
}

/** Kurikulum studijskog programa + status po predmetu (najbolji pokušaj). */
export type CurriculumSubject = {
  predmetId: number
  sifra: string
  naziv: string
  espb: number
  godinaStudija: number
  /** I / II semestar u okviru godine kurikuluma (sa backend-a). */
  semestar?: number
  status: 'POLOZENO' | 'PALI' | 'BEZ_IZLAZAKA' | 'KASNIJE'
  najboljaOcena: number | null
  izlasci: CurriculumAttempt[]
}

export type CurriculumProgress = {
  procenjenaGodinaStudija: number
  godinaUpisa: number
  studijskiProgramSifra: string
  studijskiProgramNaziv: string
  ukupnoPredmetaNaProgramu: number
  brojPolozenih: number
  brojNepolozenih: number
  brojBezIzlaska: number
  brojPredmetaKasnije: number
  predmeti: CurriculumSubject[]
}
