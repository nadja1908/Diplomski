import { useCallback, useEffect, useMemo, useState } from 'react'
import { naisApi } from './api'
import {
  naslovGodineKurikulumaZaStudenta,
  ocekivanaGodinaKurikulumaZaGeneraciju,
  strogiLinearniModelZaGeneraciju,
  REFERENCE_INTAKE_YEAR,
  SHEF_DOPUSTENE_GENERACIJE_UPISA,
} from './studijskeGodineLabele'
import type { ProgramStatisticsResponse, SubjectStatisticsRow } from './statisticsTypes'

function pct(n: number | null | undefined): string {
  if (n == null || Number.isNaN(n)) return '—'
  return `${n.toFixed(1)}%`
}

function num(n: number | null | undefined, d = 2): string {
  if (n == null || Number.isNaN(n)) return '—'
  return n.toFixed(d)
}

function weightedPassRate(subjects: SubjectStatisticsRow[]): number | null {
  let p = 0
  let t = 0
  for (const s of subjects) {
    if (s.totalStudentsWhoTook <= 0) continue
    p += s.totalStudentsWhoPassed
    t += s.totalStudentsWhoTook
  }
  if (t === 0) return null
  return Math.round((1000 * p) / t) / 10
}

type Props = {
  studyProgramId: number | null
}

function pickGeneracijaOpcije(apiList: number[] | undefined): number[] {
  const cap = REFERENCE_INTAKE_YEAR
  const fromApi = (apiList ?? []).filter((g) => g >= 2022 && g <= cap)
  if (fromApi.length > 0) return [...new Set(fromApi)].sort((a, b) => b - a)
  return [...SHEF_DOPUSTENE_GENERACIJE_UPISA]
}

export function ProgramStatisticsDashboard({ studyProgramId }: Props) {
  const [data, setData] = useState<ProgramStatisticsResponse | null>(null)
  const [filters, setFilters] = useState<Awaited<ReturnType<typeof naisApi.statisticsFilterOptions>> | null>(null)
  const [loading, setLoading] = useState(false)
  const [err, setErr] = useState('')
  const [godinaUpisa, setGodinaUpisa] = useState<string>('2025')

  const load = useCallback(async () => {
    if (studyProgramId == null || Number.isNaN(studyProgramId)) {
      setErr('Izaberite studijski program.')
      setData(null)
      return
    }
    const guNum = Number(godinaUpisa)
    if (!Number.isFinite(guNum) || guNum < 2022 || guNum > REFERENCE_INTAKE_YEAR) {
      setErr('Izaberite generaciju 2022–2025.')
      setData(null)
      return
    }
    setLoading(true)
    setErr('')
    try {
      const [stats, fo] = await Promise.all([
        naisApi.programStatistics({
          studyProgramId,
          godinaUpisa: guNum,
        }),
        naisApi.statisticsFilterOptions(studyProgramId, guNum),
      ])
      setData(stats)
      setFilters(fo)
    } catch (e) {
      setData(null)
      setErr(e instanceof Error ? e.message : 'Greška pri učitavanju statistike.')
    } finally {
      setLoading(false)
    }
  }, [studyProgramId, godinaUpisa])

  useEffect(() => {
    void load()
  }, [load])

  useEffect(() => {
    if (!filters?.generacijeUpisa) return
    const opcije = pickGeneracijaOpcije(filters.generacijeUpisa)
    const n = Number(godinaUpisa)
    if (opcije.length > 0 && !opcije.includes(n)) {
      setGodinaUpisa(String(opcije[0]))
    }
  }, [filters?.generacijeUpisa, godinaUpisa])

  const strictLinear =
    godinaUpisa !== '' && strogiLinearniModelZaGeneraciju(Number(godinaUpisa))

  const generacijaOpcije = useMemo(
    () => pickGeneracijaOpcije(filters?.generacijeUpisa),
    [filters?.generacijeUpisa],
  )

  const chartSubjects = useMemo(() => {
    const withPr = (data?.subjects ?? []).filter((s) => s.passRate != null && s.totalStudentsWhoTook > 0)
    return [...withPr].sort((a, b) => (b.passRate ?? 0) - (a.passRate ?? 0)).slice(0, 14)
  }, [data?.subjects])

  const kpiWeighted = useMemo(() => weightedPassRate(data?.subjects ?? []), [data?.subjects])
  const kpiAttempts = useMemo(
    () => (data?.subjects ?? []).reduce((a, s) => a + s.totalExamAttempts, 0),
    [data?.subjects],
  )
  const kpiTook = useMemo(
    () => (data?.subjects ?? []).filter((s) => s.totalStudentsWhoTook > 0).length,
    [data?.subjects],
  )

  if (studyProgramId == null) {
    return <p className="dj-muted">Izaberite program iznad da biste videli analitiku.</p>
  }

  return (
    <div className="stats-dash">
      {err ? (
        <p className="sp-banner sp-banner--err" style={{ marginTop: 0 }}>
          {err}
        </p>
      ) : null}

      <div className="stats-dash-filters dj-card">
        <h3 className="dj-card-title">Filteri</h3>
        <div className="stats-dash-filter-grid">
          <label className="stats-fl">
            Generacija (godina upisa)
            <select
              className="stats-fi"
              value={godinaUpisa}
              onChange={(e) => setGodinaUpisa(e.target.value)}
            >
              {generacijaOpcije.map((g) => (
                <option key={g} value={g}>
                  {g}
                </option>
              ))}
            </select>
          </label>
          {strictLinear && godinaUpisa !== '' ? (
            <div className="stats-fl" style={{ gridColumn: '1 / -1' }}>
              <p className="dj-card-hint" style={{ margin: 0 }}>
                <strong>Kontekst:</strong>{' '}
                {naslovGodineKurikulumaZaStudenta(
                  ocekivanaGodinaKurikulumaZaGeneraciju(Number(godinaUpisa)),
                )}{' '}
                · <strong>I semestar</strong>
              </p>
            </div>
          ) : null}
        </div>
        <div className="stats-dash-actions">
          <button type="button" className="dj-btn-refresh" disabled={loading} onClick={() => void load()}>
            Osveži
          </button>
        </div>
      </div>

      {loading && !data ? <div className="sp-skeleton-block sp-skeleton-block--tall" /> : null}

      {data ? (
        <>
          <div className="stats-kpi-row">
            <article className="dj-card stats-kpi">
              <span className="stats-kpi-label">Program</span>
              <strong className="stats-kpi-value">
                {data.program.sifra} — {data.program.naziv}
              </strong>
            </article>
            <article className="dj-card stats-kpi">
              <span className="stats-kpi-label">Utežena stopa prolaznosti</span>
              <strong className="stats-kpi-value">{pct(kpiWeighted)}</strong>
              <span className="stats-kpi-sub">∑položili / ∑polagali (po predmetima sa izlaskom)</span>
            </article>
            <article className="dj-card stats-kpi">
              <span className="stats-kpi-label">Predmeta sa izlascima</span>
              <strong className="stats-kpi-value">{kpiTook}</strong>
              <span className="stats-kpi-sub">od {data.subjects.length} u uzorku</span>
            </article>
            <article className="dj-card stats-kpi">
              <span className="stats-kpi-label">Ukupno izlazaka</span>
              <strong className="stats-kpi-value">{kpiAttempts}</strong>
            </article>
          </div>

          <section className="stats-rankings dj-card">
            <h3 className="dj-card-title">Rang liste</h3>
            <p className="dj-card-hint" style={{ marginTop: 0 }}>
              Prikaz punog naziva predmeta; šifra je u drugom redu.
            </p>
            <div className="stats-rank-grid">
              <div>
                <h4 className="stats-rank-h">Najteži (najniža stopa prolaznosti)</h4>
                <ol className="stats-rank-ol">
                  {data.rankings.hardestByPassRate.map((s) => (
                    <li key={s.subjectId}>
                      <span className="stats-rank-name">{s.subjectName}</span>
                      <div className="stats-rank-meta">
                        <code className="stats-rank-code">{s.subjectCode}</code>
                        {' · '}
                        {pct(s.passRate)} · polagalo {s.totalStudentsWhoTook}
                      </div>
                    </li>
                  ))}
                </ol>
              </div>
              <div>
                <h4 className="stats-rank-h">Najlakši (najviša stopa)</h4>
                <ol className="stats-rank-ol">
                  {data.rankings.easiestByPassRate.map((s) => (
                    <li key={s.subjectId}>
                      <span className="stats-rank-name">{s.subjectName}</span>
                      <div className="stats-rank-meta">
                        <code className="stats-rank-code">{s.subjectCode}</code>
                        {' · '}
                        {pct(s.passRate)} · polagalo {s.totalStudentsWhoTook}
                      </div>
                    </li>
                  ))}
                </ol>
              </div>
              <div>
                <h4 className="stats-rank-h">Najviši prosečan položen</h4>
                <ol className="stats-rank-ol">
                  {data.rankings.highestAveragePassingGrade.map((s) => (
                    <li key={s.subjectId}>
                      <span className="stats-rank-name">{s.subjectName}</span>
                      <div className="stats-rank-meta">
                        <code className="stats-rank-code">{s.subjectCode}</code> · {num(s.averagePassingGrade)}
                      </div>
                    </li>
                  ))}
                </ol>
              </div>
              <div>
                <h4 className="stats-rank-h">Najniži prosečan položen</h4>
                <ol className="stats-rank-ol">
                  {data.rankings.lowestAveragePassingGrade.map((s) => (
                    <li key={s.subjectId}>
                      <span className="stats-rank-name">{s.subjectName}</span>
                      <div className="stats-rank-meta">
                        <code className="stats-rank-code">{s.subjectCode}</code> · {num(s.averagePassingGrade)}
                      </div>
                    </li>
                  ))}
                </ol>
              </div>
            </div>
          </section>

          <section className="stats-chart dj-card">
            <h3 className="dj-card-title">Grafikon: stopa prolaznosti (uzorak)</h3>
            <div className="stats-chart-bars">
              {chartSubjects.map((s) => (
                <div key={s.subjectId} className="stats-cbar">
                  <div className="stats-cbar-label">
                    <span className="stats-cbar-name">{s.subjectName}</span>
                    <span className="stats-cbar-code">{s.subjectCode}</span>
                    <span className="stats-cbar-pct">{pct(s.passRate)}</span>
                  </div>
                  <div className="sp-bar-track">
                    <div className="sp-bar-fill" style={{ width: `${Math.min(100, s.passRate ?? 0)}%` }} />
                  </div>
                </div>
              ))}
            </div>
          </section>
        </>
      ) : null}
    </div>
  )
}
