import { useEffect, useState } from 'react';
import axios from 'axios';

const API = '/api/v1';
const CARD_TYPES = ['CSCS', 'CPCS', 'CPCS_BLUE', 'SIAS', 'CIP', 'ECS', 'FKC', 'OTHER'];
const QUAL_TYPES = ['CSCS', 'NPORS', 'CPCS', 'CPCS_BLUE', 'CITY_AND_GUILDS', 'NVQ', 'CITB', 'OTHER'];
const EMP_STATUS = ['PAYE', 'SELF_EMPLOYED', 'CIS', 'AGENCY'];
const STATUS_MAP = { ACTIVE: 'bg-green-100 text-green-700', INACTIVE: 'bg-gray-100 text-gray-600', SUSPENDED: 'bg-red-100 text-red-700', TERMINATED: 'bg-gray-100 text-gray-500' };
const EMP_MAP = { PAYE: 'PAYE', SELF_EMPLOYED: 'Self-Employed', CIS: 'CIS', AGENCY: 'Agency' };
const CARD_COLORS = { CSCS: 'bg-blue-50 border-blue-200', CPCS: 'bg-purple-50 border-purple-200', SIAS: 'bg-amber-50 border-amber-200', CIP: 'bg-teal-50 border-teal-200', CPCS_BLUE: 'bg-indigo-50 border-indigo-200' };

function SectionTitle({ children }) {
  return <h4 className="text-sm font-semibold text-slate-600 uppercase tracking-wider mb-3">{children}</h4>;
}

function Modal({ title, onClose, children, size = 'max-w-2xl' }) {
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className={`bg-white rounded-xl w-full ${size} max-h-[90vh] overflow-y-auto shadow-2xl`}>
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 sticky top-0 bg-white rounded-t-xl">
          <h3 className="text-lg font-semibold text-slate-800">{title}</h3>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 text-2xl leading-none w-8 h-8 flex items-center justify-center rounded-lg hover:bg-gray-100">&times;</button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
}

function Field({ label, children }) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-700 mb-1">{label}</label>
      {children}
    </div>
  );
}

function Input({ className = '', ...props }) {
  return <input {...props} className={`w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${className}`} />;
}

function Select({ children, ...props }) {
  return <select {...props} className={`w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 ${props.className || ''}`}>{children}</select>;
}

function Textarea({ ...props }) {
  return <textarea {...props} className={`w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none ${props.className || ''}`} />;
}

function Badge({ children, color = 'gray' }) {
  const colors = { green: 'bg-green-100 text-green-700', red: 'bg-red-100 text-red-700', amber: 'bg-amber-100 text-amber-700', gray: 'bg-gray-100 text-gray-600', blue: 'bg-blue-100 text-blue-700', purple: 'bg-purple-100 text-purple-700' };
  return <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${colors[color] || colors.gray}`}>{children}</span>;
}

function GateStatus({ status }) {
  if (!status) return null;
  const checks = [
    { label: 'CSCS/CPCS Valid', ok: status.isCSCSValid, key: 'cscs' },
    { label: 'RAMS Sign-on', ok: status.isRAMSValid, key: 'rams' },
    { label: 'Site Induction', ok: status.isInductionValid, key: 'induction' },
    { label: 'Plant Ticket', ok: status.isPlantTicketValid, key: 'plant' },
    { label: 'HMRC Verified', ok: status.isHMRCVerified, key: 'hmrc' },
  ];
  return (
    <div className={`rounded-xl p-4 ${status.isGateOpen ? 'bg-green-50 border border-green-200' : 'bg-red-50 border border-red-200'}`}>
      <div className="flex items-center gap-2 mb-3">
        <span className={`text-2xl ${status.isGateOpen ? 'text-green-600' : 'text-red-600'}`}>{status.isGateOpen ? '🔓' : '🔒'}</span>
        <div>
          <p className={`font-semibold ${status.isGateOpen ? 'text-green-800' : 'text-red-800'}`}>{status.isGateOpen ? 'Gate Open' : 'Gate Locked'}</p>
          <p className="text-sm text-slate-600">{status.statusMessage}</p>
        </div>
      </div>
      <div className="grid grid-cols-2 gap-2">
        {checks.map(c => (
          <div key={c.key} className={`flex items-center gap-2 text-xs px-2 py-1.5 rounded ${c.ok ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
            <span>{c.ok ? '✓' : '✗'}</span> {c.label}
          </div>
        ))}
      </div>
    </div>
  );
}

function OperativeForm({ operative, onSubmit, onClose }) {
  const [form, setForm] = useState(operative || {
    employeeRef: '', firstName: '', lastName: '', dateOfBirth: '', gender: '', nationality: '',
    niNumber: '', utr: '', rightToWorkExpiry: '', rightToWorkDocType: '', passportNumber: '',
    bankSortCode: '', bankAccountNumber: '', employmentStatus: 'PAYE', status: 'ACTIVE', employerId: ''
  });
  const [submitting, setSubmitting] = useState(false);

  const fc = (f) => (e) => setForm({ ...form, [f]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload = { ...form };
      if (form.utr) payload.utr = form.utr;
      if (form.employerId === '') payload.employerId = null;
      await onSubmit(payload);
      onClose();
    } finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Employee Ref *"><Input value={form.employeeRef} onChange={fc('employeeRef')} required placeholder="e.g. OP-001" /></Field>
        <Field label="Status"><Select value={form.status} onChange={fc('status')}><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option><option value="SUSPENDED">Suspended</option><option value="TERMINATED">Terminated</option></Select></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="First Name *"><Input value={form.firstName} onChange={fc('firstName')} required placeholder="John" /></Field>
        <Field label="Last Name *"><Input value={form.lastName} onChange={fc('lastName')} required placeholder="Smith" /></Field>
      </div>
      <div className="grid grid-cols-3 gap-4">
        <Field label="Date of Birth"><Input type="date" value={form.dateOfBirth} onChange={fc('dateOfBirth')} /></Field>
        <Field label="Gender">
          <Select value={form.gender} onChange={fc('gender')}>
            <option value="">Select</option>
            <option value="Male">Male</option>
            <option value="Female">Female</option>
            <option value="Other">Other</option>
          </Select>
        </Field>
        <Field label="Nationality"><Input value={form.nationality} onChange={fc('nationality')} placeholder="British" /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="NI Number"><Input value={form.niNumber} onChange={fc('niNumber')} placeholder="AB123456C" /></Field>
        <Field label="UTR"><Input value={form.utr} onChange={fc('utr')} placeholder="1234567890" /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Right to Work Expiry"><Input type="date" value={form.rightToWorkExpiry} onChange={fc('rightToWorkExpiry')} /></Field>
        <Field label="Right to Work Doc Type">
          <Select value={form.rightToWorkDocType} onChange={fc('rightToWorkDocType')}>
            <option value="">Select</option>
            <option value="PASSPORT">Passport</option>
            <option value="BRP">BRP</option>
            <option value="EAA">EAA</option>
            <option value="VISA">Visa</option>
          </Select>
        </Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Passport Number"><Input value={form.passportNumber} onChange={fc('passportNumber')} /></Field>
        <Field label="Employment Status">
          <Select value={form.employmentStatus} onChange={fc('employmentStatus')}>
            {EMP_STATUS.map(s => <option key={s} value={s}>{EMP_MAP[s]}</option>)}
          </Select>
        </Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Bank Sort Code"><Input value={form.bankSortCode} onChange={fc('bankSortCode')} placeholder="12-34-56" /></Field>
        <Field label="Bank Account Number"><Input value={form.bankAccountNumber} onChange={fc('bankAccountNumber')} placeholder="12345678" /></Field>
      </div>
      <Field label="Employer ID (Company)"><Input type="number" value={form.employerId} onChange={fc('employerId')} placeholder="Leave blank for direct employee" /></Field>
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
          {submitting ? 'Saving…' : operative ? 'Update Operative' : 'Create Operative'}
        </button>
      </div>
    </form>
  );
}

function CardForm({ operativeId, onClose, onAdded }) {
  const [form, setForm] = useState({ cardType: 'CSCS', scheme: '', cardNumber: '', expiryDate: '', competencyRef: '' });
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await axios.post(`${API}/operatives/${operativeId}/cards`, form);
      onAdded();
      onClose();
    } catch (err) { alert(err.response?.data?.message || 'Failed to add card'); }
    finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Card Type *">
          <Select value={form.cardType} onChange={e => setForm({ ...form, cardType: e.target.value })}>
            {CARD_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
          </Select>
        </Field>
        <Field label="Scheme"><Input value={form.scheme} onChange={e => setForm({ ...form, scheme: e.target.value })} placeholder="e.g. CSCS" /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Card Number *"><Input value={form.cardNumber} onChange={e => setForm({ ...form, cardNumber: e.target.value })} required placeholder="123456789" /></Field>
        <Field label="Expiry Date"><Input type="date" value={form.expiryDate} onChange={e => setForm({ ...form, expiryDate: e.target.value })} /></Field>
      </div>
      <Field label="Competency Ref"><Input value={form.competencyRef} onChange={e => setForm({ ...form, competencyRef: e.target.value })} placeholder="CSCS/123456" /></Field>
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
          {submitting ? 'Saving…' : 'Add Card'}
        </button>
      </div>
    </form>
  );
}

function QualificationForm({ operativeId, onClose, onAdded }) {
  const [form, setForm] = useState({ qualificationType: 'NPORS', level: '', awardingBody: '', certificateNumber: '', achievedDate: '', expiryDate: '', notes: '' });
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await axios.post(`${API}/operatives/${operativeId}/qualifications`, form);
      onAdded();
      onClose();
    } catch (err) { alert(err.response?.data?.message || 'Failed to add qualification'); }
    finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Qualification Type *">
          <Select value={form.qualificationType} onChange={e => setForm({ ...form, qualificationType: e.target.value })}>
            {QUAL_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
          </Select>
        </Field>
        <Field label="Level"><Input value={form.level} onChange={e => setForm({ ...form, level: e.target.value })} placeholder="e.g. NVQ Level 3" /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Awarding Body"><Input value={form.awardingBody} onChange={e => setForm({ ...form, awardingBody: e.target.value })} placeholder="NPORS" /></Field>
        <Field label="Certificate Number"><Input value={form.certificateNumber} onChange={e => setForm({ ...form, certificateNumber: e.target.value })} /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Achieved Date"><Input type="date" value={form.achievedDate} onChange={e => setForm({ ...form, achievedDate: e.target.value })} /></Field>
        <Field label="Expiry Date"><Input type="date" value={form.expiryDate} onChange={e => setForm({ ...form, expiryDate: e.target.value })} /></Field>
      </div>
      <Field label="Notes"><Textarea value={form.notes} onChange={e => setForm({ ...form, notes: e.target.value })} placeholder="Additional notes…" rows={2} /></Field>
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
          {submitting ? 'Saving…' : 'Add Qualification'}
        </button>
      </div>
    </form>
  );
}

function OperativeDetail({ operative, onClose, onDelete, onEdit, refresh }) {
  const [tab, setTab] = useState('overview');
  const [cards, setCards] = useState([]);
  const [quals, setQuals] = useState([]);
  const [gateStatus, setGateStatus] = useState(null);
  const [loadingCards, setLoadingCards] = useState(false);
  const [loadingQuals, setLoadingQuals] = useState(false);

  const loadCards = async () => {
    setLoadingCards(true);
    try { setCards(await axios.get(`${API}/operatives/${operative.id}/cards`).then(r => r.data.data || [])); }
    catch { setCards([]); }
    finally { setLoadingCards(false); }
  };

  const loadQuals = async () => {
    setLoadingQuals(true);
    try { setQuals(await axios.get(`${API}/operatives/${operative.id}/qualifications`).then(r => r.data.data || [])); }
    catch { setQuals([]); }
    finally { setLoadingQuals(false); }
  };

  const loadGateStatus = async () => {
    try { setGateStatus(await axios.get(`${API}/operatives/${operative.id}/subbie-gate-status`).then(r => r.data.data)); }
    catch { setGateStatus(null); }
  };

  const smartCheck = async (cardId) => {
    if (!confirm('Run CSCS Smart Check on this card?')) return;
    try {
      const result = await axios.post(`${API}/operatives/${operative.id}/cards/${cardId}/cscs-smart-check`).then(r => r.data.data);
      alert(`Smart Check Result: ${result.statusMessage}`);
    } catch (err) { alert('Smart check failed'); }
  };

  const deleteCard = async (cardId) => {
    if (!confirm('Delete this card?')) return;
    await axios.delete(`${API}/operatives/${operative.id}/cards/${cardId}`);
    loadCards();
  };

  const deleteQual = async (qualId) => {
    if (!confirm('Delete this qualification?')) return;
    await axios.delete(`${API}/operatives/${operative.id}/qualifications/${qualId}`);
    loadQuals();
  };

  useEffect(() => {
    if (tab === 'cards') loadCards();
    if (tab === 'qualifications') loadQuals();
    if (tab === 'gate') loadGateStatus();
  }, [tab]);

  const infoRow = (label, value) => value ? (
    <div className="flex justify-between py-2 border-b border-gray-50 last:border-0">
      <span className="text-sm text-slate-500">{label}</span>
      <span className="text-sm text-slate-800 font-medium text-right max-w-[60%]">{value}</span>
    </div>
  ) : null;

  return (
    <Modal title={`Operative: ${operative.fullName}`} onClose={onClose} size="max-w-3xl">
      <div className="flex items-center gap-4 mb-6 pb-4 border-b border-gray-200">
        <div className="w-14 h-14 rounded-full bg-blue-100 flex items-center justify-center text-xl font-bold text-blue-700">
          {operative.firstName?.[0]}{operative.lastName?.[0]}
        </div>
        <div>
          <h2 className="text-xl font-bold text-slate-800">{operative.fullName}</h2>
          <p className="text-sm text-slate-500">Ref: {operative.employeeRef} · {operative.status}</p>
        </div>
        <div className="ml-auto flex gap-2">
          <button onClick={() => { onEdit(); onClose(); }} className="px-3 py-1.5 text-xs border rounded-lg hover:bg-gray-50">Edit</button>
          <button onClick={() => { if (confirm('Delete this operative?')) { onDelete(operative.id); onClose(); } }} className="px-3 py-1.5 text-xs border border-red-200 text-red-600 rounded-lg hover:bg-red-50">Delete</button>
        </div>
      </div>

      {/* Tab bar */}
      <div className="flex gap-1 mb-5 bg-slate-100 p-1 rounded-lg w-fit">
        {['overview', 'cards', 'qualifications', 'gate'].map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-4 py-1.5 text-xs font-medium rounded-md transition-colors ${tab === t ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}>
            {t.charAt(0).toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {tab === 'overview' && (
        <div className="space-y-1">
          {infoRow('NI Number', operative.niNumber)}
          {infoRow('UTR', operative.utr)}
          {infoRow('Employment Status', operative.employmentStatus)}
          {infoRow('Status', operative.status)}
          {infoRow('Date of Birth', operative.dateOfBirth)}
          {infoRow('Nationality', operative.nationality)}
          {infoRow('Right to Work Expiry', operative.rightToWorkExpiry)}
          {infoRow('Right to Work Doc Type', operative.rightToWorkDocType)}
          {infoRow('Passport Number', operative.passportNumber)}
          {infoRow('Bank Sort Code', operative.bankSortCode)}
          {infoRow('Bank Account', operative.bankAccountNumber)}
          {infoRow('Employer', operative.employerName || 'Direct')}
        </div>
      )}

      {tab === 'cards' && (
        <div>
          <div className="flex justify-between mb-4">
            <p className="text-sm text-slate-500">{cards.length} card(s)</p>
            <button onClick={() => setTab('add-card')} className="px-3 py-1.5 text-xs bg-blue-600 text-white rounded-lg hover:bg-blue-700">+ Add Card</button>
          </div>
          {loadingCards ? <p className="text-sm text-slate-400">Loading…</p> : cards.length > 0 ? (
            <div className="space-y-3">
              {cards.map(card => (
                <div key={card.id} className={`rounded-xl p-4 border ${CARD_COLORS[card.cardType] || 'bg-gray-50 border-gray-200'}`}>
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-semibold text-slate-800">{card.cardType}</span>
                        {card.isValid ? <Badge color="green">Valid</Badge> : <Badge color="red">Expired</Badge>}
                        {card.isExpiringSoon && <Badge color="amber">Expiring Soon</Badge>}
                      </div>
                      <p className="text-sm text-slate-600 mt-1">#{card.cardNumber}</p>
                      <p className="text-xs text-slate-400">Expiry: {card.expiryDate || 'N/A'}</p>
                      {card.competencyRef && <p className="text-xs text-slate-400">Comp Ref: {card.competencyRef}</p>}
                    </div>
                    <div className="flex gap-1">
                      {(card.cardType === 'CSCS' || card.cardType === 'CPCS') && (
                        <button onClick={() => smartCheck(card.id)} className="px-2 py-1 text-xs border rounded hover:bg-white">Smart Check</button>
                      )}
                      <button onClick={() => deleteCard(card.id)} className="px-2 py-1 text-xs border border-red-200 text-red-600 rounded hover:bg-red-50">Delete</button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : <p className="text-sm text-slate-400">No cards added</p>}
        </div>
      )}

      {tab === 'add-card' && <CardForm operativeId={operative.id} onClose={() => setTab('cards')} onAdded={loadCards} />}

      {tab === 'qualifications' && (
        <div>
          <div className="flex justify-between mb-4">
            <p className="text-sm text-slate-500">{quals.length} qualification(s)</p>
            <button onClick={() => setTab('add-qual')} className="px-3 py-1.5 text-xs bg-blue-600 text-white rounded-lg hover:bg-blue-700">+ Add Qualification</button>
          </div>
          {loadingQuals ? <p className="text-sm text-slate-400">Loading…</p> : quals.length > 0 ? (
            <div className="space-y-3">
              {quals.map(q => (
                <div key={q.id} className="rounded-xl p-4 border border-green-200 bg-green-50">
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-semibold text-slate-800">{q.qualificationType}</span>
                        {q.level && <span className="text-xs text-slate-500">{q.level}</span>}
                        {q.isValid ? <Badge color="green">Valid</Badge> : <Badge color="red">Expired</Badge>}
                        {q.isExpiringSoon && <Badge color="amber">Expiring Soon</Badge>}
                      </div>
                      {q.awardingBody && <p className="text-sm text-slate-600 mt-1">{q.awardingBody}</p>}
                      {q.certificateNumber && <p className="text-xs text-slate-400">Cert: {q.certificateNumber}</p>}
                      {(q.achievedDate || q.expiryDate) && <p className="text-xs text-slate-400">{q.achievedDate || '?'} → {q.expiryDate || 'No expiry'}</p>}
                    </div>
                    <button onClick={() => deleteQual(q.id)} className="px-2 py-1 text-xs border border-red-200 text-red-600 rounded hover:bg-red-50">Delete</button>
                  </div>
                </div>
              ))}
            </div>
          ) : <p className="text-sm text-slate-400">No qualifications added</p>}
        </div>
      )}

      {tab === 'add-qual' && <QualificationForm operativeId={operative.id} onClose={() => setTab('qualifications')} onAdded={loadQuals} />}

      {tab === 'gate' && (
        <div>
          <GateStatus status={gateStatus} />
          <button onClick={loadGateStatus} className="mt-4 px-4 py-2 text-sm border rounded-lg hover:bg-gray-50">Refresh Status</button>
        </div>
      )}
    </Modal>
  );
}

export default function Operatives() {
  const [operatives, setOperatives] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(1);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editOp, setEditOp] = useState(null);
  const [selectedOp, setSelectedOp] = useState(null);

  const fetchOperatives = async (p = 0, s = search, st = statusFilter) => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page: p, size: 20, sort: 'lastName' });
      if (s) params.append('search', s);
      if (st) params.append('status', st);
      const res = await axios.get(`${API}/operatives?${params}`);
      const data = res.data.data;
      setOperatives(data.content || data || []);
      setTotalPages(data.totalPages || 1);
      setTotal(data.totalElements || 0);
      setPage(p);
    } catch { setOperatives([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchOperatives(); }, []);

  const handleCreate = async (form) => {
    const res = await axios.post(`${API}/operatives`, form);
    return res.data;
  };

  const handleUpdate = async (form) => {
    await axios.patch(`${API}/operatives/${editOp.id}`, form);
  };

  const handleDelete = async (id) => {
    await axios.delete(`${API}/operatives/${id}`);
    fetchOperatives(page, search, statusFilter);
  };

  const debounceSearch = (fn, ms = 400) => {
    let t; return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), ms); };
  };

  const debouncedFetch = debounceSearch((s) => fetchOperatives(0, s, statusFilter), 400);

  const statsCards = [
    { label: 'Total Operatives', value: total, icon: '👷', color: 'bg-blue-50' },
    { label: 'Active', value: operatives.filter(o => o.status === 'ACTIVE').length, color: 'bg-green-50' },
    { label: 'Inactive', value: operatives.filter(o => o.status === 'INACTIVE').length, color: 'bg-gray-50' },
    { label: 'Suspended', value: operatives.filter(o => o.status === 'SUSPENDED').length, color: 'bg-red-50' },
  ];

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold text-slate-800">Operatives</h2>
          <p className="text-sm text-slate-500 mt-1">Operative management, CSCS cards, qualifications and gate status</p>
        </div>
        <button onClick={() => { setEditOp(null); setShowForm(true); }} className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 flex items-center gap-2">
          + New Operative
        </button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-4 gap-4 mb-6">
        {statsCards.map(s => (
          <div key={s.label} className={`${s.color} rounded-xl p-4`}>
            <p className="text-xs text-slate-500">{s.label}</p>
            <p className="text-2xl font-bold text-slate-800 mt-1">{loading ? '…' : s.value}</p>
          </div>
        ))}
      </div>

      {/* Search + Filters */}
      <div className="flex gap-3 mb-6">
        <input value={search} onChange={e => { setSearch(e.target.value); debouncedFetch(e.target.value); }} placeholder="Search by name or employee ref…" className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        <select value={statusFilter} onChange={e => { setStatusFilter(e.target.value); fetchOperatives(0, search, e.target.value); }} className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white">
          <option value="">All Status</option>
          <option value="ACTIVE">Active</option>
          <option value="INACTIVE">Inactive</option>
          <option value="SUSPENDED">Suspended</option>
          <option value="TERMINATED">Terminated</option>
        </select>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-slate-50 border-b border-gray-100">
                <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Operative</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Employment</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Status</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Right to Work</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Employer</th>
                <th className="text-right px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={6} className="text-center py-8 text-slate-400">Loading…</td></tr>
              ) : operatives.length === 0 ? (
                <tr><td colSpan={6} className="text-center py-8 text-slate-400">No operatives found</td></tr>
              ) : operatives.map(op => (
                <tr key={op.id} className="border-b border-gray-50 hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-3">
                      <div className="w-9 h-9 rounded-full bg-blue-100 flex items-center justify-center text-sm font-bold text-blue-700 shrink-0">
                        {op.firstName?.[0]}{op.lastName?.[0]}
                      </div>
                      <div>
                        <p className="font-medium text-slate-800">{op.fullName}</p>
                        <p className="text-xs text-slate-400">{op.employeeRef}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-4"><span className="text-sm text-slate-600">{EMP_MAP[op.employmentStatus] || op.employmentStatus || '—'}</span></td>
                  <td className="px-5 py-4"><Badge color={op.status === 'ACTIVE' ? 'green' : op.status === 'SUSPENDED' ? 'red' : 'gray'}>{op.status || '—'}</Badge></td>
                  <td className="px-5 py-4"><span className="text-sm text-slate-600">{op.rightToWorkExpiry || '—'}</span></td>
                  <td className="px-5 py-4"><span className="text-sm text-slate-600">{op.employerName || 'Direct'}</span></td>
                  <td className="px-5 py-4 text-right">
                    <button onClick={() => setSelectedOp(op)} className="px-3 py-1 text-xs border rounded hover:bg-gray-50">View</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex justify-between items-center px-5 py-3 border-t border-gray-100">
            <p className="text-xs text-slate-500">Page {page + 1} of {totalPages} · {total} total</p>
            <div className="flex gap-1">
              <button disabled={page === 0} onClick={() => fetchOperatives(page - 1, search, statusFilter)} className="px-3 py-1 text-xs border rounded disabled:opacity-30 hover:bg-gray-50">Prev</button>
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const p = page < 3 ? i : page > totalPages - 3 ? totalPages - 5 + i : page - 2 + i;
                if (p < 0 || p >= totalPages) return null;
                return <button key={p} onClick={() => fetchOperatives(p, search, statusFilter)} className={`px-3 py-1 text-xs border rounded ${p === page ? 'bg-blue-600 text-white' : 'hover:bg-gray-50'}`}>{p + 1}</button>;
              })}
              <button disabled={page >= totalPages - 1} onClick={() => fetchOperatives(page + 1, search, statusFilter)} className="px-3 py-1 text-xs border rounded disabled:opacity-30 hover:bg-gray-50">Next</button>
            </div>
          </div>
        )}
      </div>

      {/* Create/Edit Modal */}
      {showForm && (
        <Modal title={editOp ? `Edit Operative: ${editOp.fullName}` : 'New Operative'} onClose={() => setShowForm(false)}>
          <OperativeForm
            operative={editOp}
            onSubmit={async (form) => {
              if (editOp) { await handleUpdate(form); } else { await handleCreate(form); }
              setShowForm(false);
              fetchOperatives(0, search, statusFilter);
            }}
            onClose={() => setShowForm(false)}
          />
        </Modal>
      )}

      {/* Detail Modal */}
      {selectedOp && (
        <OperativeDetail
          operative={selectedOp}
          onClose={() => setSelectedOp(null)}
          onDelete={handleDelete}
          onEdit={(op) => { setEditOp(selectedOp); setShowForm(true); }}
          refresh={() => fetchOperatives(page, search, statusFilter)}
        />
      )}
    </div>
  );
}