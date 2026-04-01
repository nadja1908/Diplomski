import { useCallback, useState } from 'react'
import './App.css'
import { naisApi, setToken } from './api'

type Role = string | null

function JsonBlock({ data }: { data: unknown }) {
  return (
    <pre className="json-out">
      {data === undefined || data === null
        ? '—'
        : JSON.stringify(data, null, 2)}
    </pre>
  )
}

export default function App() {
  const [email, setEmail] = useState('student001@ftn.rs')
  const [password, setPassword] = useState('student123')
  const [role, setRole] = useState<Role>(null)
  const [name, setName] = useState('')
  const [error, setError] = useState('')
  const [tab, setTab] = useState('home')
  const [loading, setLoading] = useState(false)
  const [payload, setPayload] = useState<unknown>(null)
  const [qAssist, setQAssist] = useState('Koje su teme kursa za Mašinsko učenje?')

  const logout = () => {
    setToken(null)
    setRole(null)
    setName('')
    setPayload(null)
    setTab('home')
  }

  const login = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const r = await naisApi.login(email.trim(), password)
      setToken(r.token)
      setRole(r.role)
      setName(`${r.ime} ${r.prezime}`)
      setTab(r.role === 'SEF_KATEDRE' ? 'head' : 'student')
    } catch {
      setError('Prijava nije uspela.')
    } finally {
      setLoading(false)
    }
  }

  const load = useCallback(
    async (fn: () => Promise<unknown>) => {
      setLoading(true)
      setError('')
      try {
        setPayload(await fn())
      } catch {
        setError('Zahtev nije uspeo (proverite ulogu ili token).')
        setPayload(null)
      } finally {
        setLoading(false)
      }
    },
    []
  )

  const authenticated = role !== null

  return (
    <div className="app">
      <header className="top">
        <div className="brand">NAIS · akademski informacioni sistem</div>
        {authenticated ? (
          <div className="user-bar">
            <span>
              {name} · <em>{role}</em>
            </span>
            <button type="button" className="ghost" onClick={logout}>
              Odjava
            </button>
          </div>
        ) : null}
      </header>

      {!authenticated ? (
        <main className="panel login-panel">
          <h1>Prijava</h1>
          <form onSubmit={login} className="login-form">
            <label>
              Email
              <input
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="username"
              />
            </label>
            <label>
              Lozinka
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
              />
            </label>
            {error ? <p className="err">{error}</p> : null}
            <button type="submit" disabled={loading}>
              {loading ? '…' : 'Prijavi se'}
            </button>
          </form>
          <p className="hint">
            Demo: student <code>student001@ftn.rs</code> /{' '}
            <code>student123</code>
            <br />
            Šef katedre: <code>sef.kii@ftn.rs</code> / <code>sef123</code>
          </p>
        </main>
      ) : (
        <>
          <nav className="tabs">
            {role === 'STUDENT' ? (
              <button
                type="button"
                className={tab === 'student' ? 'on' : ''}
                onClick={() => setTab('student')}
              >
                Student
              </button>
            ) : null}
            {role === 'SEF_KATEDRE' ? (
              <button
                type="button"
                className={tab === 'head' ? 'on' : ''}
                onClick={() => setTab('head')}
              >
                Šef katedre
              </button>
            ) : null}
            <button
              type="button"
              className={tab === 'stats' ? 'on' : ''}
              onClick={() => setTab('stats')}
            >
              Statistika (Cassandra)
            </button>
            <button
              type="button"
              className={tab === 'assistant' ? 'on' : ''}
              onClick={() => setTab('assistant')}
            >
              LLM asistent
            </button>
          </nav>

          <main className="panel">
            {tab === 'student' && role === 'STUDENT' ? (
              <section>
                <h2>Moj dashboard</h2>
                <div className="actions">
                  <button type="button" onClick={() => load(naisApi.studentProfile)}>
                    Profil
                  </button>
                  <button type="button" onClick={() => load(naisApi.studentGrades)}>
                    Predmeti i ocene
                  </button>
                  <button type="button" onClick={() => load(naisApi.studentGpa)}>
                    Prosek (ESPB)
                  </button>
                  <button type="button" onClick={() => load(naisApi.studentStats)}>
                    Statistika iz Cassandre
                  </button>
                </div>
                <JsonBlock data={payload} />
              </section>
            ) : null}

            {tab === 'head' && role === 'SEF_KATEDRE' ? (
              <section>
                <h2>Šef katedre</h2>
                <div className="actions">
                  <button type="button" onClick={() => load(naisApi.headStudents)}>
                    Studenti
                  </button>
                  <button type="button" onClick={() => load(naisApi.headAnalytics)}>
                    Analitika predmeta
                  </button>
                  <button type="button" onClick={() => load(naisApi.headTrends)}>
                    Trendovi položeno/palo
                  </button>
                  <button type="button" onClick={() => load(naisApi.headPerf)}>
                    Pregled performansi
                  </button>
                </div>
                <JsonBlock data={payload} />
              </section>
            ) : null}

            {tab === 'stats' ? (
              <section>
                <h2>Globalni rang predmeta</h2>
                <p className="sub">
                  Agregati čuvani u Apache Cassandri (sinhronizacija sa PostgreSQL pri
                  pokretanju backend-a).
                </p>
                <div className="actions">
                  <button type="button" onClick={() => load(naisApi.rankings)}>
                    Učitaj rang listu
                  </button>
                </div>
                <JsonBlock data={payload} />
              </section>
            ) : null}

            {tab === 'assistant' ? (
              <section>
                <h2>Semantička pretraga + LLM</h2>
                <p className="sub">
                  Kontekst dolazi iz Qdrant vektorske baze. Opciono postavite{' '}
                  <code>OPENAI_API_KEY</code> na backend kontejneru za pun odgovor.
                </p>
                <textarea
                  className="q-area"
                  value={qAssist}
                  onChange={(e) => setQAssist(e.target.value)}
                  rows={3}
                />
                <button
                  type="button"
                  onClick={() => load(() => naisApi.assistant(qAssist))}
                >
                  Pošalji
                </button>
                {payload &&
                typeof payload === 'object' &&
                payload !== null &&
                'answer' in payload ? (
                  <div className="answer">
                    <h3>Odgovor</h3>
                    <p>{String((payload as { answer: string }).answer)}</p>
                    <h4>Izvori</h4>
                    <ul>
                      {(
                        payload as unknown as {
                          sources: string[]
                        }
                      ).sources.map((s) => (
                        <li key={s.slice(0, 80)}>{s}</li>
                      ))}
                    </ul>
                  </div>
                ) : (
                  <JsonBlock data={payload} />
                )}
              </section>
            ) : null}

            {error ? <p className="err">{error}</p> : null}
            {loading ? <p className="loading">Učitavanje…</p> : null}
          </main>
        </>
      )}
    </div>
  )
}
