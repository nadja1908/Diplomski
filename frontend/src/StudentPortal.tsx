import { useCallback, useEffect, useMemo, useState } from 'react'
import { naisApi } from './api'
import { normalizeCurriculumFromApi } from './godinaStudija'
import { naslovGodineKurikulumaZaStudenta } from './studijskeGodineLabele'
import type {
  CurriculumProgress,
  CurriculumSubject,
  Gpa,
  StudentProfile,
  SubjectGrade,
  SubjectStat,
} from './studentTypes'

type LoadErr = Partial<Record<'profile' | 'grades' | 'gpa' | 'stats' | 'curriculum', string>>

type SpPage = 'overview' | 'curriculum' | 'grades' | 'analytics'

const PAGE_COPY: Record<SpPage, { title: string; sub?: string }> = {
  overview: {
    title: 'Pregled',
    sub: 'Sažetak profila, proseka i predmeta na koje obrati pažnju.',
  },
  grades: {
    title: 'Izlasci',
    sub: 'Svi izlasci na ispit iz evidencije — svaki red je jedan termin. Koristi pretragu da suziš listu.',
  },
  analytics: {
    title: 'Statistika predmeta',
    sub: 'Agregati iz Cassandre za predmete tvog programa (položeno / pali).',
  },
  curriculum: { title: 'Studijski program po godinama' },
}

const STATUS_COPY: Record<CurriculumSubject['status'], { label: string }> = {
  POLOZENO: { label: 'Položeno' },
  PALI: { label: 'Nije položeno' },
  BEZ_IZLAZAKA: { label: 'Nije izlazio/la' },
  KASNIJE: { label: 'Kasnije' },
}

function formatDate(iso: string) {
  if (!iso || iso.length < 10) return iso
  return iso.slice(0, 10)
}

/** Na FTN-u položen ispit ima ocenu 6–10; 5 nije položeno. */
const PASSING_GRADE = 6

function bestGradePerSubject(grades: SubjectGrade[]): SubjectGrade[] {
  const m = new Map<string, SubjectGrade>()
  for (const g of grades) {
    const prev = m.get(g.predmetSifra)
    if (!prev || g.ocena > prev.ocena) m.set(g.predmetSifra, g)
  }
  return [...m.values()]
}

function notPassedBestAttempts(grades: SubjectGrade[]): SubjectGrade[] {
  return bestGradePerSubject(grades).filter((g) => g.ocena < PASSING_GRADE)
}

function passRatePct(s: SubjectStat): number {
  const t = s.polozeno + s.pali
  if (t <= 0) return 0
  return Math.round((100 * s.polozeno) / t)
}

type Props = {
  displayName: string
  onLogout: () => void
}

export function StudentPortal({ displayName, onLogout }: Props) {
  const [logoSrc, setLogoSrc] = useState('/logoFINAL.png')
  const [page, setPage] = useState<SpPage>('overview')
  const [profile, setProfile] = useState<StudentProfile | null>(null)
  const [grades, setGrades] = useState<SubjectGrade[] | null>(null)
  const [gpa, setGpa] = useState<Gpa | null>(null)
  const [curriculum, setCurriculum] = useState<CurriculumProgress | null>(null)
  const [stats, setStats] = useState<SubjectStat[] | null>(null)
  const [loading, setLoading] = useState(true)
  const [errors, setErrors] = useState<LoadErr>({})
  const [gradeFilter, setGradeFilter] = useState('')
  const [objaveOpen, setObjaveOpen] = useState(false)

  const refresh = useCallback(async () => {
    setLoading(true)
    setErrors({})
    const [rP, rG, rA, rC, rS] = await Promise.allSettled([
      naisApi.studentProfile(),
      naisApi.studentGrades(),
      naisApi.studentGpa(),
      naisApi.studentCurriculum(),
      naisApi.studentStats(),
    ])
    const nextErr: LoadErr = {}
    if (rP.status === 'fulfilled') setProfile(rP.value)
    else nextErr.profile = 'Profil nije učitan.'
    if (rG.status === 'fulfilled') setGrades(rG.value)
    else nextErr.grades = 'Izlasci nisu učitani.'
    if (rA.status === 'fulfilled') {
      const v = rA.value
      setGpa({
        ...v,
        brojPolozenihPredmeta: v.brojPolozenihPredmeta ?? 0,
        ukupnoPredmetaNaProgramu: v.ukupnoPredmetaNaProgramu ?? 0,
      })
    }
    else nextErr.gpa = 'Prosek nije učitan.'
    if (rC.status === 'fulfilled') {
      const c = normalizeCurriculumFromApi({
        ...rC.value,
        predmeti: (rC.value.predmeti ?? []).map((p) => ({
          ...p,
          izlasci: p.izlasci ?? [],
        })),
      })
      setCurriculum(c)
    } else {
      const msg =
        rC.reason instanceof Error
          ? rC.reason.message
          : typeof rC.reason === 'string'
            ? rC.reason
            : 'Kurikulum nije učitan.'
      nextErr.curriculum = msg
      setCurriculum(null)
    }
    if (rS.status === 'fulfilled') setStats(rS.value)
    else nextErr.stats = 'Statistika nije učitana.'
    setErrors(nextErr)
    setLoading(false)
  }, [])

  useEffect(() => {
    void refresh()
  }, [refresh])

  useEffect(() => {
    if (!objaveOpen) return
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setObjaveOpen(false)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [objaveOpen])

  const firstName = displayName.split(/\s+/)[0] || 'studente'

  const notPassedBest = useMemo(() => (grades ? notPassedBestAttempts(grades) : []), [grades])

  const filteredGrades = useMemo(() => {
    if (!grades) return []
    const q = gradeFilter.trim().toLowerCase()
    if (!q) return grades
    return grades.filter(
      (g) =>
        g.predmetNaziv.toLowerCase().includes(q) ||
        g.predmetSifra.toLowerCase().includes(q) ||
        g.ispitniRok.toLowerCase().includes(q)
    )
  }, [grades, gradeFilter])

  const statRowsSorted = useMemo(() => {
    if (!stats) return []
    return [...stats].sort((a, b) => passRatePct(a) - passRatePct(b))
  }, [stats])

  const curriculumByYear = useMemo(() => {
    if (!curriculum?.predmeti?.length) return new Map<number, CurriculumSubject[]>()
    const m = new Map<number, CurriculumSubject[]>()
    for (const row of curriculum.predmeti) {
      const y = row.godinaStudija
      const list = m.get(y) ?? []
      list.push(row)
      m.set(y, list)
    }
    for (const list of m.values()) {
      list.sort(
        (a, b) =>
          (a.semestar ?? 1) - (b.semestar ?? 1) || a.sifra.localeCompare(b.sifra, 'sr-Latn'),
      )
    }
    return new Map([...m.entries()].sort((a, b) => a[0] - b[0]))
  }, [curriculum])

  const navBtn = (id: SpPage, label: string) => (
    <button
      type="button"
      className={`dj-nav-item${page === id ? ' dj-nav-item--active' : ''}`}
      onClick={() => setPage(id)}
      aria-current={page === id ? 'page' : undefined}
    >
      {label}
    </button>
  )

  const overviewView = (
    <>
      <div className="dj-hero">
        <div className="dj-hero-main">
          <p className="dj-hero-kicker">Studentska kontrolna tabla</p>
          <h2 className="dj-hero-title">Dobrodošao/la nazad, {firstName}</h2>
          <p className="dj-hero-lead">
            Jednostavan i čist pregled tvojih podataka, proseka i predmeta na kojima treba poraditi.
          </p>
        </div>
        <div className="dj-hero-pills" aria-label="Brze statistike">
          {loading && !gpa ? (
            <div className="sp-skeleton sp-skeleton--hero" />
          ) : gpa ? (
            <>
              <div className="dj-pill">
                <span className="dj-pill-label">Prosek (ESPB)</span>
                <span className="dj-pill-value">
                  {gpa.prosekNaEspb != null ? gpa.prosekNaEspb.toFixed(2) : '—'}
                </span>
              </div>
              <div className="dj-pill">
                <span className="dj-pill-label">ESPB položeno</span>
                <span className="dj-pill-value">{gpa.zbirEspbPolozenih}</span>
              </div>
              <div className="dj-pill">
                <span className="dj-pill-label">Položeni predmeti</span>
                <span className="dj-pill-value">{gpa.brojPolozenihPredmeta}</span>
              </div>
            </>
          ) : (
            <p className="dj-muted">{errors.gpa ?? 'Nema podataka o proseku.'}</p>
          )}
        </div>
      </div>

      <div className="dj-dash-grid">
        <article className="dj-card">
          <h3 className="dj-card-title">Student</h3>
          {loading && !profile ? (
            <div className="sp-skeleton-block" />
          ) : profile ? (
            <div className="dj-student-block">
              <p className="dj-field-label">Ime i prezime</p>
              <p className="dj-field-value">
                {profile.ime} {profile.prezime}
              </p>
              <p className="dj-field-label">Program</p>
              <p className="dj-field-value">
                {profile.studijskiProgramNaziv}{' '}
                <span className="dj-pill-mini">{profile.studijskiProgramSifra}</span>
              </p>
              <p className="dj-field-label meta">Indeks · {profile.email}</p>
              <p className="dj-field-smeta">
                {profile.brojIndeksa} · {profile.katedraNaziv}
              </p>
            </div>
          ) : (
            <p className="dj-muted">{errors.profile ?? 'Nema podataka.'}</p>
          )}
        </article>

        <article className="dj-card">
          <h3 className="dj-card-title">Treba pažnje</h3>
          <p className="dj-card-hint">Najbolji pokušaj ispod 6 — kao paralelno programiranje sa ocenom 5.</p>
          {!grades || loading ? (
            <div className="sp-skeleton-block" />
          ) : notPassedBest.length ? (
            <ul className="dj-mini-list">
              {[...notPassedBest].sort((a, b) => a.ocena - b.ocena).slice(0, 4).map((g) => (
                <li key={g.predmetSifra}>
                  <span className="dj-mini-list-name">{g.predmetNaziv}</span>
                  <span className="sp-grade-badge sp-grade-badge--fail">{g.ocena}</span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="dj-muted">Nema takvih predmeta — ili još učitavam ocene.</p>
          )}
        </article>

        <article className="dj-card">
          <h3 className="dj-card-title">Statistika</h3>
          {loading && !gpa ? (
            <div className="sp-skeleton-block" />
          ) : gpa ? (
            <div className="dj-stat-big">
              <span className="dj-stat-big-num">{gpa.brojPolozenihPredmeta}</span>
              <span className="dj-stat-big-label">Položeni predmeti</span>
              <p className="dj-stat-sub">
                Na programu ukupno:{' '}
                <strong>{gpa.ukupnoPredmetaNaProgramu ?? '—'}</strong>
                {gpa.ukupnoIspita > 0 ? (
                  <>
                    {' '}
                    · izlazaka na ispit: <strong>{gpa.ukupnoIspita}</strong>
                  </>
                ) : null}
              </p>
            </div>
          ) : (
            <p className="dj-muted">{errors.gpa ?? 'Nema podataka.'}</p>
          )}
        </article>
      </div>
    </>
  )

  const gradesView = (
    <>
      <div className="sp-section-head sp-section-head--compact">
        <h2 className="sp-section-title">Svi izlasci</h2>
        <p className="sp-section-desc">
          Svaki red je jedan izlazak na ispit. Redovi sa ocenom ispod 6 su vizuelno označeni (nije položeno).
        </p>
      </div>
      <div className="sp-table-wrap">
        {loading && !grades ? (
          <div className="sp-skeleton-block sp-skeleton-block--tall" />
        ) : grades?.length ? (
          <table className="sp-table">
            <thead>
              <tr>
                <th>Predmet</th>
                <th>Šifra</th>
                <th>Ocena</th>
                <th>ESPB</th>
                <th>Rok</th>
                <th>Datum</th>
              </tr>
            </thead>
            <tbody>
              {filteredGrades.map((g, i) => (
                <tr
                  key={`${g.predmetSifra}-${g.datumIspita}-${i}`}
                  className={g.ocena < PASSING_GRADE ? 'sp-tr-fail' : undefined}
                >
                  <td>{g.predmetNaziv}</td>
                  <td>
                    <code className="sp-code">{g.predmetSifra}</code>
                  </td>
                  <td>
                    <span
                      className={
                        g.ocena < PASSING_GRADE
                          ? 'sp-grade-badge sp-grade-badge--fail'
                          : 'sp-grade-badge'
                      }
                    >
                      {g.ocena}
                    </span>
                  </td>
                  <td>{g.espb}</td>
                  <td>{g.ispitniRok}</td>
                  <td>{formatDate(g.datumIspita)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <p className="sp-muted sp-pad">{errors.grades ?? 'Nema izlazaka.'}</p>
        )}
        {grades && filteredGrades.length === 0 ? (
          <p className="sp-muted sp-pad">Nema rezultata za „{gradeFilter}”.</p>
        ) : null}
      </div>
    </>
  )

  const curriculumView = (
    <>
      {errors.curriculum ? (
        <div className="sp-banner sp-banner--warn">
          <p className="sp-banner-title">Kurikulum</p>
          <p className="sp-banner-detail">{errors.curriculum}</p>
          <p className="sp-banner-hint">
            Proveri da li API Gateway radi na <code className="sp-code">localhost:9000</code> i da li je
            ponovo izgrađen posle dodavanja rute <code className="sp-code">curriculum-progress</code>{' '}
            (<code className="sp-code">docker compose build gateway-api</code>).
          </p>
        </div>
      ) : null}
      {loading && !curriculum ? (
        <div className="sp-skeleton-block sp-skeleton-block--tall" />
      ) : curriculum && curriculum.predmeti.length ? (
        <div className="sp-curriculum-years dj-curriculum-years">
          {[...curriculumByYear.entries()].map(([year, rows]) => (
              <section key={year} className="dj-card dj-curriculum-year">
                <h3 className="dj-card-title sp-card-title">
                  {naslovGodineKurikulumaZaStudenta(year)}
                  <span className="sp-cur-year-count">{rows.length} predmeta</span>
                </h3>
                <div className="sp-table-wrap">
                  <table className="sp-table sp-table--compact">
                    <thead>
                      <tr>
                        <th>Šifra</th>
                        <th>Sem.</th>
                        <th>Predmet</th>
                        <th>ESPB</th>
                        <th>Status</th>
                        <th>Najbolja</th>
                        <th>Izlasci</th>
                      </tr>
                    </thead>
                    <tbody>
                      {rows.map((r) => (
                        <tr
                          key={r.predmetId}
                          className={
                            r.status === 'PALI'
                              ? 'sp-tr-fail'
                              : r.status === 'KASNIJE'
                                ? 'sp-tr-muted'
                                : undefined
                          }
                        >
                          <td>
                            <code className="sp-code">{r.sifra}</code>
                          </td>
                          <td className="sp-muted">{r.semestar ?? '—'}</td>
                          <td>{r.naziv}</td>
                          <td>{r.espb}</td>
                          <td>
                            <span className={`sp-cur-pill sp-cur-pill--${r.status.toLowerCase()}`}>
                              {STATUS_COPY[r.status].label}
                            </span>
                          </td>
                          <td>
                            {r.najboljaOcena != null ? (
                              <span
                                className={
                                  r.najboljaOcena < PASSING_GRADE
                                    ? 'sp-grade-badge sp-grade-badge--fail'
                                    : 'sp-grade-badge'
                                }
                              >
                                {r.najboljaOcena}
                              </span>
                            ) : (
                              <span className="sp-muted">—</span>
                            )}
                          </td>
                          <td>
                            {r.izlasci.length ? (
                              <details className="sp-cur-details">
                                <summary>
                                  {r.izlasci.length}{' '}
                                  {r.izlasci.length === 1 ? 'izlazak' : 'izlaska'}
                                </summary>
                                <ul className="sp-cur-attempt-list">
                                  {r.izlasci.map((a, ai) => (
                                    <li key={`${a.datumIspita}-${ai}`}>
                                      <span className="sp-cur-attempt-date">{formatDate(a.datumIspita)}</span>
                                      <span className="sp-muted"> · {a.ispitniRok} · </span>
                                      <span
                                        className={
                                          a.ocena < PASSING_GRADE
                                            ? 'sp-grade-badge sp-grade-badge--fail sp-grade-badge--sm'
                                            : 'sp-grade-badge sp-grade-badge--sm'
                                        }
                                      >
                                        {a.ocena}
                                      </span>
                                      {a.poeni != null ? (
                                        <span className="sp-muted"> · {a.poeni} poena</span>
                                      ) : null}
                                    </li>
                                  ))}
                                </ul>
                              </details>
                            ) : (
                              <span className="sp-muted">—</span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>
          ))}
        </div>
      ) : errors.curriculum && !loading ? null : !loading ? (
        <p className="sp-muted sp-pad">Nema predmeta za tvoj studijski program u bazi.</p>
      ) : null}
    </>
  )

  const analyticsView = (
    <>
      {errors.stats ? <p className="sp-banner sp-banner--warn">{errors.stats}</p> : null}
      <div className="sp-bars">
        {loading && !stats ? (
          <div className="sp-skeleton-block sp-skeleton-block--tall" />
        ) : statRowsSorted.length ? (
          statRowsSorted.map((s) => {
            const pct = passRatePct(s)
            return (
              <div key={s.predmetId} className="sp-bar-row">
                <div className="sp-bar-head">
                  <span className="sp-bar-name">{s.nazivPredmeta}</span>
                  <span className="sp-bar-meta">
                    položeno {s.polozeno} · pali {s.pali}
                    {s.prosecnaOcena != null ? ` · prosečna ${s.prosecnaOcena.toFixed(2)}` : ''}
                  </span>
                </div>
                <div className="sp-bar-track" role="presentation">
                  <div className="sp-bar-fill" style={{ width: `${pct}%` }} />
                </div>
                <span className="sp-bar-pct">{pct}% položeno</span>
              </div>
            )
          })
        ) : (
          <p className="sp-muted sp-pad">{loading ? '…' : 'Nema redova statistike za tvoj program.'}</p>
        )}
      </div>
    </>
  )

  return (
    <div className="sp-layout dj-shell">
      <header className="dj-topnav" role="banner">
        <div className="dj-brand">
          <img
            className="dj-brand-logo"
            src={logoSrc}
            width={56}
            height={64}
            alt=""
            onError={() => setLogoSrc('/brand-logo.svg')}
          />
          <div className="dj-brand-text">
            <span className="dj-brand-name">DjordUNI</span>
            <span className="dj-brand-tag">student</span>
          </div>
        </div>
        <nav className="dj-nav" aria-label="Glavna navigacija">
          {navBtn('overview', 'Pregled')}
          {navBtn('curriculum', 'Program')}
          {navBtn('grades', 'Izlasci')}
          {navBtn('analytics', 'Statistika')}
        </nav>
        <div className="dj-top-right">
          <span className="dj-user-chip" title={profile?.studijskiProgramSifra ?? 'Program'}>
            {profile?.studijskiProgramSifra ?? '—'}
          </span>
          <span className="dj-student-name">{displayName}</span>
         
        
          <button type="button" className="dj-logout-link" onClick={onLogout}>
            Odjava
          </button>
        </div>
      </header>

      {objaveOpen ? (
        <div className="dj-objave-backdrop" role="presentation" onClick={() => setObjaveOpen(false)}>
          <div className="dj-objave-panel" role="dialog" aria-modal="true" aria-labelledby="dj-objave-title" onClick={(e) => e.stopPropagation()}>
            <h2 id="dj-objave-title" className="dj-objave-title">
              Objave
            </h2>
            <p className="dj-objave-intro">Obaveštenja za studente (demo — statička lista).</p>
            <ul className="dj-objave-list">
              <li>
                <time className="dj-objave-date">01. 04. 2026.</time> — Početak letnjeg upisnog roka za izborne predmete.
              </li>
              <li>
                <time className="dj-objave-date">28. 03. 2026.</time> — Radionica: priprema za odbranu diplomskog rada.
              </li>
              <li>
                <time className="dj-objave-date">15. 03. 2026.</time> — Održavanje sistema: mogući kratki prekid u nedelju od 02:00.
              </li>
            </ul>
            <div className="dj-objave-actions">
              <button type="button" className="dj-btn-ghost" onClick={() => void refresh()}>
                Osveži podatke portala
              </button>
              <button type="button" className="dj-btn-refresh" onClick={() => setObjaveOpen(false)}>
                Zatvori
              </button>
            </div>
          </div>
        </div>
      ) : null}

      <div className="sp-main dj-main">
        {page !== 'overview' ? (
          <header className="dj-page-head">
            <div>
              <h1 className="dj-page-title">{PAGE_COPY[page].title}</h1>
              {PAGE_COPY[page].sub ? <p className="dj-page-sub">{PAGE_COPY[page].sub}</p> : null}
            </div>
            {page === 'grades' ? (
              <label className="sp-search dj-search">
                <span className="visually-hidden">Pretraga izlazaka</span>
                <input
                  type="search"
                  placeholder="Pretraži predmet, šifru, rok…"
                  value={gradeFilter}
                  onChange={(e) => setGradeFilter(e.target.value)}
                />
              </label>
            ) : null}
          </header>
        ) : null}

        {Object.keys(errors).length === 4 ? (
          <div className="sp-banner sp-banner--err">
            Nije moguće učitati podatke. Proveri da li je gateway pokrenut i da li si ulogovan kao student.
          </div>
        ) : null}

        <div key={page} className="sp-page-pane">
          {page === 'overview' ? overviewView : null}
          {page === 'curriculum' ? curriculumView : null}
          {page === 'grades' ? gradesView : null}
          {page === 'analytics' ? analyticsView : null}
        </div>
      </div>
    </div>
  )
}
