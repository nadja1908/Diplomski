import { useEffect, useId, useRef, useState } from 'react'
import './App.css'
import { naisApi, setToken } from './api'
import { CassandraStatsPage } from './CassandraStatsPage'
import { HeadPortal, type HeadPortalView } from './HeadPortal'
import { StudentPortal } from './StudentPortal'

type Role = string | null

type StaffSection = HeadPortalView | 'stats'

type ChatMessage =
  | { id: string; role: 'user'; text: string }
  | { id: string; role: 'assistant'; text: string }

const WELCOME_TEXT =
  'Zdravo! Pitaj šta god ti treba o predmetima, ocenama i kurikulumu — odgovori su vezani za tvoj studijski program.'
const BRAND_LOGO_SRC = `${import.meta.env.BASE_URL}brand-logo.svg`

const CHAT_STARTERS = [
  'Šta znaš o meni?',
  'Koje predmete još nisam položio/la?',
  'Moj prosek i ocene',
  'Navedi sve predmete na mom smeru',
]

function newId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
}

function ChatBotAvatar({
  size = 40,
  className,
  fabStyle,
}: {
  size?: number
  className?: string
  fabStyle?: boolean
}) {
  const [broken, setBroken] = useState(false)
  if (broken) {
    return (
      <span
        className={className}
        style={
          fabStyle
            ? { width: size, height: size, display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }
            : undefined
        }
      >
        <FabBotIcon />
      </span>
    )
  }
  return (
    <img
      src={`${import.meta.env.BASE_URL}chatBot.png`}
      alt=""
      width={size}
      height={size}
      className={`${className ?? ''}${fabStyle ? ' chat-fab-bot-img' : ' chat-bot-avatar-img'}`.trim()}
      onError={() => setBroken(true)}
      decoding="async"
    />
  )
}

function FabBotIcon() {
  const gid = useId().replace(/:/g, '')
  return (
    <svg className="chat-fab-svg" viewBox="0 0 40 40" aria-hidden>
      <defs>
        <linearGradient id={`fabg-${gid}`} x1="10" y1="6" x2="30" y2="36">
          <stop stopColor="#fb923c" />
          <stop offset="1" stopColor="#d97736" />
        </linearGradient>
      </defs>
      <circle cx="20" cy="20" r="19" fill={`url(#fabg-${gid})`} />
      <rect x="11" y="14" width="18" height="14" rx="3" fill="white" fillOpacity="0.95" />
      <circle cx="15.5" cy="20" r="2" fill="#9a3412" />
      <circle cx="24.5" cy="20" r="2" fill="#9a3412" />
      <path
        d="M15 25.5c1.2 1 2.8 1.5 5 1.5s3.8-.5 5-1.5"
        stroke="#9a3412"
        strokeWidth="1.3"
        strokeLinecap="round"
        fill="none"
      />
      <rect x="17" y="9" width="6" height="4" rx="1" fill="white" fillOpacity="0.85" />
    </svg>
  )
}

function FormattedBubbleText({ text, className }: { text: string; className?: string }) {
  const blocks = text.split(/\n\n+/).filter((b) => b.trim().length > 0)
  return (
    <div className={className ?? 'chat-formatted'}>
      {blocks.map((block, bi) => {
        const lines = block.split('\n').map((l) => l.trimEnd())
        const bulletLines = lines.filter((l) => l.length > 0)
        const allBullets = bulletLines.length > 0 && bulletLines.every((l) => /^[•\-\*]\s/.test(l))
        if (allBullets) {
          return (
            <ul key={bi} className="chat-bullet-list">
              {bulletLines.map((line, li) => (
                <li key={li}>{line.replace(/^[•\-\*]\s*/, '')}</li>
              ))}
            </ul>
          )
        }
        return (
          <p key={bi} className="chat-para">
            {block}
          </p>
        )
      })}
    </div>
  )
}

function ChatQuickStarters({
  disabled,
  onPick,
}: {
  disabled: boolean
  onPick: (q: string) => void
}) {
  return (
    <div className="chat-starters" role="group" aria-label="Brzi predlozi pitanja">
      <span className="chat-starters-label">Brzo pitaj</span>
      <div className="chat-starters-scroll">
        {CHAT_STARTERS.map((q) => (
          <button
            key={q}
            type="button"
            className="chat-starter-chip"
            disabled={disabled}
            onClick={() => onPick(q)}
          >
            {q}
          </button>
        ))}
      </div>
    </div>
  )
}

function FabCloseIcon() {
  return (
    <svg className="chat-fab-svg chat-fab-svg--close" viewBox="0 0 40 40" aria-hidden>
      <path
        d="M12 12l16 16M28 12L12 28"
        stroke="white"
        strokeWidth="2.5"
        strokeLinecap="round"
      />
    </svg>
  )
}

export default function App() {
  const [email, setEmail] = useState(() =>
    typeof localStorage !== 'undefined' ? localStorage.getItem('nais_remember_email') ?? 'student001@ftn.rs' : 'student001@ftn.rs',
  )
  const [password, setPassword] = useState('student123')
  const [rememberMe, setRememberMe] = useState(true)
  const [loginLogoSrc, setLoginLogoSrc] = useState(BRAND_LOGO_SRC)
  const [role, setRole] = useState<Role>(null)
  const [name, setName] = useState('')
  const [error, setError] = useState('')
  const [staffSection, setStaffSection] = useState<StaffSection>('analytics')
  const [authLoading, setAuthLoading] = useState(false)
  const [chatOpen, setChatOpen] = useState(false)
  const [chatPending, setChatPending] = useState(false)
  const [chatError, setChatError] = useState('')
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([
    { id: 'welcome', role: 'assistant', text: WELCOME_TEXT },
  ])
  const [chatInput, setChatInput] = useState('')
  const chatEndRef = useRef<HTMLDivElement>(null)
  const chatInputRef = useRef<HTMLTextAreaElement>(null)

  const logout = () => {
    setToken(null)
    setRole(null)
    setName('')
    setStaffSection('analytics')
    setChatOpen(false)
    setChatMessages([{ id: newId(), role: 'assistant', text: WELCOME_TEXT }])
    setChatInput('')
    setChatError('')
  }

  const login = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setAuthLoading(true)
    try {
      const r = await naisApi.login(email.trim(), password)
      setToken(r.token)
      setRole(r.role)
      setName(`${r.ime} ${r.prezime}`)
      if (r.role === 'SEF_KATEDRE') setStaffSection('analytics')
      else if (r.role !== 'STUDENT') setStaffSection('stats')
      if (rememberMe) {
        localStorage.setItem('nais_remember_email', email.trim())
      } else {
        localStorage.removeItem('nais_remember_email')
      }
    } catch {
      setError('Prijava nije uspela.')
    } finally {
      setAuthLoading(false)
    }
  }

  const sendAssistantMessage = async (presetText?: string) => {
    const text = (presetText ?? chatInput).trim()
    if (!text || chatPending) return
    if (presetText === undefined) {
      setChatInput('')
    }
    const userMsg: ChatMessage = { id: newId(), role: 'user', text }
    setChatMessages((m) => [...m, userMsg])
    setChatPending(true)
    setChatError('')
    try {
      const r = await naisApi.assistant(text)
      setChatMessages((m) => [
        ...m,
        {
          id: newId(),
          role: 'assistant',
          text: r.answer,
        },
      ])
    } catch (e) {
      const raw = e instanceof Error ? e.message : ''
      const httpBody =
        /^HTTP \d+:\s*(.*)$/s.exec(raw)?.[1]?.trim() ?? ''
      if (httpBody) {
        setChatError(httpBody)
        setChatMessages((m) => [
          ...m,
          { id: newId(), role: 'assistant', text: httpBody },
        ])
      } else {
        setChatError('Nije moguće dohvatiti odgovor. Proveri mrežu ili da li si ulogovan kao student.')
        setChatMessages((m) => [
          ...m,
          {
            id: newId(),
            role: 'assistant',
            text: 'Servis trenutno nije dostupan. Pokušaj ponovo za trenutak.',
          },
        ])
      }
    } finally {
      setChatPending(false)
    }
  }

  const resetChat = () => {
    setChatMessages([{ id: newId(), role: 'assistant', text: WELCOME_TEXT }])
    setChatInput('')
    setChatError('')
  }

  useEffect(() => {
    if (chatOpen) {
      chatEndRef.current?.scrollIntoView({ behavior: 'smooth' })
    }
  }, [chatMessages, chatPending, chatOpen])

  useEffect(() => {
    if (chatOpen) {
      const t = window.setTimeout(() => chatInputRef.current?.focus(), 200)
      return () => window.clearTimeout(t)
    }
  }, [chatOpen])

  useEffect(() => {
    if (!chatOpen) return
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setChatOpen(false)
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [chatOpen])

  const authenticated = role !== null

  const chatPanel = (
    <div
      className="chat-popup"
      role="dialog"
      aria-modal="true"
      aria-labelledby="chat-dock-title"
      onClick={(e) => e.stopPropagation()}
    >
      <div className="chat-card chat-card--popup">
        <div className="chat-card-header">
          <div className="chat-bot-icon" aria-hidden>
            <ChatBotAvatar size={44} className="chat-header-bot" />
          </div>
          <div className="chat-card-title" id="chat-dock-title">
            <strong>NAIS asistent</strong>
            <span>Predmeti, ocene i kurikulum · samo za tvoj smer</span>
          </div>
          <button type="button" className="chat-new" onClick={resetChat}>
            Novi razgovor
          </button>
          <button
            type="button"
            className="chat-popup-x"
            onClick={() => setChatOpen(false)}
            aria-label="Zatvori prozor četa"
          >
            ×
          </button>
        </div>

        {chatError ? <div className="chat-banner-err">{chatError}</div> : null}

        <div className="chat-thread" role="log" aria-live="polite">
          {chatMessages.map((msg) => (
            <div
              key={msg.id}
              className={`chat-row ${msg.role === 'user' ? 'user' : 'assistant'}`}
            >
              <div className="chat-avatar" aria-hidden>
                {msg.role === 'user' ? (
                  <span className="chat-avatar-inner">Ti</span>
                ) : (
                  <span className="chat-avatar-inner chat-avatar-inner--bot" title="Asistent">
                    <ChatBotAvatar size={28} className="chat-thread-bot" />
                  </span>
                )}
              </div>
              <div className="chat-col">
                <div className={`chat-bubble ${msg.role === 'user' ? 'chat-bubble--user' : 'chat-bubble--assistant'}`}>
                  {msg.role === 'assistant' ? (
                    <FormattedBubbleText text={msg.text} />
                  ) : (
                    <FormattedBubbleText text={msg.text} className="chat-formatted chat-formatted--user" />
                  )}
                </div>
              </div>
            </div>
          ))}
          {chatPending ? (
            <div className="chat-row assistant">
              <div className="chat-avatar" aria-hidden>
                <span className="chat-avatar-inner chat-avatar-inner--bot">
                  <ChatBotAvatar size={28} className="chat-thread-bot" />
                </span>
              </div>
              <div className="chat-typing-bubble" aria-label="Asistent traži odgovor">
                <div className="chat-typing-dots" aria-hidden>
                  <span />
                  <span />
                  <span />
                </div>
                <span className="chat-typing-text">Pretražujem bazu i kurikulum…</span>
              </div>
            </div>
          ) : null}
          <div ref={chatEndRef} />
        </div>

        <ChatQuickStarters
          disabled={chatPending}
          onPick={(q) => void sendAssistantMessage(q)}
        />
        <div className="chat-composer">
          <textarea
            ref={chatInputRef}
            value={chatInput}
            onChange={(e) => setChatInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault()
                void sendAssistantMessage()
              }
            }}
            placeholder="Napiši pitanje ili izaberi predlog iznad…"
            rows={2}
            disabled={chatPending}
            aria-label="Poruka asistentu"
          />
          <button
            type="button"
            className="chat-send-btn"
            onClick={() => void sendAssistantMessage()}
            disabled={chatPending || !chatInput.trim()}
            aria-label="Pošalji poruku"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden>
              <path
                d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          </button>
        </div>
        <div className="chat-hint-bar">
          <kbd>Enter</kbd> šalje · <kbd>Shift</kbd>+<kbd>Enter</kbd> novi red
        </div>
      </div>
    </div>
  )

  return (
    <div className={`app${!authenticated ? ' app--login' : ''}`}>
      {!authenticated ? (
        <div className="login-page-dj">
          <main className="login-card-dj">
            <div className="login-brand-dj">
              <img
                className="login-brand-logo-dj"
                src={loginLogoSrc}
                width={64}
                height={72}
                alt=""
                decoding="async"
                onError={() => setLoginLogoSrc(BRAND_LOGO_SRC)}
              />
              <div className="login-brand-titles-dj">
                <span className="login-brand-name-dj">DjordUNI</span>
                <span className="login-brand-sub-dj">Fakultet tehničkih nauka</span>
              </div>
            </div>
            <h1 className="login-heading-dj">Prijava</h1>
            <p className="login-lead-dj">Unesi podatke za nastavak</p>
            <form onSubmit={login} className="login-form-dj">
              <label className="login-label-dj">
                Email
                <input
                  className="login-input-dj"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="username"
                  placeholder="ime.prezime@djorduni.edu"
                />
              </label>
              <label className="login-label-dj">
                Lozinka
                <input
                  className="login-input-dj"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete="current-password"
                  placeholder="••••••••"
                />
              </label>
              <div className="login-extras-dj">
                <label className="login-remember-dj">
                  <input
                    type="checkbox"
                    checked={rememberMe}
                    onChange={(e) => setRememberMe(e.target.checked)}
                  />
                  Zapamti me
                </label>
               
              </div>
              {error ? <p className="err login-err-dj">{error}</p> : null}
              <button type="submit" className="login-submit-dj" disabled={authLoading}>
                {authLoading ? '…' : 'Prijavi se'}
              </button>
            </form>
         
           
          </main>
        </div>
      ) : (
        <>
          {role === 'STUDENT' ? (
            <main className="panel panel--fab panel--student-portal">
              <StudentPortal displayName={name || 'Student'} onLogout={logout} />
            </main>
          ) : (
            <div className="sp-layout dj-shell head-portal-wrap">
              <header className="dj-topnav" role="banner">
                <div className="dj-brand">
                  <img
                    className="dj-brand-logo"
                    src={loginLogoSrc}
                    width={56}
                    height={64}
                    alt=""
                    decoding="async"
                    onError={() => setLoginLogoSrc(BRAND_LOGO_SRC)}
                  />
                  <div className="dj-brand-text">
                    <span className="dj-brand-name">DjordUNI</span>
                    <span className="dj-brand-tag">
                      {role === 'SEF_KATEDRE' ? 'šef katedre' : 'nastavnik'}
                    </span>
                  </div>
                </div>
                <nav className="dj-nav" aria-label="Glavna navigacija">
                  {role === 'SEF_KATEDRE' ? (
                    <>
                      <button
                        type="button"
                        className={`dj-nav-item${staffSection === 'analytics' ? ' dj-nav-item--active' : ''}`}
                        onClick={() => setStaffSection('analytics')}
                      >
                        Analitika
                      </button>
                      <button
                        type="button"
                        className={`dj-nav-item${staffSection === 'students' ? ' dj-nav-item--active' : ''}`}
                        onClick={() => setStaffSection('students')}
                      >
                        Studenti
                      </button>
                    </>
                  ) : null}
                  <button
                    type="button"
                    className={`dj-nav-item${staffSection === 'stats' ? ' dj-nav-item--active' : ''}`}
                    onClick={() => setStaffSection('stats')}
                  >
                    Statistika
                  </button>
                </nav>
                <div className="dj-top-right">
                  <span className="dj-student-name">{name}</span>
                  <button type="button" className="dj-logout-link" onClick={logout}>
                    Odjava
                  </button>
                </div>
              </header>

              <div className="sp-main dj-main head-portal-main">
                {role === 'SEF_KATEDRE' && staffSection !== 'stats' ? (
                  <HeadPortal view={staffSection} />
                ) : null}

                {(role !== 'SEF_KATEDRE' || staffSection === 'stats') ? (
                  <CassandraStatsPage isHeadOfDepartment={role === 'SEF_KATEDRE'} />
                ) : null}

                {error ? <p className="err head-portal-err">{error}</p> : null}
              </div>
            </div>
          )}

          {role === 'STUDENT' ? (
            <div className="chat-dock-root">
              {chatOpen ? (
                <button
                  type="button"
                  className="chat-backdrop"
                  aria-label="Zatvori asistenta"
                  onClick={() => setChatOpen(false)}
                />
              ) : null}
              <div className="chat-dock">
                {chatOpen ? chatPanel : null}
                <button
                  type="button"
                  className={`chat-fab ${chatOpen ? 'chat-fab--open' : ''}`}
                  onClick={() => setChatOpen((o) => !o)}
                  aria-expanded={chatOpen}
                  title={chatOpen ? 'Zatvori asistenta' : 'Otvori asistenta'}
                >
                  <span className="chat-fab-glow" aria-hidden />
                  {chatOpen ? (
                    <FabCloseIcon />
                  ) : (
                    <ChatBotAvatar size={36} fabStyle />
                  )}
                </button>
              </div>
            </div>
          ) : null}
        </>
      )}
    </div>
  )
}
