/** Greška sa HTTP kodom za prikaz bez sirovog JSON-a */
export class ApiRequestError extends Error {
    readonly status: number
  
    constructor(status: number, message: string) {
      super(message)
      this.name = 'ApiRequestError'
      this.status = status
    }
  
    override toString(): string {
      return this.message
    }
  }
  
  const SR_STATUS_FALLBACK: Record<number, string> = {
    400: 'Zahtev nije validan.',
    401: 'Niste prijavljeni ili je sesija istekla.',
    403: 'Nemate dozvolu za ovu radnju.',
    404: 'Traženi podatak nije pronađen.',
    409: 'Zahtev nije mogao da se ispuni zbog konflikta podataka.',
    422: 'Podaci su nepodobni za obradu.',
    429: 'Previše zahteva. Pokušajte za trenutak.',
    500: 'Greška na serveru.',
    502: 'Privremeni problem sa pozadinskim servisom.',
    503: 'Servis je trenutno nedostupan.',
  }
  
  function fallbackForStatus(status: number): string {
    return SR_STATUS_FALLBACK[status] ?? `Greška (${status}).`
  }
  
  function extractFromFieldErrors(errors: unknown): string | null {
    if (!Array.isArray(errors) || errors.length === 0) return null
    const parts: string[] = []
    for (const e of errors) {
      if (typeof e !== 'object' || e === null) continue
      const o = e as Record<string, unknown>
      let part = ''
      if (typeof o.field === 'string' && o.field) part = `${o.field}: `
      if (typeof o.defaultMessage === 'string' && o.defaultMessage) {
        parts.push(`${part}${o.defaultMessage}`)
      }
    }
    return parts.length > 0 ? parts.join('; ') : null
  }
  
  /** Čita telo Spring / gateway JSON grešaka (message, ProblemDetail.detail, Bean Validation errors). */
  export function parseHttpErrorBody(status: number, rawText: string): string {
    const trimmed = rawText.trim()
    if (!trimmed) {
      return fallbackForStatus(status)
    }
    let j: Record<string, unknown>
    try {
      j = JSON.parse(trimmed) as Record<string, unknown>
    } catch {
      const short = trimmed.length > 380 ? `${trimmed.slice(0, 380)}…` : trimmed
      return short || fallbackForStatus(status)
    }
  
    const explicit =
      typeof j.message === 'string' && j.message.trim().length > 0
        ? j.message.trim()
        : typeof j.detail === 'string' && j.detail.trim().length > 0
          ? j.detail.trim()
          : null
  
    const fromErrors =
      extractFromFieldErrors(j.errors) ??
      extractFromFieldErrors(j.fieldErrors) ??
      extractFromFieldErrors(j.violations)
  
    const merged = (explicit ?? fromErrors)?.trim()
    const out = merged && merged.length > 0 ? merged : fallbackForStatus(status)
    return out.length > 600 ? `${out.slice(0, 600)}…` : out
  }