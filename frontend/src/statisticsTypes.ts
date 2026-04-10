export type ProgramSummary = {
  id: number
  sifra: string
  naziv: string
}

export type GenerationBreakdownRow = {
  godinaUpisa: number
  totalStudentsWhoTook: number
  totalStudentsWhoPassed: number
  totalStudentsWhoFailed: number
  passRate: number | null
  averagePassingGrade: number | null
}

export type SubjectStatisticsRow = {
  subjectId: number
  subjectCode: string
  subjectName: string
  espb: number
  kurikulumGodina: number
  semestar: number
  totalStudentsWhoTook: number
  totalStudentsWhoPassed: number
  totalStudentsWhoFailed: number
  passRate: number | null
  averagePassingGrade: number | null
  medianPassingGrade: number | null
  highestGrade: number | null
  lowestPassingGrade: number | null
  totalExamAttempts: number
  generationBreakdown: GenerationBreakdownRow[]
}

export type RankingsBundle = {
  hardestByPassRate: SubjectStatisticsRow[]
  easiestByPassRate: SubjectStatisticsRow[]
  highestAveragePassingGrade: SubjectStatisticsRow[]
  lowestAveragePassingGrade: SubjectStatisticsRow[]
}

export type ProgramStatisticsResponse = {
  program: ProgramSummary
  subjects: SubjectStatisticsRow[]
  rankings: RankingsBundle
  aggregationNote: string
}

export type StatisticsFilterOptions = {
  generacijeUpisa: number[]
  skolskeGodine: string[]
  predmeti: {
    id: number
    sifra: string
    naziv: string
    kurikulumGodina: number
    semestar: number
  }[]
}

export type UnpassedSubjectPassRate = {
  subjectCode: string
  subjectName: string
  kurikulumGodina: number
  kurikulumSemestar: number
  passRate: number | null
}

function num(v: unknown, fallback: number): number {
  if (typeof v === 'number' && !Number.isNaN(v)) return v
  if (typeof v === 'string' && v.trim() !== '') {
    const n = parseInt(v, 10)
    if (!Number.isNaN(n)) return n
  }
  return fallback
}

export function parseUnpassedSubjectPassRate(raw: unknown): UnpassedSubjectPassRate {
  if (raw == null || typeof raw !== 'object') {
    return {
      subjectCode: '',
      subjectName: '',
      kurikulumGodina: 0,
      kurikulumSemestar: 1,
      passRate: null,
    }
  }
  const r = raw as Record<string, unknown>
  const hasKg =
    (r.kurikulumGodina !== undefined && r.kurikulumGodina !== null && r.kurikulumGodina !== '') ||
    (r.kurikulum_godina !== undefined && r.kurikulum_godina !== null && r.kurikulum_godina !== '')
  const kgRaw = r.kurikulumGodina ?? r.kurikulum_godina
  const ksRaw = r.kurikulumSemestar ?? r.kurikulum_semestar
  const pr = r.passRate ?? r.pass_rate
  return {
    subjectCode: String(r.subjectCode ?? r.subject_code ?? ''),
    subjectName: String(r.subjectName ?? r.subject_name ?? ''),
    kurikulumGodina: hasKg ? num(kgRaw, 0) : 0,
    kurikulumSemestar: ksRaw !== undefined && ksRaw !== null && ksRaw !== '' ? num(ksRaw, 1) : 1,
    passRate: pr == null || pr === '' ? null : Number(pr),
  }
}
