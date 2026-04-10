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
