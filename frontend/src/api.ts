import type { HeadProgramPregled, HeadProgramSummary, HeadStudentsBundle } from './headTypes'
import type { CurriculumProgress, Gpa, StudentProfile, SubjectGrade } from './studentTypes'
import type { ColumnarSubjectStat, PassFailTrend, PerformanceOverview } from './columnarTypes'
import {
  parseUnpassedSubjectPassRate,
  type ProgramStatisticsResponse,
  type StatisticsFilterOptions,
} from './statisticsTypes'

const base = ''

export function getToken(): string | null {
  return localStorage.getItem('nais_token')
}

export function setToken(t: string | null) {
  if (t) localStorage.setItem('nais_token', t)
  else localStorage.removeItem('nais_token')
}

async function api<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(init.headers as Record<string, string> | undefined),
  }
  const t = getToken()
  if (t) headers['Authorization'] = `Bearer ${t}`
  const res = await fetch(`${base}${path}`, { ...init, headers })
  if (!res.ok) {
    const txt = await res.text()
    const snippet = txt.length > 280 ? `${txt.slice(0, 280)}…` : txt
    throw new Error(
      snippet ? `HTTP ${res.status}: ${snippet}` : `HTTP ${res.status} ${res.statusText}`,
    )
  }
  if (res.status === 204) return undefined as T
  return res.json() as Promise<T>
}

export const naisApi = {
  login: (email: string, password: string) =>
    api<{
      token: string
      role: string
      ime: string
      prezime: string
      email: string
    }>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),
  /** Šta baza kaže za tvoj JWT (id, email, uloga) — za dijagnostiku. */
  authMe: () =>
    api<{ id: number; email: string; ime: string; prezime: string; uloga: string }>('/api/auth/me'),
  studentProfile: () => api<StudentProfile>('/api/student/me/profile'),
  studentGrades: () => api<SubjectGrade[]>('/api/student/me/subjects-grades'),
  studentGpa: () => api<Gpa>('/api/student/me/gpa'),
  studentCurriculum: () => api<CurriculumProgress>('/api/student/me/curriculum-progress'),
  /** Predmeti koje student još nije položio + programska stopa prolaznosti (sort backend). */
  studentUnpassedSubjectPassRates: async () => {
    const rows = await api<unknown[]>('/api/student/me/unpassed-subject-pass-rates')
    return Array.isArray(rows) ? rows.map(parseUnpassedSubjectPassRate) : []
  },
  /** Isti URL kao bundle; vraća { programi, studenti } ili treba koristiti headStudentsBundle. */
  headStudents: () => api<unknown>('/api/head/students'),
  headStudentsBundle: () => api<HeadStudentsBundle>('/api/head/students'),
  headPrograms: () => api<HeadProgramSummary[]>('/api/head/programs'),
  /** Koristi /api/head/students?programPregledId= da radi bez rute /api/head/program/** na gatewayju. */
  headProgramPregled: (
    programId: number,
    opts?: {
      statistikaGodinaUpisa?: number | null
      /** true = svi studenti smera × svi predmeti, bez uzorka po semestru/generaciji */
      statistikaCeoProgram?: boolean
    },
  ) => {
    const q = new URLSearchParams()
    q.set('programPregledId', String(programId))
    if (opts?.statistikaGodinaUpisa != null) q.set('statistikaGodinaUpisa', String(opts.statistikaGodinaUpisa))
    if (opts?.statistikaCeoProgram === true) q.set('statistikaCeoProgram', 'true')
    return api<HeadProgramPregled>(`/api/head/students?${q}`)
  },
  /** Cassandra — predmeti katedre (samo šef). */
  headAnalytics: () => api<ColumnarSubjectStat[]>('/api/head/subjects/analytics'),
  /** Cassandra — trend položeno / palo po ispitnim rokovima za jednu kalendarsku godinu roka. */
  headTrends: (godina?: number) => {
    const q = godina != null ? `?godina=${encodeURIComponent(String(godina))}` : ''
    return api<PassFailTrend>(`/api/head/trends/pass-fail${q}`)
  },
  /** Cassandra — zbirni KPI + najlakši / najteži predmet katedre (samo šef). */
  headPerf: () => api<PerformanceOverview>('/api/head/performance-overview'),
  /** Cassandra — globalni rang svih predmeta (sinhronizovano iz PostgreSQL-a). */
  rankings: () => api<ColumnarSubjectStat[]>('/api/stats/rankings'),
  assistant: (question: string) =>
    api<{ answer: string; sources: string[] }>('/api/assistant/query', {
      method: 'POST',
      body: JSON.stringify({ question }),
    }),

  /** Statistika predmeta iz PostgreSQL-a — samo šef katedre (studyProgramId obavezan). */
  programStatistics: (opts: {
    studyProgramId: number
    godinaUpisa?: number
    skolskaGodina?: string
    kurikulumGodina?: number
    semestar?: number
    predmetId?: number
    includeGenerationBreakdown?: boolean
  }) => {
    const q = new URLSearchParams()
    q.set('studyProgramId', String(opts.studyProgramId))
    if (opts.godinaUpisa != null) q.set('godinaUpisa', String(opts.godinaUpisa))
    if (opts.skolskaGodina) q.set('skolskaGodina', opts.skolskaGodina)
    if (opts.kurikulumGodina != null) q.set('kurikulumGodina', String(opts.kurikulumGodina))
    if (opts.semestar != null) q.set('semestar', String(opts.semestar))
    if (opts.predmetId != null) q.set('predmetId', String(opts.predmetId))
    if (opts.includeGenerationBreakdown) q.set('includeGenerationBreakdown', 'true')
    const qs = q.toString()
    return api<ProgramStatisticsResponse>(`/api/statistics/subjects?${qs}`)
  },

  statisticsFilterOptions: (studyProgramId: number, godinaUpisa?: number) => {
    const q = new URLSearchParams({ studyProgramId: String(studyProgramId) })
    if (godinaUpisa != null) q.set('godinaUpisa', String(godinaUpisa))
    return api<StatisticsFilterOptions>(`/api/statistics/filter-options?${q}`)
  },
}
