import { useEffect, useState, useCallback } from 'react';
import axios from 'axios';

const API = '/api/v1';

const STATUS_COLORS = {
  ACTIVE: 'bg-green-100 text-green-700',
  INACTIVE: 'bg-gray-100 text-gray-600',
  CLOSED: 'bg-red-100 text-red-700',
  DORMANT: 'bg-amber-100 text-amber-700',
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

function Field({ label, children }) {
  return (
    <div>
      <p className="text-xs font-medium text-slate-500 uppercase tracking-wide mb-1">{label}</p>
      {children}
    </div>
  );
}

function Input({ className = '', ...props }) {
  return <input {...props} className={`w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${className}`} />;
}

function Textarea({ ...props }) {
  return <textarea {...props} rows={2} className={`w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none`} />;
}

export default function Sites() {
  const [sites, setSites] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [detailModal, setDetailModal] = useState(null);
  const [form, setForm] = useState({ siteName: '', address: '', postCode: '', status: 'ACTIVE', contactName: '', contactPhone: '', description: '' });

  const fetchSites = useCallback(async (pageNum = 0, q = search) => {
    setLoading(true);
    try {
      const { data } = await axios.get(`${API}/sites`, { params: { page: pageNum, size: 15, search: q } });
      const p = data.data || data;
      setSites(p.content || p || []);
      setTotalPages(p.totalPages || 1);
      setTotalElements(p.totalElements || 0);
      setError(null);
    } catch { setError('Failed to load sites'); }
    finally { setLoading(false); }
  }, [search]);

  useEffect(() => { fetchSites(page); }, [fetchSites, page]);

  const handleSearch = (e) => { e.preventDefault(); setPage(0); fetchSites(0, search); };

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`${API}/sites`, form);
      setShowModal(false);
      fetchSites(page);
    } catch (err) { alert(err.response?.data?.message || 'Failed to create site'); }
  };

  const fc = (f) => (e) => setForm({ ...form, [f]: e.target.value });

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold text-slate-800">Sites</h2>
          <p className="text-sm text-slate-500 mt-1">{totalElements} total sites</p>
        </div>
        <button onClick={() => { setForm({ siteName: '', address: '', postCode: '', status: 'ACTIVE', contactName: '', contactPhone: '', description: '' }); setShowModal(true); }} className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700">+ New Site</button>
      </div>

      <form onSubmit={handleSearch} className="mb-6 flex gap-3">
        <input type="text" value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search sites…" className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        <button type="submit" className="px-4 py-2 bg-slate-800 text-white text-sm rounded-lg hover:bg-slate-700">Search</button>
      </form>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
        {loading ? <div className="col-span-full p-8 text-center text-slate-400">Loading…</div>
          : error ? <div className="col-span-full p-8 text-center text-red-500">{error}</div>
          : sites.length === 0 ? <div className="col-span-full p-8 text-center text-slate-400">No sites found</div>
          : sites.map((s) => (
            <div key={s.id} className="bg-white rounded-xl shadow-sm border border-gray-100 p-5 hover:shadow-md transition-shadow cursor-pointer" onClick={() => setDetailModal(s)}>
              <div className="flex items-start justify-between mb-3">
                <div>
                  <h3 className="font-semibold text-slate-800">{s.siteName || s.name || '—'}</h3>
                  <p className="text-xs text-slate-400 mt-0.5">{s.postCode || s.postalCode || '—'}</p>
                </div>
                <StatusBadge status={s.status} />
              </div>
              <p className="text-sm text-slate-600 line-clamp-2">{s.address || s.siteAddress || '—'}</p>
              {s.contactName && <p className="text-xs text-slate-400 mt-2">👤 {s.contactName} · {s.contactPhone || '—'}</p>}
            </div>
          ))}
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
        <Modal title="New Site" onClose={() => setShowModal(false)}>
          <form onSubmit={handleCreate} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Site Name *</label><Input value={form.siteName} onChange={fc('siteName')} required placeholder="Site name" /></div>
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Post Code</label><Input value={form.postCode} onChange={fc('postCode')} placeholder="XX00 0XX" /></div>
            </div>
            <div><label className="block text-sm font-medium text-slate-700 mb-1">Address</label><Textarea value={form.address} onChange={fc('address')} placeholder="Full address…" /></div>
            <div className="grid grid-cols-2 gap-4">
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Status</label><select value={form.status} onChange={fc('status')} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"><option value="ACTIVE">Active</option><option value="INACTIVE">Inactive</option><option value="CLOSED">Closed</option></select></div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Contact Name</label><Input value={form.contactName} onChange={fc('contactName')} placeholder="Site manager name" /></div>
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Contact Phone</label><Input value={form.contactPhone} onChange={fc('contactPhone')} placeholder="+44..." /></div>
            </div>
            <div><label className="block text-sm font-medium text-slate-700 mb-1">Description</label><Textarea value={form.description} onChange={fc('description')} placeholder="Site description…" /></div>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
              <button type="submit" className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700">Create Site</button>
            </div>
          </form>
        </Modal>
      )}

      {detailModal && (
        <Modal title="Site Details" onClose={() => setDetailModal(null)}>
          <div className="space-y-3 text-sm">
            <div className="flex justify-between items-start">
              <div><p className="text-slate-500">Name</p><p className="font-semibold text-slate-800">{detailModal.siteName || detailModal.name || '—'}</p></div>
              <StatusBadge status={detailModal.status} />
            </div>
            <div><p className="text-slate-500">Post Code</p><p className="text-slate-800">{detailModal.postCode || '—'}</p></div>
            <div><p className="text-slate-500">Address</p><p className="text-slate-800">{detailModal.address || '—'}</p></div>
            {detailModal.contactName && <div><p className="text-slate-500">Contact</p><p className="text-slate-800">{detailModal.contactName} · {detailModal.contactPhone || '—'}</p></div>}
            {detailModal.description && <div><p className="text-slate-500">Description</p><p className="text-slate-800">{detailModal.description}</p></div>}
          </div>
          <div className="flex justify-end mt-6"><button onClick={() => setDetailModal(null)} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Close</button></div>
        </Modal>
      )}
    </div>
  );
}
