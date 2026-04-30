import { useEffect, useState, useCallback } from 'react';
import axios from 'axios';

const API = '/api/v1';

const STATUS_COLORS = { ACTIVE: 'bg-green-100 text-green-700', INACTIVE: 'bg-gray-100 text-gray-600', SUSPENDED: 'bg-orange-100 text-orange-700', PENDING: 'bg-amber-100 text-amber-700' };
const CIS_COLORS = { VERIFIED: 'bg-green-100 text-green-700', UNVERIFIED: 'bg-red-100 text-red-700', EXEMPT: 'bg-gray-100 text-gray-600', SUSPENDED: 'bg-orange-100 text-orange-700' };
const TRADES = ['GROUNDWORKS', 'MECHANICAL', 'ELECTRICAL', 'BRICKWORK', 'CARPENTRY', 'PAINTING', 'PLUMBING', 'SCAFFOLDING', 'WASTE'];

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
  const colors = { green: 'bg-green-100 text-green-700', red: 'bg-red-100 text-red-700', amber: 'bg-amber-100 text-amber-700', gray: 'bg-gray-100 text-gray-600', orange: 'bg-orange-100 text-orange-700', blue: 'bg-blue-100 text-blue-700' };
  return <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${colors[color] || colors.gray}`}>{children}</span>;
}

function formatPostcode(val) {
  if (!val) return '';
  const cleaned = val.replace(/[^a-zA-Z0-9]/g, '').toUpperCase();
  if (cleaned.length <= 3) return cleaned;
  if (cleaned.length <= 4) return `${cleaned.slice(0, 2)} ${cleaned.slice(2)}`;
  return `${cleaned.slice(0, cleaned.length - 3)} ${cleaned.slice(-3)}`;
}

function formatSortCode(val) {
  if (!val) return '';
  const cleaned = val.replace(/[^0-9]/g, '');
  if (cleaned.length <= 2) return cleaned;
  if (cleaned.length <= 4) return `${cleaned.slice(0, 2)}-${cleaned.slice(2)}`;
  return `${cleaned.slice(0, 2)}-${cleaned.slice(2, 4)}-${cleaned.slice(4, 6)}`;
}

function SubcontractorForm({ subcontractor, onSubmit, onClose }) {
  const [form, setForm] = useState(subcontractor || {
    companyRef: '', companyName: '', trade: 'GROUNDWORKS', addressLine1: '', addressLine2: '', city: '', postcode: '',
    contactName: '', contactEmail: '', contactPhone: '', status: 'ACTIVE', cisStatus: 'UNVERIFIED',
    uniqueTaxRef: '', niNumber: '', verificationRef: '', paymentTermsDays: '30',
    bankName: '', bankSortCode: '', bankAccountNumber: '', bankAccountName: ''
  });
  const [submitting, setSubmitting] = useState(false);

  const fc = (f) => (e) => setForm({ ...form, [f]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await onSubmit(form);
      onClose();
    } catch (err) { alert(err.response?.data?.message || 'Failed to save subcontractor'); }
    finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Company Ref *"><Input value={form.companyRef} onChange={fc('companyRef')} required placeholder="e.g. SUB-001" /></Field>
        <Field label="Status">
          <Select value={form.status} onChange={fc('status')}>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="SUSPENDED">Suspended</option>
            <option value="PENDING">Pending</option>
          </Select>
        </Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Company Name *"><Input value={form.companyName} onChange={fc('companyName')} required placeholder="Company name" /></Field>
        <Field label="Trade">
          <Select value={form.trade} onChange={fc('trade')}>
            {TRADES.map(t => <option key={t} value={t}>{t}</option>)}
          </Select>
        </Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="CIS Status">
          <Select value={form.cisStatus} onChange={fc('cisStatus')}>
            <option value="UNVERIFIED">Unverified</option>
            <option value="VERIFIED">Verified</option>
            <option value="SUSPENDED">Suspended</option>
            <option value="EXEMPT">Exempt</option>
          </Select>
        </Field>
        <Field label="UTR"><Input value={form.uniqueTaxRef} onChange={fc('uniqueTaxRef')} placeholder="1234567890" /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="NI Number"><Input value={form.niNumber} onChange={fc('niNumber')} placeholder="AB123456C" /></Field>
        <Field label="Verification Ref"><Input value={form.verificationRef} onChange={fc('verificationRef')} placeholder="VRN/123456" /></Field>
      </div>
      <div className="border-t border-gray-200 pt-4">
        <h4 className="text-sm font-semibold text-slate-600 mb-3">Address</h4>
        <div className="grid grid-cols-2 gap-4">
          <Field label="Address Line 1"><Input value={form.addressLine1} onChange={fc('addressLine1')} placeholder="Street address" /></Field>
          <Field label="Address Line 2"><Input value={form.addressLine2} onChange={fc('addressLine2')} placeholder="Building, suite" /></Field>
        </div>
        <div className="grid grid-cols-2 gap-4 mt-3">
          <Field label="City"><Input value={form.city} onChange={fc('city')} placeholder="City" /></Field>
          <Field label="Postcode"><Input value={form.postcode} onChange={(e) => setForm({ ...form, postcode: formatPostcode(e.target.value) })} placeholder="SW1A 1AA" maxLength={8} /></Field>
        </div>
      </div>
      <div className="border-t border-gray-200 pt-4">
        <h4 className="text-sm font-semibold text-slate-600 mb-3">Contact Details</h4>
        <div className="grid grid-cols-2 gap-4">
          <Field label="Contact Name"><Input value={form.contactName} onChange={fc('contactName')} placeholder="Full name" /></Field>
          <Field label="Contact Email"><Input type="email" value={form.contactEmail} onChange={fc('contactEmail')} placeholder="email@example.com" /></Field>
        </div>
        <div className="grid grid-cols-2 gap-4 mt-3">
          <Field label="Contact Phone"><Input value={form.contactPhone} onChange={fc('contactPhone')} placeholder="+44 1234 567890" /></Field>
          <Field label="Payment Terms (days)"><Input type="number" value={form.paymentTermsDays} onChange={fc('paymentTermsDays')} placeholder="30" /></Field>
        </div>
      </div>
      <div className="border-t border-gray-200 pt-4">
        <h4 className="text-sm font-semibold text-slate-600 mb-3">Bank Details</h4>
        <Field label="Bank Name"><Input value={form.bankName} onChange={fc('bankName')} placeholder="Bank name" /></Field>
        <div className="grid grid-cols-2 gap-4 mt-3">
          <Field label="Sort Code"><Input value={form.bankSortCode} onChange={(e) => setForm({ ...form, bankSortCode: formatSortCode(e.target.value) })} placeholder="12-34-56" maxLength={8} /></Field>
          <Field label="Account Number"><Input value={form.bankAccountNumber} onChange={fc('bankAccountNumber')} placeholder="12345678" maxLength={8} /></Field>
        </div>
        <Field label="Account Name" className="mt-3"><Input value={form.bankAccountName} onChange={fc('bankAccountName')} placeholder="Account holder name" /></Field>
      </div>
      <div className="flex justify-end gap-3 pt-4">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
          {submitting ? 'Saving…' : subcontractor ? 'Update Subcontractor' : 'Create Subcontractor'}
        </button>
      </div>
    </form>
  );
}

function CisVerificationForm({ subcontractorId, onClose, onAdded }) {
  const [form, setForm] = useState({
    subcontractorId: subcontractorId,
    verificationRef: '',
    status: 'VERIFIED',
    verificationDate: new Date().toISOString().split('T')[0],
    remarks: ''
  });
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await axios.post(`${API}/cis-verifications`, form);
      onAdded();
      onClose();
    } catch (err) { alert(err.response?.data?.message || 'Failed to create verification'); }
    finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Verification Ref *"><Input value={form.verificationRef} onChange={(e) => setForm({ ...form, verificationRef: e.target.value })} required placeholder="VRN/123456" /></Field>
        <Field label="Status">
          <Select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
            <option value="VERIFIED">Verified</option>
            <option value="UNVERIFIED">Unverified</option>
            <option value="SUSPENDED">Suspended</option>
            <option value="EXEMPT">Exempt</option>
          </Select>
        </Field>
      </div>
      <Field label="Verification Date"><Input type="date" value={form.verificationDate} onChange={(e) => setForm({ ...form, verificationDate: e.target.value })} /></Field>
      <Field label="Remarks"><Textarea value={form.remarks} onChange={(e) => setForm({ ...form, remarks: e.target.value })} placeholder="Additional notes…" rows={3} /></Field>
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
          {submitting ? 'Saving…' : 'Create Verification'}
        </button>
      </div>
    </form>
  );
}

function SubcontractorDetail({ subcontractor, onClose, onEdit, onDelete, refresh }) {
  const [tab, setTab] = useState('overview');
  const [verifications, setVerifications] = useState([]);
  const [cisReturns, setCisReturns] = useState([]);
  const [loadingVerifications, setLoadingVerifications] = useState(false);
  const [loadingReturns, setLoadingReturns] = useState(false);
  const [showVerificationForm, setShowVerificationForm] = useState(false);

  const loadVerifications = async () => {
    setLoadingVerifications(true);
    try {
      const res = await axios.get(`${API}/cis-verifications`, { params: { subcontractorId: subcontractor.id } });
      setVerifications(res.data.data || res.data || []);
    } catch { setVerifications([]); }
    finally { setLoadingVerifications(false); }
  };

  const loadCisReturns = async () => {
    setLoadingReturns(true);
    try {
      const res = await axios.get(`${API}/cis-returns`, { params: { page: 0, size: 20 } });
      const data = res.data.data || res.data;
      setCisReturns(data.content || data || []);
    } catch { setCisReturns([]); }
    finally { setLoadingReturns(false); }
  };

  useEffect(() => {
    if (tab === 'verifications') loadVerifications();
    if (tab === 'returns') loadCisReturns();
  }, [tab]);

  const infoRow = (label, value) => value ? (
    <div className="flex justify-between py-2 border-b border-gray-50 last:border-0">
      <span className="text-sm text-slate-500">{label}</span>
      <span className="text-sm text-slate-800 font-medium text-right max-w-[60%]">{value}</span>
    </div>
  ) : null;

  const address = [subcontractor.addressLine1, subcontractor.addressLine2, subcontractor.city, subcontractor.postcode].filter(Boolean).join(', ');

  return (
    <Modal title={`Subcontractor: ${subcontractor.companyName}`} onClose={onClose} size="max-w-3xl">
      <div className="flex items-center gap-4 mb-6 pb-4 border-b border-gray-200">
        <div className="w-14 h-14 rounded-full bg-blue-100 flex items-center justify-center text-xl font-bold text-blue-700">
          {subcontractor.companyName?.[0] || 'S'}
        </div>
        <div>
          <h2 className="text-xl font-bold text-slate-800">{subcontractor.companyName}</h2>
          <p className="text-sm text-slate-500">Ref: {subcontractor.companyRef} · {subcontractor.trade}</p>
        </div>
        <div className="ml-auto flex gap-2">
          <button onClick={() => { onEdit(); onClose(); }} className="px-3 py-1.5 text-xs border rounded-lg hover:bg-gray-50">Edit</button>
          <button onClick={() => { if (confirm('Delete this subcontractor?')) { onDelete(subcontractor.id); onClose(); } }} className="px-3 py-1.5 text-xs border border-red-200 text-red-600 rounded-lg hover:bg-red-50">Delete</button>
        </div>
      </div>

      <div className="flex gap-4 mb-4">
        <span className={`inline-flex px-3 py-1 rounded-full text-xs font-medium ${STATUS_COLORS[subcontractor.status] || 'bg-gray-100 text-gray-600'}`}>{subcontractor.status}</span>
        <span className={`inline-flex px-3 py-1 rounded-full text-xs font-medium ${CIS_COLORS[subcontractor.cisStatus] || 'bg-gray-100 text-gray-600'}`}>CIS: {subcontractor.cisStatus}</span>
      </div>

      <div className="flex gap-1 mb-5 bg-slate-100 p-1 rounded-lg w-fit">
        {['overview', 'verifications', 'returns'].map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-4 py-1.5 text-xs font-medium rounded-md transition-colors ${tab === t ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}>
            {t.charAt(0).toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {tab === 'overview' && (
        <div className="space-y-1">
          {infoRow('Company Ref', subcontractor.companyRef)}
          {infoRow('Trade', subcontractor.trade)}
          {infoRow('Status', subcontractor.status)}
          {infoRow('CIS Status', subcontractor.cisStatus)}
          {infoRow('UTR', subcontractor.uniqueTaxRef)}
          {infoRow('NI Number', subcontractor.niNumber)}
          {infoRow('Verification Ref', subcontractor.verificationRef)}
          {infoRow('Last Verification Date', subcontractor.lastVerificationDate)}
          {infoRow('Contractor Ref', subcontractor.contractorRef)}
          {address && infoRow('Address', address)}
          {infoRow('Contact Name', subcontractor.contactName)}
          {infoRow('Contact Email', subcontractor.contactEmail)}
          {infoRow('Contact Phone', subcontractor.contactPhone)}
          {infoRow('Payment Terms', subcontractor.paymentTermsDays ? `${subcontractor.paymentTermsDays} days` : '')}
          {infoRow('Bank Name', subcontractor.bankName)}
          {infoRow('Sort Code', subcontractor.bankSortCode)}
          {infoRow('Account Number', subcontractor.bankAccountNumber)}
          {infoRow('Account Name', subcontractor.bankAccountName)}
        </div>
      )}

      {tab === 'verifications' && (
        <div>
          <div className="flex justify-between mb-4">
            <p className="text-sm text-slate-500">{verifications.length} verification(s)</p>
            <button onClick={() => setShowVerificationForm(true)} className="px-3 py-1.5 text-xs bg-blue-600 text-white rounded-lg hover:bg-blue-700">+ Add Verification</button>
          </div>
          {showVerificationForm ? (
            <div className="mb-4 p-4 bg-slate-50 rounded-lg">
              <h4 className="text-sm font-semibold text-slate-600 mb-3">New CIS Verification</h4>
              <CisVerificationForm subcontractorId={subcontractor.id} onClose={() => setShowVerificationForm(false)} onAdded={loadVerifications} />
            </div>
          ) : null}
          {loadingVerifications ? <p className="text-sm text-slate-400">Loading…</p> : verifications.length > 0 ? (
            <div className="space-y-3">
              {verifications.map(v => (
                <div key={v.id} className="rounded-xl p-4 border border-gray-200 bg-gray-50">
                  <div className="flex justify-between items-start">
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="font-semibold text-slate-800">{v.verificationRef || 'N/A'}</span>
                        <span className={`inline-flex px-2 py-0.5 rounded text-xs font-medium ${CIS_COLORS[v.status] || 'bg-gray-100 text-gray-600'}`}>{v.status}</span>
                      </div>
                      {v.verificationDate && <p className="text-sm text-slate-600 mt-1">Date: {v.verificationDate}</p>}
                      {v.remarks && <p className="text-xs text-slate-400 mt-1">{v.remarks}</p>}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          ) : <p className="text-sm text-slate-400">No verifications found</p>}
        </div>
      )}

      {tab === 'returns' && (
        <div>
          <div className="flex justify-between mb-4">
            <p className="text-sm text-slate-500">{cisReturns.length} CIS return(s)</p>
            <button onClick={loadCisReturns} className="px-3 py-1.5 text-xs border rounded-lg hover:bg-gray-50">Refresh</button>
          </div>
          {loadingReturns ? <p className="text-sm text-slate-400">Loading…</p> : cisReturns.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-200">
                    <th className="text-left py-2 px-3 text-slate-500 font-medium">Month</th>
                    <th className="text-left py-2 px-3 text-slate-500 font-medium">Year</th>
                    <th className="text-left py-2 px-3 text-slate-500 font-medium">Contractor Ref</th>
                    <th className="text-left py-2 px-3 text-slate-500 font-medium">Submitted Date</th>
                    <th className="text-left py-2 px-3 text-slate-500 font-medium">Submitted By</th>
                  </tr>
                </thead>
                <tbody>
                  {cisReturns.map(r => (
                    <tr key={r.id} className="border-b border-gray-100 hover:bg-gray-50">
                      <td className="py-2 px-3">{r.month || '-'}</td>
                      <td className="py-2 px-3">{r.year || '-'}</td>
                      <td className="py-2 px-3">{r.contractorRef || '-'}</td>
                      <td className="py-2 px-3">{r.submittedDate || '-'}</td>
                      <td className="py-2 px-3">{r.submittedBy || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : <p className="text-sm text-slate-400">No CIS returns found</p>}
        </div>
      )}
    </Modal>
  );
}

export default function Subcontractors() {
  const [subcontractors, setSubcontractors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [tradeFilter, setTradeFilter] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [detailModal, setDetailModal] = useState(null);
  const [editSub, setEditSub] = useState(null);

  const fetchSubcontractors = useCallback(async (p = 0, s = search, st = statusFilter, tr = tradeFilter) => {
    setLoading(true);
    try {
      const params = { page: p, size: 20 };
      if (s) params.search = s;
      if (st) params.status = st;
      const { data } = await axios.get(`${API}/subcontractors`, { params });
      const pl = data.data || data;
      setSubcontractors(pl.content || pl || []);
      setTotalPages(pl.totalPages || 1);
      setError(null);
    } catch { setError('Failed to load subcontractors'); }
    finally { setLoading(false); }
  }, [search, statusFilter]);

  useEffect(() => { fetchSubcontractors(page); }, [fetchSubcontractors, page]);

  const handleCreate = async (form) => {
    await axios.post(`${API}/subcontractors`, form);
    fetchSubcontractors(page);
  };

  const handleUpdate = async (form) => {
    await axios.patch(`${API}/subcontractors/${editSub.id}`, form);
    fetchSubcontractors(page);
  };

  const handleDelete = async (id) => {
    await axios.delete(`${API}/subcontractors/${id}`);
    fetchSubcontractors(page);
  };

  const debounce = (fn, ms = 400) => {
    let t;
    return (...args) => {
      clearTimeout(t);
      t = setTimeout(() => fn(...args), ms);
    };
  };

  const debouncedSearch = debounce((s) => fetchSubcontractors(0, s, statusFilter, tradeFilter), 400);

  const stats = {
    total: subcontractors.length,
    active: subcontractors.filter(s => s.status === 'ACTIVE').length,
    suspended: subcontractors.filter(s => s.status === 'SUSPENDED').length,
    pending: subcontractors.filter(s => s.cisStatus === 'UNVERIFIED' || s.status === 'PENDING').length
  };

  const statsCards = [
    { label: 'Total Subcontractors', value: stats.total, icon: '🏗️', color: 'bg-blue-50' },
    { label: 'Active', value: stats.active, icon: '✅', color: 'bg-green-50' },
    { label: 'Suspended', value: stats.suspended, icon: '⛔', color: 'bg-orange-50' },
    { label: 'Pending Verification', value: stats.pending, icon: '⏳', color: 'bg-amber-50' }
  ];

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold text-slate-800">Subcontractors</h2>
          <p className="text-sm text-slate-500 mt-1">Manage subcontractors and CIS compliance</p>
        </div>
        <button onClick={() => { setEditSub(null); setShowModal(true); }} className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 flex items-center gap-2">
          + New Subcontractor
        </button>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
        {statsCards.map((card) => (
          <div key={card.label} className={`${card.color} rounded-xl p-4`}>
            <div className="flex items-center gap-2 mb-1">
              <span className="text-lg">{card.icon}</span>
              <span className="text-sm text-slate-600">{card.label}</span>
            </div>
            <p className="text-2xl font-bold text-slate-800">{card.value}</p>
          </div>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 mb-6">
        <form onSubmit={(e) => { e.preventDefault(); setPage(0); fetchSubcontractors(0, search, statusFilter, tradeFilter); }} className="flex gap-3 flex-wrap">
          <input type="text" value={search} onChange={(e) => { setSearch(e.target.value); debouncedSearch(e.target.value); }} placeholder="Search subcontractors…" className="flex-1 min-w-[200px] px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
          <select value={statusFilter} onChange={(e) => { setStatusFilter(e.target.value); }} className="px-4 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">All Status</option>
            <option value="ACTIVE">Active</option>
            <option value="INACTIVE">Inactive</option>
            <option value="SUSPENDED">Suspended</option>
            <option value="PENDING">Pending</option>
          </select>
          <select value={tradeFilter} onChange={(e) => { setTradeFilter(e.target.value); }} className="px-4 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option value="">All Trades</option>
            {TRADES.map(t => <option key={t} value={t}>{t}</option>)}
          </select>
          <button type="submit" className="px-4 py-2 bg-slate-800 text-white text-sm rounded-lg">Search</button>
          {(search || statusFilter || tradeFilter) && (
            <button type="button" onClick={() => { setSearch(''); setStatusFilter(''); setTradeFilter(''); fetchSubcontractors(0, '', '', ''); }} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Clear</button>
          )}
        </form>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {loading ? <div className="p-8 text-center text-slate-400">Loading…</div>
          : error ? <div className="p-8 text-center text-red-500">{error}</div>
          : subcontractors.length === 0 ? <div className="p-8 text-center text-slate-400">No subcontractors found</div>
          : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-gray-100 bg-slate-50">
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">Company Ref</th>
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">Company Name</th>
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">Trade</th>
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">Status</th>
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">CIS Status</th>
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">UTR</th>
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">Verification Ref</th>
                    <th className="text-left py-3 px-4 text-slate-500 font-medium">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {subcontractors.map((sub) => (
                    <tr key={sub.id} className="border-b border-gray-50 hover:bg-gray-50 cursor-pointer" onClick={() => setDetailModal(sub)}>
                      <td className="py-3 px-4 font-medium text-slate-800">{sub.companyRef || '-'}</td>
                      <td className="py-3 px-4 text-slate-700">{sub.companyName || '-'}</td>
                      <td className="py-3 px-4 text-slate-600">{sub.trade || '-'}</td>
                      <td className="py-3 px-4">
                        <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[sub.status] || 'bg-gray-100 text-gray-600'}`}>{sub.status || '-'}</span>
                      </td>
                      <td className="py-3 px-4">
                        <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${CIS_COLORS[sub.cisStatus] || 'bg-gray-100 text-gray-600'}`}>{sub.cisStatus || '-'}</span>
                      </td>
                      <td className="py-3 px-4 text-slate-600 font-mono text-xs">{sub.uniqueTaxRef || '-'}</td>
                      <td className="py-3 px-4 text-slate-600 font-mono text-xs">{sub.verificationRef || '-'}</td>
                      <td className="py-3 px-4">
                        <button onClick={(e) => { e.stopPropagation(); setDetailModal(sub); }} className="px-3 py-1 text-xs border rounded-lg hover:bg-gray-100">View</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-between mt-4">
          <p className="text-xs text-slate-500">Page {page + 1} of {totalPages}</p>
          <div className="flex gap-2">
            <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0} className="px-3 py-1 text-xs border rounded-lg disabled:opacity-40">Previous</button>
            <button onClick={() => setPage(Math.min(totalPages - 1, page + 1))} disabled={page >= totalPages - 1} className="px-3 py-1 text-xs border rounded-lg disabled:opacity-40">Next</button>
          </div>
        </div>
      )}

      {showModal && (
        <Modal title={editSub ? 'Edit Subcontractor' : 'New Subcontractor'} onClose={() => setShowModal(false)} size="max-w-3xl">
          <SubcontractorForm subcontractor={editSub} onSubmit={editSub ? handleUpdate : handleCreate} onClose={() => setShowModal(false)} />
        </Modal>
      )}

      {detailModal && (
        <SubcontractorDetail
          subcontractor={detailModal}
          onClose={() => setDetailModal(null)}
          onEdit={() => { setEditSub(detailModal); setShowModal(true); }}
          onDelete={handleDelete}
          refresh={() => fetchSubcontractors(page)}
        />
      )}
    </div>
  );
}
