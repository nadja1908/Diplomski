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
    throw new Error(txt || res.statusText)
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
  studentProfile: () => api<unknown>('/api/student/me/profile'),
  studentGrades: () => api<unknown>('/api/student/me/subjects-grades'),
  studentGpa: () => api<unknown>('/api/student/me/gpa'),
  studentStats: () => api<unknown>('/api/student/me/statistics'),
  headStudents: () => api<unknown>('/api/head/students'),
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
