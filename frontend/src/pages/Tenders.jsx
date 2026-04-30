import { useEffect, useState, useCallback } from 'react';
import axios from 'axios';

const API = '/api/v1';

const STATUS_COLORS = {
  OPEN: 'bg-blue-100 text-blue-700',
  CLOSED: 'bg-gray-100 text-gray-600',
  AWARDED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
  DRAFT: 'bg-slate-100 text-slate-600',
  SUBMITTED: 'bg-amber-100 text-amber-700',
};

function StatusBadge({ status }) {
  return (
    <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[status] || 'bg-gray-100 text-gray-600'}`}>
      {status || '—'}
    </span>
  );
}

function Modal({ title, onClose, children }) {
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto shadow-2xl">
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200 sticky top-0 bg-white rounded-t-xl">
          <h3 className="text-lg font-semibold text-slate-800">{title}</h3>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 text-2xl leading-none">&times;</button>
        </div>
        <div className="p-6">{children}</div>
      </div>
    </div>
  );
}

export default function Tenders() {
  const [tenders, setTenders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [detailModal, setDetailModal] = useState(null);
  const [form, setForm] = useState({ title: '', reference: '', client: '', submissionDeadline: '', estimatedValue: '', status: 'DRAFT', probability: '' });

  const fetch = useCallback(async (p = 0, q = search) => {
    setLoading(true);
    try {
      const { data } = await axios.get(`${API}/tenders`, { params: { page: p, size: 15, search: q } });
      const pl = data.data || data;
      setTenders(pl.content || pl || []);
      setTotalPages(pl.totalPages || 1);
      setError(null);
    } catch { setError('Failed to load tenders'); }
    finally { setLoading(false); }
  }, [search]);

  useEffect(() => { fetch(page); }, [fetch, page]);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`${API}/tenders`, { ...form, estimatedValue: parseFloat(form.estimatedValue) || null, probability: parseInt(form.probability) || null });
      setShowModal(false);
      fetch(page);
    } catch (err) { alert(err.response?.data?.message || 'Failed to create tender'); }
  };

  const fc = (f) => (e) => setForm({ ...form, [f]: e.target.value });

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div><h2 className="text-2xl font-bold text-slate-800">Tenders</h2><p className="text-sm text-slate-500 mt-1">{tenders.length > 0 ? `${tenders.length} tenders shown` : 'No data'}</p></div>
        <button onClick={() => { setForm({ title: '', reference: '', client: '', submissionDeadline: '', estimatedValue: '', status: 'DRAFT', probability: '' }); setShowModal(true); }} className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700">+ New Tender</button>
      </div>

      <form onSubmit={(e) => { e.preventDefault(); setPage(0); fetch(0, search); }} className="mb-6 flex gap-3">
        <input type="text" value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search tenders…" className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        <button type="submit" className="px-4 py-2 bg-slate-800 text-white text-sm rounded-lg">Search</button>
      </form>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {loading ? <div className="p-8 text-center text-slate-400">Loading…</div>
          : error ? <div className="p-8 text-center text-red-500">{error}</div>
          : tenders.length === 0 ? <div className="p-8 text-center text-slate-400">No tenders found</div>
          : (
            <table className="w-full">
              <thead>
                <tr className="bg-slate-50 border-b border-gray-100">
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Ref</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Title</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Client</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Value</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Deadline</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Status</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {tenders.map((t) => (
                  <tr key={t.id} className="hover:bg-slate-50/50 transition-colors">
                    <td className="px-5 py-4 text-sm font-mono text-slate-600">{t.reference || '—'}</td>
                    <td className="px-5 py-4 text-sm font-medium text-slate-800">{t.title || '—'}</td>
                    <td className="px-5 py-4 text-sm text-slate-600">{t.client || t.clientName || t.company?.name || '—'}</td>
                    <td className="px-5 py-4 text-sm text-slate-600">{t.estimatedValue != null ? `£${parseFloat(t.estimatedValue).toLocaleString()}` : '—'}</td>
                    <td className="px-5 py-4 text-sm text-slate-600">{t.submissionDeadline || t.deadline || '—'}</td>
                    <td className="px-5 py-4"><StatusBadge status={t.status} /></td>
                    <td className="px-5 py-4">
                      <button onClick={() => setDetailModal(t)} className="text-xs text-blue-600 hover:text-blue-800 font-medium">View</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-between mt-6">
          <p className="text-xs text-slate-500">Page {page + 1} of {totalPages}</p>
          <div className="flex gap-2">
            <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0} className="px-3 py-1 text-xs border rounded-lg disabled:opacity-40">Previous</button>
            <button onClick={() => setPage(Math.min(totalPages - 1, page + 1))} disabled={page >= totalPages - 1} className="px-3 py-1 text-xs border rounded-lg disabled:opacity-40">Next</button>
          </div>
        </div>
      )}

      {showModal && (
        <Modal title="New Tender" onClose={() => setShowModal(false)}>
          <form onSubmit={handleCreate} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Tender Reference *</label><input value={form.reference} onChange={fc('reference')} required placeholder="TND-001" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Title *</label><input value={form.title} onChange={fc('title')} required placeholder="Tender title" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
            </div>
            <div><label className="block text-sm font-medium text-slate-700 mb-1">Client</label><input value={form.client} onChange={fc('client')} placeholder="Client name" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
            <div className="grid grid-cols-2 gap-4">
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Estimated Value (£)</label><input type="number" step="0.01" value={form.estimatedValue} onChange={fc('estimatedValue')} placeholder="0.00" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Submission Deadline</label><input type="date" value={form.submissionDeadline} onChange={fc('submissionDeadline')} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Status</label><select value={form.status} onChange={fc('status')} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"><option value="DRAFT">Draft</option><option value="OPEN">Open</option><option value="SUBMITTED">Submitted</option><option value="CLOSED">Closed</option><option value="AWARDED">Awarded</option><option value="CANCELLED">Cancelled</option></select></div>
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Probability (%)</label><input type="number" min="0" max="100" value={form.probability} onChange={fc('probability')} placeholder="50" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
            </div>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
              <button type="submit" className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700">Create Tender</button>
            </div>
          </form>
        </Modal>
      )}

      {detailModal && (
        <Modal title="Tender Details" onClose={() => setDetailModal(null)}>
          <div className="space-y-3 text-sm">
            <div className="grid grid-cols-2 gap-4">
              <div><p className="text-slate-500">Reference</p><p className="font-medium text-slate-800">{detailModal.reference || '—'}</p></div>
              <div><p className="text-slate-500">Status</p><p className="mt-0.5"><StatusBadge status={detailModal.status} /></p></div>
            </div>
            <div><p className="text-slate-500">Title</p><p className="font-medium text-slate-800">{detailModal.title || '—'}</p></div>
            <div><p className="text-slate-500">Client</p><p className="text-slate-800">{detailModal.client || detailModal.clientName || detailModal.company?.name || '—'}</p></div>
            <div className="grid grid-cols-2 gap-4">
              <div><p className="text-slate-500">Estimated Value</p><p className="text-slate-800">{detailModal.estimatedValue != null ? `£${parseFloat(detailModal.estimatedValue).toLocaleString()}` : '—'}</p></div>
              <div><p className="text-slate-500">Probability</p><p className="text-slate-800">{detailModal.probability ? `${detailModal.probability}%` : '—'}</p></div>
            </div>
            <div><p className="text-slate-500">Submission Deadline</p><p className="text-slate-800">{detailModal.submissionDeadline || detailModal.deadline || '—'}</p></div>
          </div>
          <div className="flex justify-end mt-6"><button onClick={() => setDetailModal(null)} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Close</button></div>
        </Modal>
      )}
    </div>
  );
}
