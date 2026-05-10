export type HeadProgramSummary = {
  id: number
  sifra: string
  naziv: string
}

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
  godinaUpisa?: number
}

export type HeadPredmetRow = {
  id: number
  sifra: string
  naziv: string
  espb: number
  godinaStudija: number
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

/** Predmet sa sadržajem — šef katedre (CRUD liste) */
export type HeadPredmetDetail = {
  id: number
  sifra: string
  naziv: string
  espb: number
  studijskiProgramId: number
  studijskiProgramSifra: string
  studijskiProgramNaziv: string
  kratakOpis: string
  kurikulumGodina: number
  kurikulumSemestar: number
  cilj: string
  ishodiUcenja: string
  metodeNastave: string
  temeKursa: string
}

export type HeadPredmetUpsertPayload = {
  sifra: string
  naziv: string
  espb: number
  studijskiProgramId: number
  kurikulumGodina: number
  kurikulumSemestar: number
  kratakOpis?: string | null
  cilj?: string | null
  ishodiUcenja?: string | null
  metodeNastave?: string | null
  temeKursa?: string | null
}