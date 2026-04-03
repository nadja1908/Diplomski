import { useEffect, useState } from 'react'
import { naisApi } from './api'
import { ProgramStatisticsDashboard } from './ProgramStatisticsDashboard'
import { HeadStudentsSection } from './HeadStudentsSection'

export type HeadPortalView = 'analytics' | 'students'

type Props = {
  view: HeadPortalView
}

export function HeadPortal({ view }: Props) {
  const [programs, setPrograms] = useState<Awaited<ReturnType<typeof naisApi.headStudentsBundle>>['programi']>([])
  const [programId, setProgramId] = useState<number | null>(null)
  const [pregledZaStudente, setPregledZaStudente] = useState<Awaited<
    ReturnType<typeof naisApi.headProgramPregled>
  > | null>(null)
  const [loadingPrograms, setLoadingPrograms] = useState(false)
  const [loadingStudents, setLoadingStudents] = useState(false)
  const [programsErr, setProgramsErr] = useState('')
  const [studentsErr, setStudentsErr] = useState('')

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setProgramsErr('')
      setLoadingPrograms(true)
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
            /* ignore */
          }
          setProgramsErr(msg)
        }
      } finally {
        if (!cancelled) setLoadingPrograms(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (programId == null || view !== 'students') {
      setPregledZaStudente(null)
      setStudentsErr('')
      return
    }
    let cancelled = false
    ;(async () => {
      setLoadingStudents(true)
      setStudentsErr('')
      try {
        const p = await naisApi.headProgramPregled(programId, { statistikaCeoProgram: true })
        if (!cancelled) setPregledZaStudente(p)
      } catch {
        if (!cancelled) {
          setStudentsErr('Pregled studenata nije učitan.')
          setPregledZaStudente(null)
        }
      } finally {
        if (!cancelled) setLoadingStudents(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [programId, view])

  const titleForView = view === 'analytics' ? 'Analitika programa' : 'Studenti'

  return (
    <>
      <article className="dj-card head-work-card head-pregled-card">
        <h2 className="dj-card-title">{titleForView}</h2>
        {view !== 'analytics' ? (
          <p className="dj-card-hint">Lista studenata po generaciji upisa.</p>
        ) : null}

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
            disabled={programs.length === 0 || loadingPrograms}
          >
            {programs.length === 0 ? <option value="">Nema programa</option> : null}
            {programs.map((p) => (
              <option key={p.id} value={p.id}>
                {p.sifra} — {p.naziv}
              </option>
            ))}
          </select>
        </div>

        {view === 'analytics' && programId != null ? (
          <div className="head-analytics-embed">
            <ProgramStatisticsDashboard studyProgramId={programId} />
          </div>
        ) : null}

        {view === 'students' ? (
          <>
            {studentsErr ? <p className="err head-portal-err">{studentsErr}</p> : null}
            <HeadStudentsSection pregled={pregledZaStudente} loading={loadingStudents} />
          </>
        ) : null}
      </article>
    </>
  )
}
