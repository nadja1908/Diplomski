import { useMemo, useState } from 'react'
import type { HeadProgramPregled } from './headTypes'
import { SHEF_DOPUSTENE_GENERACIJE_UPISA } from './studijskeGodineLabele'
import { godinaUpisaIzStudenta, studentJeUShefGeneracijama } from './headStudentUtils'

type Props = {
  pregled: HeadProgramPregled | null
  loading: boolean
}

export function HeadStudentsSection({ pregled, loading }: Props) {
  const [gen, setGen] = useState<number>(2025)

  const rows = useMemo(() => {
    if (!pregled) return []
    return pregled.studenti.filter(
      (s) => studentJeUShefGeneracijama(s) && godinaUpisaIzStudenta(s) === gen,
    )
  }, [pregled, gen])

  if (loading) {
    return <p className="loading head-portal-loading">Učitavanje studenata…</p>
  }
  if (!pregled) {
    return <p className="dj-muted">Nema podataka o studentima.</p>
  }

  return (
    <section className="head-section" aria-labelledby="head-students-page-title">
      <h3 id="head-students-page-title" className="head-section-title">
        Studenti
      </h3>
      <p className="dj-card-hint">
        Tabela studenata izabranog programa za jednu generaciju upisa (2022–2025). Godina u indeksu odgovara godini
        upisa (npr. …/25).
      </p>
      <div className="head-program-picker head-stat-godina-picker">
        <label className="head-program-picker-label" htmlFor="head-students-gen">
          Generacija (godina upisa)
        </label>
        <select
          id="head-students-gen"
          className="head-program-select"
          value={gen}
          onChange={(e) => setGen(Number(e.target.value))}
        >
          {SHEF_DOPUSTENE_GENERACIJE_UPISA.map((g) => (
            <option key={g} value={g}>
              {g}
            </option>
          ))}
        </select>
      </div>
      <div className="head-table-wrap">
        <table className="head-stat-table">
          <thead>
            <tr>
              <th>Broj indeksa</th>
              <th>Ime i prezime</th>
              <th>Email</th>
              <th>Proc. god. studija</th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 ? (
              <tr>
                <td colSpan={4} className="head-stat-empty">
                  Nema studenata ove generacije na programu.
                </td>
              </tr>
            ) : (
              rows.map((s) => (
                <tr key={s.id}>
                  <td>{s.brojIndeksa}</td>
                  <td>
                    {s.ime} {s.prezime}
                  </td>
                  <td>{s.email}</td>
                  <td>{s.procenjenaGodina}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  )
}
