import { useCallback, useEffect, useMemo, useState } from 'react'
import { naisApi } from './api'
import type {
  HeadPredmetStat,
  HeadProgramPregled,
  HeadProgramSummary,
  HeadPredmetRow,
  HeadStudentRow,
} from './headTypes'
import {
  GODINE_UPISA_KARTICE,
  bucketGodinaUpisa,
  naslovGodineStudija,
  naslovGodineUpisa,
} from './studijskeGodineLabele'

function JsonBlock({ data }: { data: unknown }) {
  return (
    <pre className="json-out head-advanced-json">
      {data === undefined || data === null ? '—' : JSON.stringify(data, null, 2)}
    </pre>
  )
}

/** Fallback ako stariji backend ne šalje godinaUpisa — /YY na indeksu = godina upisa 20YY. */
function godinaUpisaIzStudenta(s: HeadStudentRow): number {
  if (typeof s.godinaUpisa === 'number' && !Number.isNaN(s.godinaUpisa)) return s.godinaUpisa
  const m = /\/(\d{2})$/.exec(s.brojIndeksa.trim())
  if (!m) return 2021
  return 2000 + parseInt(m[1], 10)
}

function groupStudentsByGodinaUpisa(studenti: HeadStudentRow[]): Map<number, HeadStudentRow[]> {
  const m = new Map<number, HeadStudentRow[]>()
  for (const s of studenti) {
    const b = bucketGodinaUpisa(godinaUpisaIzStudenta(s))
    const list = m.get(b) ?? []
    list.push(s)
    m.set(b, list)
  }
  return m
}

function utezeniProcenatPolozenih(rows: HeadPredmetStat[]): number | null {
  let sum = 0
  let w = 0
  for (const r of rows) {
    const p = r.procenatPolozenihOdIzlazaka
    const n = r.brojSaBarJednimIzlaskom
    if (p == null || n === 0) continue
    sum += p * n
    w += n
  }
  if (w === 0) return null
  return Math.round((sum / w) * 100) / 100
}

function groupPredmetiByGodina(predmeti: HeadPredmetRow[]): Map<number, HeadPredmetRow[]> {
  const m = new Map<number, HeadPredmetRow[]>()
  for (const p of predmeti) {
    const list = m.get(p.godinaStudija) ?? []
    list.push(p)
    m.set(p.godinaStudija, list)
  }
  for (const list of m.values()) {
    list.sort(
      (a, b) =>
        (a.semestar ?? 1) - (b.semestar ?? 1) || a.sifra.localeCompare(b.sifra, 'sr-Latn'),
    )
  }
  return m
}

function oznakaSemestra(sem: number | undefined): string {
  return (sem ?? 1) === 2 ? 'II sem.' : 'I sem.'
}

export function HeadPortal() {
  const [programs, setPrograms] = useState<HeadProgramSummary[]>([])
  const [programId, setProgramId] = useState<number | null>(null)
  const [pregled, setPregled] = useState<HeadProgramPregled | null>(null)
  const [loading, setLoading] = useState(false)
  const [programsErr, setProgramsErr] = useState('')
  const [pregledErr, setPregledErr] = useState('')
  const [advancedPayload, setAdvancedPayload] = useState<unknown>(null)
  const [advancedLoading, setAdvancedLoading] = useState(false)
  const [advancedErr, setAdvancedErr] = useState('')
  const [showAdvanced, setShowAdvanced] = useState(false)
  /** Godina kurikuluma (1–4) za filtriranje statistike polaganja. */
  const [statGodinaKurikuluma, setStatGodinaKurikuluma] = useState<1 | 2 | 3 | 4>(1)
  /** 0 = oba semestra; 1 / 2 = samo taj semestar u okviru izabrane godine. */
  const [statSemestar, setStatSemestar] = useState<0 | 1 | 2>(0)
  /** null = svi studenti programa; inače samo ta godina upisa (2021 = „2021 i ranije“ na backendu). */
  const [statGeneracijaUpisa, setStatGeneracijaUpisa] = useState<number | null>(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setProgramsErr('')
      try {
        const bundle = await naisApi.headStudentsBundle()
        if (cancelled) return
        if (!bundle?.programi || !Array.isArray(bundle.programi)) {
          throw new Error(
            'Backend mora biti ažuriran: GET /api/head/students sada vraća objekat { programi, studenti }.',
          )
        }
        const list = bundle.programi
        setPrograms(list)
        if (list.length > 0) {
          setProgramId((prev) => (prev != null && list.some((p) => p.id === prev) ? prev : list[0].id))
        } else {
          setProgramId(null)
          setPregled(null)
        }
      } catch (e) {
        if (!cancelled) {
          let msg =
            e instanceof Error
              ? e.message
              : 'Ne mogu da učitam podatke katedre (nepoznata greška).'
          if (/403|Forbidden/i.test(msg)) {
            msg +=
              ' Ako ste šef katedre: odjavite se, ponovo se prijavite. Ako koristite Docker + pokrećete servis i iz IDE-a, proverite da je JWT_SECRET isti i da u Eureci nije registrovano više relational instanci sa različitim tajnama.'
          }
          if (/401|Unauthorized/i.test(msg)) {
            msg +=
              ' Token nije prihvaćen — odjava, ponovna prijava, ili uskladite JWT_SECRET između svih instanci relacionog servisa i docker-compose.'
          }
          try {
            const me = await naisApi.authMe()
            if (!cancelled) {
              msg += ` — GET /api/auth/me: uloga u bazi = ${me.uloga}, email = ${me.email}`
            }
          } catch {
            /* token ne važi ili /me nedostupan */
          }
          setProgramsErr(msg)
        }
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (programId == null) {
      setPregled(null)
      return
    }
    let cancelled = false
    ;(async () => {
      setLoading(true)
      setPregledErr('')
      try {
        const p = await naisApi.headProgramPregled(programId, statGeneracijaUpisa)
        if (!cancelled) setPregled(p)
      } catch {
        if (!cancelled) {
          setPregledErr('Pregled programa nije učitan (proverite da li ste šef katedre).')
          setPregled(null)
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [programId, statGeneracijaUpisa])

  useEffect(() => {
    setStatGodinaKurikuluma(1)
    setStatSemestar(0)
    setStatGeneracijaUpisa(null)
  }, [programId])

  const loadAdvanced = useCallback(async (fn: () => Promise<unknown>) => {
    setAdvancedLoading(true)
    setAdvancedErr('')
    try {
      setAdvancedPayload(await fn())
    } catch {
      setAdvancedErr('Napredni zahtev nije uspeo.')
      setAdvancedPayload(null)
    } finally {
      setAdvancedLoading(false)
    }
  }, [])

  const studentMap = pregled ? groupStudentsByGodinaUpisa(pregled.studenti) : null
  const predmetMap = pregled ? groupPredmetiByGodina(pregled.predmeti) : null
  const godineUpisaRedosled = GODINE_UPISA_KARTICE

  const statistikaFiltrirano = useMemo(() => {
    if (!pregled) return []
    return pregled.statistikaPolaganja.filter((r) => {
      if (r.godinaStudija !== statGodinaKurikuluma) return false
      if (statSemestar === 0) return true
      return (r.semestar ?? 1) === statSemestar
    })
  }, [pregled, statGodinaKurikuluma, statSemestar])

  const statUtezeniProcenat = useMemo(
    () => utezeniProcenatPolozenih(statistikaFiltrirano),
    [statistikaFiltrirano],
  )

  return (
    <>
      <article className="dj-card head-work-card head-pregled-card">
        <h2 className="dj-card-title">Pregled po studijskom programu</h2>
        <p className="dj-card-hint">
          Izaberite program svoje katedre: studenti i predmeti po godinama kurikuluma, statistika polaganja uz uzorak
          starijih godina za svaki predmet.
        </p>

        {programsErr ? <p className="err head-portal-err">{programsErr}</p> : null}

        <div className="head-program-picker">
          <label className="head-program-picker-label" htmlFor="head-program-select">
            Studijski program
          </label>
          <select
            id="head-program-select"
            className="head-program-select"
            value={programId ?? ''}
            onChange={(e) => {
              const v = e.target.value
              setProgramId(v ? Number(v) : null)
            }}
            disabled={programs.length === 0}
          >
            {programs.length === 0 ? <option value="">Nema programa</option> : null}
            {programs.map((p) => (
              <option key={p.id} value={p.id}>
                {p.sifra} — {p.naziv}
              </option>
            ))}
          </select>
        </div>

        {loading ? <p className="loading head-portal-loading">Učitavanje pregleda…</p> : null}
        {pregledErr ? <p className="err head-portal-err">{pregledErr}</p> : null}

        {pregled && !loading ? (
          <>
            <p className="dj-card-hint head-napomena">{pregled.napomenaOUzorku}</p>

            <section className="head-section" aria-labelledby="head-students-title">
              <h3 id="head-students-title" className="head-section-title">
                Studenti po godini upisa
              </h3>
              <p className="dj-card-hint head-students-hint">
                Godina u indeksu odgovara godini upisa (npr. …/25); prefiks je šifra smera (RI, SI, AUT, …). Procenjena
                godina studija je u zagradi.
              </p>
              <div className="head-year-grid">
                {godineUpisaRedosled.map((g) => {
                  const list = studentMap?.get(g) ?? []
                  return (
                    <div key={g} className="head-year-block">
                      <h4 className="head-year-heading">{naslovGodineUpisa(g)}</h4>
                      {list.length === 0 ? (
                        <p className="head-year-empty">Nema studenata.</p>
                      ) : (
                        <ul className="head-student-list">
                          {list.map((s) => (
                            <li key={s.id}>
                              <span className="head-stu-index">{s.brojIndeksa}</span>
                              <span className="head-stu-name">
                                {s.ime} {s.prezime}
                              </span>
                              <span
                                className="head-stu-meta"
                                title={`${s.email} · procenjena god. studija: ${s.procenjenaGodina}`}
                              >
                                proc. {s.procenjenaGodina}
                              </span>
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  )
                })}
              </div>
            </section>

            <section className="head-section" aria-labelledby="head-predmeti-title">
              <h3 id="head-predmeti-title" className="head-section-title">
                Predmeti po godini kurikuluma
              </h3>
              <div className="head-year-grid head-year-grid--predmeti">
                {([1, 2, 3, 4] as const).map((g) => {
                  const list = predmetMap?.get(g) ?? []
                  return (
                    <div key={g} className="head-year-block">
                      <h4 className="head-year-heading">{naslovGodineStudija(g)}</h4>
                      {list.length === 0 ? (
                        <p className="head-year-empty">Nema predmeta u ovom bloku.</p>
                      ) : (
                        <ul className="head-predmet-list">
                          {list.map((p) => (
                            <li key={p.id}>
                              <span className="head-pr-sifra">{p.sifra}</span>
                              <span className="head-pr-sem">{oznakaSemestra(p.semestar)}</span>
                              <span className="head-pr-naz">{p.naziv}</span>
                              <span className="head-pr-espb">{p.espb} ESPB</span>
                            </li>
                          ))}
                        </ul>
                      )}
                    </div>
                  )
                })}
              </div>
            </section>

            <section className="head-section" aria-labelledby="head-stats-title">
              <h3 id="head-stats-title" className="head-section-title">
                Statistika polaganja (uzorak starijih godina)
              </h3>
              <div className="head-program-picker head-stat-godina-picker">
                <label className="head-program-picker-label" htmlFor="head-stat-godina-select">
                  Godina kurikuluma (predmeti te godine — ista logika kao gore)
                </label>
                <select
                  id="head-stat-godina-select"
                  className="head-program-select"
                  value={statGodinaKurikuluma}
                  onChange={(e) =>
                    setStatGodinaKurikuluma(Number(e.target.value) as 1 | 2 | 3 | 4)
                  }
                >
                  {([1, 2, 3, 4] as const).map((g) => (
                    <option key={g} value={g}>
                      {naslovGodineStudija(g)}
                    </option>
                  ))}
                </select>
                <label className="head-program-picker-label" htmlFor="head-stat-sem-select">
                  Semestar u toj godini
                </label>
                <select
                  id="head-stat-sem-select"
                  className="head-program-select"
                  value={statSemestar}
                  onChange={(e) => setStatSemestar(Number(e.target.value) as 0 | 1 | 2)}
                >
                  <option value={0}>Oba semestra</option>
                  <option value={1}>Samo I semestar</option>
                  <option value={2}>Samo II semestar</option>
                </select>
              </div>
              <p className="dj-card-hint head-stat-hint">
                Brojači se odnose na izabranu generaciju upisa (bez starijih kohorta). Za predmete II semestra u toj godini
                kurikuluma prikaz je smislen tek posle letnjeg dela godine (jul–sep); ranije u školskoj godini ti redovi
                ostaju prazni. Filtrirajte tablicu po I / II semestru kao i do sada.
              </p>
              {statUtezeniProcenat != null ? (
                <p className="head-stat-zbir">
                  <strong>Uteženi procenat položenih</strong> (sve izlazke u ovoj godini):{' '}
                  {statUtezeniProcenat}%
                </p>
              ) : statistikaFiltrirano.length > 0 ? (
                <p className="head-stat-zbir head-stat-zbir--mute">Nema izlazaka za zbirni procenat.</p>
              ) : null}
              <div className="head-table-wrap">
                <table className="head-stat-table">
                  <thead>
                    <tr>
                      <th>God.</th>
                      <th>Sem.</th>
                      <th>Šifra</th>
                      <th>Predmet</th>
                      <th>Elegibilnih</th>
                      <th>Sa izlaskom</th>
                      <th>Položili</th>
                      <th>Pali</th>
                      <th>% polož.</th>
                    </tr>
                  </thead>
                  <tbody>
                    {statistikaFiltrirano.length === 0 ? (
                      <tr>
                        <td colSpan={9} className="head-stat-empty">
                          Nema predmeta ili statistike za ovu godinu kurikuluma.
                        </td>
                      </tr>
                    ) : (
                      statistikaFiltrirano.map((r) => (
                        <tr key={r.predmetId}>
                          <td>{naslovGodineStudija(r.godinaStudija)}</td>
                          <td>{r.semestar ?? '—'}</td>
                          <td>{r.sifra}</td>
                          <td>{r.naziv}</td>
                          <td>{r.brojElegibilnihStudenata}</td>
                          <td>{r.brojSaBarJednimIzlaskom}</td>
                          <td>{r.brojPolozenih}</td>
                          <td>{r.brojPali}</td>
                          <td>
                            {r.procenatPolozenihOdIzlazaka == null
                              ? '—'
                              : `${r.procenatPolozenihOdIzlazaka}%`}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </section>
          </>
        ) : null}
      </article>

      <article className="dj-card head-work-card head-advanced-card">
        <h2 className="dj-card-title">Napredna analitika (Cassandra)</h2>
        <p className="dj-card-hint">Agregati iz columnar servisa — kao ranije, za poređenje.</p>
        <button
          type="button"
          className="head-toggle-advanced"
          onClick={() => setShowAdvanced((v) => !v)}
        >
          {showAdvanced ? 'Sakrij napredno' : 'Prikaži napredno'}
        </button>
        {showAdvanced ? (
          <>
            <div className="dj-head-actions">
              <button type="button" onClick={() => loadAdvanced(naisApi.headStudents)}>
                Svi studenti (raw)
              </button>
              <button type="button" onClick={() => loadAdvanced(naisApi.headAnalytics)}>
                Analitika predmeta
              </button>
              <button type="button" onClick={() => loadAdvanced(naisApi.headTrends)}>
                Trendovi
              </button>
              <button type="button" onClick={() => loadAdvanced(naisApi.headPerf)}>
                Performanse
              </button>
            </div>
            {advancedErr ? <p className="err head-portal-err">{advancedErr}</p> : null}
            {advancedLoading ? <p className="loading head-portal-loading">Učitavanje…</p> : null}
            <div className="dj-json-wrap">
              <JsonBlock data={advancedPayload} />
            </div>
          </>
        ) : null}
      </article>
    </>
  )
}
