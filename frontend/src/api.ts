import type { HeadProgramPregled, HeadProgramSummary, HeadStudentsBundle } from './headTypes'
import type {
  CurriculumProgress,
  Gpa,
  StudentProfile,
  SubjectGrade,
  SubjectStat,
} from './studentTypes'

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
  studentStats: () => api<SubjectStat[]>('/api/student/me/statistics'),
  /** Isti URL kao bundle; vraća { programi, studenti } ili treba koristiti headStudentsBundle. */
  headStudents: () => api<unknown>('/api/head/students'),
  headStudentsBundle: () => api<HeadStudentsBundle>('/api/head/students'),
  headPrograms: () => api<HeadProgramSummary[]>('/api/head/programs'),
  /** Koristi /api/head/students?programPregledId= da radi bez rute /api/head/program/** na gatewayju. */
  headProgramPregled: (programId: number, statistikaGodinaUpisa?: number | null) => {
    const q = new URLSearchParams()
    q.set('programPregledId', String(programId))
    if (statistikaGodinaUpisa != null) q.set('statistikaGodinaUpisa', String(statistikaGodinaUpisa))
    return api<HeadProgramPregled>(`/api/head/students?${q}`)
  },
  headAnalytics: () => api<unknown>('/api/head/subjects/analytics'),
  headTrends: () => api<unknown>('/api/head/trends/pass-fail'),
  headPerf: () => api<unknown>('/api/head/performance-overview'),
  rankings: () => api<unknown>('/api/stats/rankings'),
  assistant: (question: string) =>
    api<{ answer: string; sources: string[] }>('/api/assistant/query', {
      method: 'POST',
      body: JSON.stringify({ question }),
    }),
}
