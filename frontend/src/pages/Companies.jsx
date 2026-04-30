import { useEffect, useState, useCallback } from 'react';
import axios from 'axios';

const API = '/api/v1';

const TYPE_COLORS = { CLIENT: 'bg-blue-50 text-blue-700', CONTRACTOR: 'bg-purple-50 text-purple-700', SUBCONTRACTOR: 'bg-amber-50 text-amber-700', SUPPLIER: 'bg-green-50 text-green-700', CONSULTANT: 'bg-slate-50 text-slate-700' };

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

export default function Companies() {
  const [companies, setCompanies] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [detailModal, setDetailModal] = useState(null);
  const [form, setForm] = useState({ name: '', registrationNumber: '', type: 'CONTRACTOR', email: '', phone: '', website: '', address: '' });

  const fetch = useCallback(async (p = 0, q = search) => {
    setLoading(true);
    try {
      const { data } = await axios.get(`${API}/companies`, { params: { page: p, size: 15, search: q } });
      const pl = data.data || data;
      setCompanies(pl.content || pl || []);
      setTotalPages(pl.totalPages || 1);
      setError(null);
    } catch { setError('Failed to load companies'); }
    finally { setLoading(false); }
  }, [search]);

  useEffect(() => { fetch(page); }, [fetch, page]);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`${API}/companies`, form);
      setShowModal(false);
      fetch(page);
    } catch (err) { alert(err.response?.data?.message || 'Failed to create company'); }
  };

  const fc = (f) => (e) => setForm({ ...form, [f]: e.target.value });

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div><h2 className="text-2xl font-bold text-slate-800">Companies</h2><p className="text-sm text-slate-500 mt-1">{companies.length > 0 ? `${companies.length} companies shown` : 'No data'}</p></div>
        <button onClick={() => { setForm({ name: '', registrationNumber: '', type: 'CONTRACTOR', email: '', phone: '', website: '', address: '' }); setShowModal(true); }} className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700">+ New Company</button>
      </div>

      <form onSubmit={(e) => { e.preventDefault(); setPage(0); fetch(0, search); }} className="mb-6 flex gap-3">
        <input type="text" value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search companies…" className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        <button type="submit" className="px-4 py-2 bg-slate-800 text-white text-sm rounded-lg">Search</button>
      </form>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
        {loading ? <div className="col-span-full p-8 text-center text-slate-400">Loading…</div>
          : error ? <div className="col-span-full p-8 text-center text-red-500">{error}</div>
          : companies.length === 0 ? <div className="col-span-full p-8 text-center text-slate-400">No companies found</div>
          : companies.map((c) => (
            <div key={c.id} className="bg-white rounded-xl shadow-sm border border-gray-100 p-5 hover:shadow-md transition-shadow cursor-pointer" onClick={() => setDetailModal(c)}>
              <div className="flex items-start justify-between mb-2">
                <h3 className="font-semibold text-slate-800">{c.name || '—'}</h3>
                <span className={`inline-flex px-2 py-0.5 rounded text-xs font-medium ${TYPE_COLORS[c.type] || 'bg-gray-100 text-gray-600'}`}>{c.type || '—'}</span>
              </div>
              <p className="text-xs text-slate-400 mb-2">{c.registrationNumber || '—'}</p>
              <p className="text-sm text-slate-600">{c.email || c.contactEmail || '—'}</p>
              <p className="text-xs text-slate-400 mt-1">📞 {c.phone || c.telephone || '—'}</p>
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
        <Modal title="New Company" onClose={() => setShowModal(false)}>
          <form onSubmit={handleCreate} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Company Name *</label><input value={form.name} onChange={fc('name')} required placeholder="Company name" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Registration No.</label><input value={form.registrationNumber} onChange={fc('registrationNumber')} placeholder="12345678" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
            </div>
            <div><label className="block text-sm font-medium text-slate-700 mb-1">Type</label><select value={form.type} onChange={fc('type')} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"><option value="CLIENT">Client</option><option value="CONTRACTOR">Contractor</option><option value="SUBCONTRACTOR">Subcontractor</option><option value="SUPPLIER">Supplier</option><option value="CONSULTANT">Consultant</option></select></div>
            <div className="grid grid-cols-2 gap-4">
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Email</label><input type="email" value={form.email} onChange={fc('email')} placeholder="info@company.com" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
              <div><label className="block text-sm font-medium text-slate-700 mb-1">Phone</label><input value={form.phone} onChange={fc('phone')} placeholder="+44..." className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" /></div>
            </div>
            <div><label className="block text-sm font-medium text-slate-700 mb-1">Address</label><textarea value={form.address} onChange={fc('address')} rows={2} placeholder="Registered address…" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none" /></div>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
              <button type="submit" className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700">Create</button>
            </div>
          </form>
        </Modal>
      )}

      {detailModal && (
        <Modal title="Company Details" onClose={() => setDetailModal(null)}>
          <div className="space-y-3 text-sm">
            <div className="flex justify-between"><div><p className="text-slate-500">Name</p><p className="font-semibold">{detailModal.name || '—'}</p></div><span className={`inline-flex px-2 py-0.5 rounded text-xs font-medium ${TYPE_COLORS[detailModal.type] || ''}`}>{detailModal.type || '—'}</span></div>
            <div><p className="text-slate-500">Registration</p><p className="text-slate-800">{detailModal.registrationNumber || '—'}</p></div>
            <div className="grid grid-cols-2 gap-4"><div><p className="text-slate-500">Email</p><p className="text-slate-800">{detailModal.email || '—'}</p></div><div><p className="text-slate-500">Phone</p><p className="text-slate-800">{detailModal.phone || '—'}</p></div></div>
            {detailModal.address && <div><p className="text-slate-500">Address</p><p className="text-slate-800">{detailModal.address}</p></div>}
          </div>
          <div className="flex justify-end mt-6"><button onClick={() => setDetailModal(null)} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Close</button></div>
        </Modal>
      )}
    </div>
  );
}
