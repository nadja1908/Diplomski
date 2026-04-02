export type HeadProgramSummary = {
  id: number
  sifra: string
  naziv: string
}

/** Odgovor na GET /api/head/students (bez query parametra). */
export type HeadStudentsBundle = {
  programi: HeadProgramSummary[]
  studenti: HeadStudentListRow[]
}

export type HeadStudentListRow = {
  id: number
  brojIndeksa: string
  ime: string
  prezime: string
  email: string
  programNaziv: string
  studijskiProgramId: number
  studijskiProgramSifra: string
}

export type HeadStudentRow = {
  id: number
  brojIndeksa: string
  ime: string
  prezime: string
  email: string
  procenjenaGodina: number
  /** Godina upisa (kalendarska); sufiks indeksa /YY = poslednja dva cifre (ako backend ne pošalje, parsira se iz indeksa). */
  godinaUpisa?: number
}

export type HeadPredmetRow = {
  id: number
  sifra: string
  naziv: string
  espb: number
  godinaStudija: number
  /** 1 ili 2 — podela godine kurikuluma po šifri (backend). */
  semestar?: number
}

export type HeadPredmetStat = {
  predmetId: number
  sifra: string
  naziv: string
  godinaStudija: number
  semestar?: number
  brojElegibilnihStudenata: number
  brojSaBarJednimIzlaskom: number
  brojPolozenih: number
  brojPali: number
  procenatPolozenihOdIzlazaka: number | null
}

export type HeadProgramPregled = {
  program: HeadProgramSummary
  studenti: HeadStudentRow[]
  predmeti: HeadPredmetRow[]
  statistikaPolaganja: HeadPredmetStat[]
  napomenaOUzorku: string
}
