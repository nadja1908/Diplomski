import { useCallback, useEffect, useMemo, useState } from 'react'
import { naisApi } from './api'
import { formatPassFailTrendLabel } from './ispitniRokLabel'
import type { ColumnarSubjectStat, PassFailTrend, PerformanceOverview } from './columnarTypes'

function fmtAvg(v: number | null | undefined): string {
  if (v == null || Number.isNaN(v)) return '—'
  return v.toFixed(2)
}

type Props = {
  /** Dodatni endpoint-i za šefa katedre (katedra, trend, KPI). */
  isHeadOfDepartment: boolean
}

export function CassandraStatsPage({ isHeadOfDepartment }: Props) {
  const [overview, setOverview] = useState<PerformanceOverview | null>(null)
  const [trend, setTrend] = useState<PassFailTrend | null>(null)
  const [katedraPredmeti, setKatedraPredmeti] = useState<ColumnarSubjectStat[] | null>(null)
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [warn, setWarn] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setErr('')
    setWarn('')
    try {
      if (isHeadOfDepartment) {
        const [r0, r1, r2] = await Promise.allSettled([
          naisApi.headPerf(),
          naisApi.headTrends(),
          naisApi.headAnalytics(),
        ])
        setOverview(r0.status === 'fulfilled' ? r0.value : null)
        setTrend(r1.status === 'fulfilled' ? r1.value : null)
        if (r2.status === 'fulfilled') {
          const ka = r2.value
          setKatedraPredmeti(Array.isArray(ka) ? ka : [])
        } else {
          setKatedraPredmeti(null)
        }
        const failed = [r0, r1, r2].filter((r) => r.status === 'rejected') as PromiseRejectedResult[]
        if (failed.length === 3) {
          const msg =
            failed[0].reason instanceof Error
              ? failed[0].reason.message
              : 'Učitavanje statistike nije uspelo.'
          setErr(msg)
          setOverview(null)
          setTrend(null)
          setKatedraPredmeti(null)
        } else if (failed.length > 0) {
          setWarn(
            'Deo podataka nije učitan. HTTP 503 obično znači da gateway nema columnar-database-service (pokreni taj servis, Eureka i Cassandra, ili proveri registraciju u Eureka).',
          )
        }
      } else {
        setOverview(null)
        setTrend(null)
        setKatedraPredmeti(null)
      }
    } catch (e) {
      setErr(e instanceof Error ? e.message : 'Učitavanje statistike nije uspelo.')
      setOverview(null)
      setTrend(null)
      setKatedraPredmeti(null)
    } finally {
      setLoading(false)
    }
  }, [isHeadOfDepartment])

  useEffect(() => {
    void load()
  }, [load])

  const trendMax = useMemo(() => {
    const pts = trend?.meseci ?? []
    let m = 1
    for (const p of pts) {
      m = Math.max(m, p.polozeno + p.pali)
    }
    return m
  }, [trend])

  return (
    <div className="cass-stats-page">
      <article className="dj-card head-work-card">
        <div className="cass-stats-head">
          <div>
            <h2 className="dj-card-title">Statistika</h2>
          </div>
          <button type="button" className="dj-btn-refresh" disabled={loading} onClick={() => void load()}>
            Osveži
          </button>
        </div>

        {err ? <p className="sp-banner sp-banner--err">{err}</p> : null}
        {warn && !err ? <p className="sp-banner sp-banner--warn">{warn}</p> : null}
        {loading && isHeadOfDepartment ? <p className="loading head-portal-loading">Učitavanje…</p> : null}

        {isHeadOfDepartment && overview ? (
          <section className="cass-stats-section" aria-labelledby="cass-kpi-title">
            <h3 id="cass-kpi-title" className="cass-stats-h3">
              Pregled katedre
            </h3>
            <div className="stats-kpi-row cass-kpi-row">
              <article className="dj-card stats-kpi">
                <span className="stats-kpi-label">Ukupno položeno (katedra)</span>
                <strong className="stats-kpi-value">{overview.ukupnoPolozeno.toLocaleString('sr-Latn')}</strong>
              </article>
              <article className="dj-card stats-kpi">
                <span className="stats-kpi-label">Ukupno palo (katedra)</span>
                <strong className="stats-kpi-value">{overview.ukupnoPali.toLocaleString('sr-Latn')}</strong>
              </article>
              <article className="dj-card stats-kpi">
                <span className="stats-kpi-label">Predmeta na celoj katedri</span>
                <strong className="stats-kpi-value">{overview.brojPredmetaUKatedri}</strong>
              </article>
            </div>
            <div className="cass-extremes">
              {overview.najlaksiPredmet ? (
                <div className="dj-card cass-extreme cass-extreme--easy">
                  <span className="cass-extreme-label">Najviši prosečan na katedri</span>
                  <strong className="cass-extreme-name">{overview.najlaksiPredmet.nazivPredmeta}</strong>
                  <span className="cass-extreme-meta">
                    prosečna {fmtAvg(overview.najlaksiPredmet.prosecnaOcena)} · ocena zapisa{' '}
                    {overview.najlaksiPredmet.brojOcena}
                  </span>
                </div>
              ) : null}
              {overview.najteziPredmet ? (
                <div className="dj-card cass-extreme cass-extreme--hard">
                  <span className="cass-extreme-label">Najniži prosečan na katedri</span>
                  <strong className="cass-extreme-name">{overview.najteziPredmet.nazivPredmeta}</strong>
                  <span className="cass-extreme-meta">
                    prosečna {fmtAvg(overview.najteziPredmet.prosecnaOcena)} · ocena zapisa{' '}
                    {overview.najteziPredmet.brojOcena}
                  </span>
                </div>
              ) : null}
            </div>
          </section>
        ) : null}

        {isHeadOfDepartment && trend && trend.meseci.length > 0 ? (
          <section className="cass-stats-section" aria-labelledby="cass-trend-title">
            <h3 id="cass-trend-title" className="cass-stats-h3">
              Trend položeno / palo po ispitnim rokovima (katedra)
            </h3>
            <p className="dj-card-hint">
              Zbir agregata po ispitnim rokovima (Januarski, Februarski, Aprilski, Junski, Avgustovski, Oktobarski) —
              datum ispita mapiran na odgovarajući rok.
            </p>
            <div className="cass-trend-list">
              {trend.meseci.map((p) => {
                const total = p.polozeno + p.pali
                const wPass = total > 0 ? (p.polozeno / trendMax) * 100 : 0
                const wFail = total > 0 ? (p.pali / trendMax) * 100 : 0
                return (
                  <div key={p.mesec} className="cass-trend-row">
                    <span className="cass-trend-month">{formatPassFailTrendLabel(p.mesec)}</span>
                    <div className="cass-trend-bars" title={`Položeno ${p.polozeno}, palo ${p.pali}`}>
                      <div className="cass-trend-seg cass-trend-seg--pass" style={{ width: `${wPass}%` }} />
                      <div className="cass-trend-seg cass-trend-seg--fail" style={{ width: `${wFail}%` }} />
                    </div>
                    <span className="cass-trend-nums">
                      <span className="cass-t-n-pass">{p.polozeno}</span>
                      {' / '}
                      <span className="cass-t-n-fail">{p.pali}</span>
                    </span>
                  </div>
                )
              })}
            </div>
          </section>
        ) : null}

        {isHeadOfDepartment && katedraPredmeti && katedraPredmeti.length > 0 ? (
          <section className="cass-stats-section" aria-labelledby="cass-kat-title">
            <h3 id="cass-kat-title" className="cass-stats-h3">
              Predmeti katedre
            </h3>
            <div className="head-table-wrap">
              <table className="head-stat-table">
                <thead>
                  <tr>
                    <th>Naziv predmeta</th>
                    <th>Polaganja</th>
                    <th>Položeno</th>
                    <th>Palo</th>
                    <th>Prosek ocena</th>
                  </tr>
                </thead>
                <tbody>
                  {katedraPredmeti.map((r) => (
                    <tr key={r.predmetId}>
                      <td>
                        <strong>{r.nazivPredmeta}</strong>
                      </td>
                      <td>{r.ukupnoPolaganja}</td>
                      <td>{r.polozeno}</td>
                      <td>{r.pali}</td>
                      <td>{fmtAvg(r.prosecnaOcena)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        ) : null}

        {!isHeadOfDepartment && !loading ? (
          <p className="dj-muted" style={{ marginTop: '1rem' }}>
            Pregled katedre i agregati po predmetima dostupni su šefu katedre.
          </p>
        ) : null}
      </article>
    </div>
  )
}
