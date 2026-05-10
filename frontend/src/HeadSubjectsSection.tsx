import { useCallback, useEffect, useMemo, useState } from 'react'
import { naisApi } from './api'
import type { HeadPredmetDetail, HeadPredmetUpsertPayload, HeadProgramSummary } from './headTypes'

type FormFields = {
  sifra: string
  naziv: string
  espbStr: string
  studijskiProgramId: string
  kurikulumGodinaStr: string
  kurikulumSemestarStr: string
  kratakOpis: string
  cilj: string
  ishodiUcenja: string
  metodeNastave: string
  temeKursa: string
}

type ModalKind = 'add' | 'edit' | 'detail'

const emptyForm: FormFields = {
  sifra: '',
  naziv: '',
  espbStr: '6',
  studijskiProgramId: '',
  kurikulumGodinaStr: '1',
  kurikulumSemestarStr: '1',
  kratakOpis: '',
  cilj: '',
  ishodiUcenja: '',
  metodeNastave: '',
  temeKursa: '',
}

function detailToForm(d: HeadPredmetDetail): FormFields {
  return {
    sifra: d.sifra ?? '',
    naziv: d.naziv ?? '',
    espbStr: String(d.espb ?? ''),
    studijskiProgramId: String(d.studijskiProgramId ?? ''),
    kurikulumGodinaStr: String(d.kurikulumGodina ?? 1),
    kurikulumSemestarStr: String(d.kurikulumSemestar ?? 1),
    kratakOpis: d.kratakOpis ?? '',
    cilj: d.cilj ?? '',
    ishodiUcenja: d.ishodiUcenja ?? '',
    metodeNastave: d.metodeNastave ?? '',
    temeKursa: d.temeKursa ?? '',
  }
}

function formToPayload(f: FormFields): HeadPredmetUpsertPayload {
  const espb = Number.parseInt(f.espbStr.trim(), 10)
  const pid = Number.parseInt(f.studijskiProgramId, 10)
  const kg = Number.parseInt(f.kurikulumGodinaStr.trim(), 10)
  const ks = Number.parseInt(f.kurikulumSemestarStr.trim(), 10)
  return {
    sifra: f.sifra.trim(),
    naziv: f.naziv.trim(),
    espb,
    studijskiProgramId: pid,
    kurikulumGodina: kg,
    kurikulumSemestar: ks,
    kratakOpis: f.kratakOpis.trim() ? f.kratakOpis.trim() : null,
    cilj: f.cilj.trim() ? f.cilj.trim() : null,
    ishodiUcenja: f.ishodiUcenja.trim() ? f.ishodiUcenja.trim() : null,
    metodeNastave: f.metodeNastave.trim() ? f.metodeNastave.trim() : null,
    temeKursa: f.temeKursa.trim() ? f.temeKursa.trim() : null,
  }
}

function nvl(t: string) {
  const s = (t ?? '').trim()
  return s.length === 0 ? '—' : s
}

export function HeadSubjectsSection() {
  const [programs, setPrograms] = useState<HeadProgramSummary[]>([])
  const [predmeti, setPredmeti] = useState<HeadPredmetDetail[]>([])
  const [filterProgramId, setFilterProgramId] = useState<number | ''>('')
  const [loading, setLoading] = useState(true)
  const [err, setErr] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [modalKind, setModalKind] = useState<ModalKind>('add')
  const [editingId, setEditingId] = useState<number | null>(null)
  const [detailRow, setDetailRow] = useState<HeadPredmetDetail | null>(null)
  const [form, setForm] = useState<FormFields>(emptyForm)
  const [modalErr, setModalErr] = useState('')
  const [modalBusy, setModalBusy] = useState(false)

  const reload = useCallback(async () => {
    setErr('')
    setLoading(true)
    try {
      const [progs, subj] = await Promise.all([naisApi.headPrograms(), naisApi.headSubjectsList()])
      setPrograms(Array.isArray(progs) ? progs : [])
      setPredmeti(Array.isArray(subj) ? subj : [])
    } catch (e) {
      let msg =
        e instanceof Error
          ? e.message
          : 'Ne mogu da učitam predmete.'
      if (/403|Forbidden/i.test(msg)) msg += ' Za ovu akciju potrebna je uloga šefa katedre.'
      setErr(msg)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void reload()
  }, [reload])

  const filteredRows = useMemo(() => {
    if (filterProgramId === '') return predmeti
    return predmeti.filter((p) => p.studijskiProgramId === filterProgramId)
  }, [predmeti, filterProgramId])

  const openAdd = () => {
    const firstPid = programs[0]?.id
    setModalKind('add')
    setEditingId(null)
    setDetailRow(null)
    setForm({
      ...emptyForm,
      studijskiProgramId: firstPid != null ? String(firstPid) : '',
    })
    setModalErr('')
    setModalOpen(true)
  }

  const openDetail = (d: HeadPredmetDetail) => {
    setModalKind('detail')
    setEditingId(null)
    setDetailRow(d)
    setModalErr('')
    setModalOpen(true)
  }

  const openEdit = (d: HeadPredmetDetail) => {
    setModalKind('edit')
    setEditingId(d.id)
    setDetailRow(null)
    setForm(detailToForm(d))
    setModalErr('')
    setModalOpen(true)
  }

  const fromDetailToEdit = () => {
    if (!detailRow) return
    setModalKind('edit')
    setEditingId(detailRow.id)
    setForm(detailToForm(detailRow))
    setModalErr('')
  }

  const closeModal = () => {
    setModalOpen(false)
    setModalBusy(false)
    setDetailRow(null)
  }

  const saveModal = async () => {
    setModalErr('')
    const espbN = Number.parseInt(form.espbStr.trim(), 10)
    const pid = Number.parseInt(form.studijskiProgramId, 10)
    const kg = Number.parseInt(form.kurikulumGodinaStr.trim(), 10)
    const ks = Number.parseInt(form.kurikulumSemestarStr.trim(), 10)
    if (!Number.isFinite(espbN) || espbN <= 0) {
      setModalErr('ESPB mora biti pozitivan ceo broj.')
      return
    }
    if (!Number.isFinite(pid) || pid <= 0) {
      setModalErr('Izaberi studijski program.')
      return
    }
    if (!Number.isFinite(kg) || kg < 1 || kg > 4) {
      setModalErr('Kurikulum godina mora biti broj od 1 do 4.')
      return
    }
    if (!Number.isFinite(ks) || (ks !== 1 && ks !== 2)) {
      setModalErr('Kurikulum semestar mora biti 1 ili 2.')
      return
    }
    if (!form.sifra.trim()) {
      setModalErr('Šifra predmeta je obavezna.')
      return
    }
    if (!form.naziv.trim()) {
      setModalErr('Naziv predmeta je obavezan.')
      return
    }
    const body = formToPayload(form)
    setModalBusy(true)
    try {
      if (modalKind === 'add') {
        await naisApi.headSubjectCreate(body)
      } else if (modalKind === 'edit' && editingId != null) {
        await naisApi.headSubjectUpdate(editingId, body)
      }
      await reload()
      closeModal()
    } catch (e: unknown) {
      const raw =
        e instanceof Error ? e.message : 'Greška pri snimanju.'
      setModalErr(raw.slice(0, 520))
      setModalBusy(false)
    }
  }

  return (
    <>
      <div className="head-section head-subjects-toolbar">
        <div className="head-program-picker head-subjects-picker">
          <label className="head-program-picker-label" htmlFor="head-subjects-filter-program">
            Filtriraj po programu
          </label>
          <select
            id="head-subjects-filter-program"
            className="head-program-select"
            value={filterProgramId === '' ? '' : String(filterProgramId)}
            onChange={(e) => {
              const v = e.target.value
              setFilterProgramId(v === '' ? '' : Number(v))
            }}
            disabled={loading || programs.length === 0}
          >
            <option value="">Svi programi vaše katedre</option>
            {programs.map((p) => (
              <option key={p.id} value={p.id}>
                {p.sifra} — {p.naziv}
              </option>
            ))}
          </select>
        </div>
        <button type="button" className="dj-nav-item head-subjects-add" onClick={openAdd}>
          Dodaj predmet
        </button>
      </div>

      {loading ? <p className="head-portal-loading">Učitavam predmete…</p> : null}
      {err ? <p className="err head-portal-err">{err}</p> : null}

      {!loading && !err ? (
        <div className="head-table-wrap">
          <table className="head-stat-table head-subj-table">
            <thead>
              <tr>
                <th>Šifra</th>
                <th>Naziv</th>
                <th>ESPB</th>
                <th>Program</th>
                <th>U kurikulumu</th>
                <th>Akcije</th>
              </tr>
            </thead>
            <tbody>
              {filteredRows.length === 0 ? (
                <tr>
                  <td colSpan={6} className="head-stat-empty">
                    {predmeti.length === 0
                      ? 'Nema evidentiranih predmeta za vašu katedru.'
                      : 'Nema predmeta koji odgovaraju izabranom filt.'}
                  </td>
                </tr>
              ) : (
                filteredRows.map((row) => (
                  <tr
                    key={row.id}
                    className="head-subj-row"
                    tabIndex={0}
                    role="button"
                    title="Detaljni prikaz"
                    aria-label={`Detalji: ${row.sifra}, ${row.naziv}`}
                    onClick={() => openDetail(row)}
                    onKeyDown={(ev) => {
                      if (ev.key === 'Enter' || ev.key === ' ') {
                        ev.preventDefault()
                        openDetail(row)
                      }
                    }}
                  >
                    <td className="head-pr-sifra">{row.sifra}</td>
                    <td className="head-pr-naz">{row.naziv}</td>
                    <td className="head-pr-espb">{row.espb}</td>
                    <td>{row.studijskiProgramSifra}</td>
                    <td>
                      god. {row.kurikulumGodina}, sem. {row.kurikulumSemestar}
                    </td>
                    <td className="head-subj-actions-cell" onClick={(e) => e.stopPropagation()} onKeyDown={(e) => e.stopPropagation()}>
                      <button
                        type="button"
                        className="head-subj-edit-btn"
                        onClick={() => openEdit(row)}
                      >
                        Izmeni
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      ) : null}

      {modalOpen ? (
        <>
          <div
            className="head-modal-backdrop"
            role="presentation"
            onClick={() => (modalBusy ? null : closeModal())}
          />
          <div
            className={`head-modal-dialog${modalKind === 'detail' ? ' head-modal-dialog--wide' : ''}`}
            role="dialog"
            aria-modal="true"
            aria-labelledby={modalKind === 'detail' ? 'head-subj-detail-title' : 'head-subj-form-title'}
          >
            {modalKind === 'detail' && detailRow ? (
              <>
                <h3 id="head-subj-detail-title" className="head-section-title head-modal-title">
                  Detalji predmeta
                </h3>
                <section className="head-detail-section" aria-labelledby="head-subj-d-osnovni">
                  <h4 id="head-subj-d-osnovni" className="head-detail-h">
                    Osnovni podaci
                  </h4>
                  <dl className="head-detail-dl">
                    <dt>Šifra</dt>
                    <dd>{detailRow.sifra}</dd>
                    <dt>Naziv</dt>
                    <dd>{detailRow.naziv}</dd>
                    <dt>ESPB</dt>
                    <dd>{detailRow.espb}</dd>
                    <dt>Program</dt>
                    <dd>
                      {detailRow.studijskiProgramSifra} — {detailRow.studijskiProgramNaziv}
                    </dd>
                    <dt>Kurikulum</dt>
                    <dd>
                      {detailRow.kurikulumGodina}. godina, {detailRow.kurikulumSemestar}. semestar
                    </dd>
                  </dl>
                </section>
                <section className="head-detail-section" aria-labelledby="head-subj-d-sadrz">
                  <h4 id="head-subj-d-sadrz" className="head-detail-h">
                    Sadržaj predmeta
                  </h4>
                  <p className="head-detail-subh">Kratak opis</p>
                  <pre className="head-detail-pre">{nvl(detailRow.kratakOpis)}</pre>
                  <p className="head-detail-subh">Cilj</p>
                  <pre className="head-detail-pre">{nvl(detailRow.cilj)}</pre>
                  <p className="head-detail-subh">Ishodi učenja</p>
                  <pre className="head-detail-pre">{nvl(detailRow.ishodiUcenja)}</pre>
                  <p className="head-detail-subh">Metode nastave</p>
                  <pre className="head-detail-pre">{nvl(detailRow.metodeNastave)}</pre>
                  <p className="head-detail-subh">Teme kursa</p>
                  <pre className="head-detail-pre">{nvl(detailRow.temeKursa)}</pre>
                </section>
                <div className="head-modal-actions">
                  <button type="button" className="dj-nav-item head-modal-cancel" onClick={closeModal}>
                    Zatvori
                  </button>
                  <button type="button" className="head-subj-edit-btn" onClick={fromDetailToEdit}>
                    Izmeni
                  </button>
                </div>
              </>
            ) : (
              <>
                <h3 id="head-subj-form-title" className="head-section-title head-modal-title">
                  {modalKind === 'add' ? 'Novi predmet' : 'Izmena predmeta'}
                </h3>
                <div className="head-modal-form">
                  <label className="head-modal-field">
                    <span>Šifra predmeta *</span>
                    <input
                      value={form.sifra}
                      onChange={(e) => setForm((x) => ({ ...x, sifra: e.target.value }))}
                      disabled={modalBusy || modalKind === 'edit'}
                      maxLength={20}
                      className="head-modal-input"
                      autoComplete="off"
                    />
                  </label>
                  <label className="head-modal-field">
                    <span>Naziv *</span>
                    <input
                      value={form.naziv}
                      onChange={(e) => setForm((x) => ({ ...x, naziv: e.target.value }))}
                      disabled={modalBusy}
                      maxLength={255}
                      className="head-modal-input"
                    />
                  </label>
                  <label className="head-modal-field">
                    <span>ESPB *</span>
                    <input
                      type="number"
                      min={1}
                      step={1}
                      value={form.espbStr}
                      onChange={(e) => setForm((x) => ({ ...x, espbStr: e.target.value }))}
                      disabled={modalBusy}
                      className="head-modal-input"
                    />
                  </label>
                  <label className="head-modal-field">
                    <span>Studijski program *</span>
                    <select
                      className="head-program-select head-modal-select"
                      value={form.studijskiProgramId}
                      onChange={(e) => setForm((x) => ({ ...x, studijskiProgramId: e.target.value }))}
                      disabled={modalBusy || programs.length === 0}
                    >
                      {programs.map((p) => (
                        <option key={p.id} value={p.id}>
                          {p.sifra} — {p.naziv}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label className="head-modal-field">
                    <span>Kurikulum godina *</span>
                    <input
                      type="number"
                      min={1}
                      max={4}
                      step={1}
                      value={form.kurikulumGodinaStr}
                      onChange={(e) => setForm((x) => ({ ...x, kurikulumGodinaStr: e.target.value }))}
                      disabled={modalBusy}
                      className="head-modal-input"
                    />
                  </label>
                  <label className="head-modal-field">
                    <span>Kurikulum semestar *</span>
                    <select
                      className="head-program-select head-modal-select"
                      value={form.kurikulumSemestarStr}
                      onChange={(e) => setForm((x) => ({ ...x, kurikulumSemestarStr: e.target.value }))}
                      disabled={modalBusy}
                    >
                      <option value="1">1</option>
                      <option value="2">2</option>
                    </select>
                  </label>
                  <label className="head-modal-field">
                    <span>Kratak opis</span>
                    <textarea
                      rows={3}
                      value={form.kratakOpis}
                      onChange={(e) => setForm((x) => ({ ...x, kratakOpis: e.target.value }))}
                      disabled={modalBusy}
                      className="head-modal-textarea"
                    />
                  </label>
                  <label className="head-modal-field">
                    <span>Cilj</span>
                    <textarea
                      rows={3}
                      value={form.cilj}
                      onChange={(e) => setForm((x) => ({ ...x, cilj: e.target.value }))}
                      disabled={modalBusy}
                      className="head-modal-textarea"
                    />
                  </label>
                  <label className="head-modal-field">
                    <span>Ishodi učenja</span>
                    <textarea
                      rows={3}
                      value={form.ishodiUcenja}
                      onChange={(e) => setForm((x) => ({ ...x, ishodiUcenja: e.target.value }))}
                      disabled={modalBusy}
                      className="head-modal-textarea"
                    />
                  </label>
                  <label className="head-modal-field">
                    <span>Metode nastave</span>
                    <textarea
                      rows={2}
                      value={form.metodeNastave}
                      onChange={(e) => setForm((x) => ({ ...x, metodeNastave: e.target.value }))}
                      disabled={modalBusy}
                      className="head-modal-textarea"
                    />
                  </label>
                  <label className="head-modal-field">
                    <span>Teme kursa</span>
                    <textarea
                      rows={2}
                      value={form.temeKursa}
                      onChange={(e) => setForm((x) => ({ ...x, temeKursa: e.target.value }))}
                      disabled={modalBusy}
                      className="head-modal-textarea"
                    />
                  </label>

                  {modalErr ? (
                    <p className="err head-portal-err head-modal-err" role="alert">
                      {modalErr}
                    </p>
                  ) : null}

                  <div className="head-modal-actions">
                    <button type="button" className="dj-nav-item head-modal-cancel" disabled={modalBusy} onClick={closeModal}>
                      Otkaži
                    </button>
                    <button type="button" className="dj-nav-item head-modal-submit" disabled={modalBusy} onClick={() => void saveModal()}>
                      {modalBusy ? 'Snimam…' : modalKind === 'add' ? 'Dodaj' : 'Sačuvaj'}
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        </>
      ) : null}
    </>
  )
}