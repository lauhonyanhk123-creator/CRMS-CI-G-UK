import { useEffect, useState, useCallback } from 'react';
import axios from 'axios';

const API = '/api/v1';

const STATUS_COLORS = {
  DRAFT: 'bg-gray-100 text-gray-700',
  ACTIVE: 'bg-green-100 text-green-700',
  COMPLETED: 'bg-blue-100 text-blue-700',
  TERMINATED: 'bg-red-100 text-red-700',
  SUSPENDED: 'bg-amber-100 text-amber-700',
  EXPIRED: 'bg-slate-100 text-slate-600',
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

function FormField({ label, children }) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-700 mb-1">{label}</label>
      {children}
    </div>
  );
}

function Input({ className = '', ...props }) {
  return (
    <input
      {...props}
      className={`w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${className}`}
    />
  );
}

function Select({ children, ...props }) {
  return (
    <select
      {...props}
      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
    >
      {children}
    </select>
  );
}

function Textarea({ children, ...props }) {
  return (
    <textarea
      {...props}
      rows={3}
      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none"
    />
  );
}

export default function Contracts() {
  const [contracts, setContracts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [detailModal, setDetailModal] = useState(null);
  const [deleting, setDeleting] = useState(null);

  const [form, setForm] = useState({
    title: '',
    contractRef: '',
    companyId: '',
    value: '',
    status: 'DRAFT',
    type: 'MAIN_CONTRACT',
    startDate: '',
    endDate: '',
    description: '',
  });

  const fetchContracts = useCallback(async (pageNum = 0, searchTerm = search) => {
    setLoading(true);
    try {
      const params = { page: pageNum, size: 15, search: searchTerm };
      const { data } = await axios.get(`${API}/contracts`, { params });
      const payload = data.data || data;
      setContracts(payload.content || payload || []);
      setTotalPages(payload.totalPages || 1);
      setTotalElements(payload.totalElements || 0);
      setError(null);
    } catch (err) {
      setError('Failed to load contracts');
    } finally {
      setLoading(false);
    }
  }, [search]);

  useEffect(() => { fetchContracts(page); }, [fetchContracts, page]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    fetchContracts(0, search);
  };

  const openCreate = () => {
    setForm({ title: '', contractRef: '', companyId: '', value: '', status: 'DRAFT', type: 'MAIN_CONTRACT', startDate: '', endDate: '', description: '' });
    setShowModal(true);
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`${API}/contracts`, { ...form, value: parseFloat(form.value) || 0 });
      setShowModal(false);
      fetchContracts(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create contract');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this contract?')) return;
    setDeleting(id);
    try {
      await axios.delete(`${API}/contracts/${id}`);
      fetchContracts(page);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete contract');
    } finally {
      setDeleting(null);
    }
  };

  const formChange = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-bold text-slate-800">Contracts</h2>
          <p className="text-sm text-slate-500 mt-1">{totalElements} total contracts</p>
        </div>
        <button
          onClick={openCreate}
          className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
        >
          + New Contract
        </button>
      </div>

      {/* Search */}
      <form onSubmit={handleSearch} className="mb-6 flex gap-3">
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search contracts…"
          className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <button type="submit" className="px-4 py-2 bg-slate-800 text-white text-sm rounded-lg hover:bg-slate-700">Search</button>
      </form>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-slate-400">Loading…</div>
        ) : error ? (
          <div className="p-8 text-center text-red-500">{error}</div>
        ) : contracts.length === 0 ? (
          <div className="p-8 text-center text-slate-400">No contracts found</div>
        ) : (
          <>
            <table className="w-full">
              <thead>
                <tr className="bg-slate-50 border-b border-gray-100">
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Reference</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Title</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Company</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Value</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Status</th>
                  <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-50">
                {contracts.map((c) => (
                  <tr key={c.id} className="hover:bg-slate-50/50 transition-colors">
                    <td className="px-5 py-4 text-sm font-mono text-slate-600">{c.contractRef || c.reference || '—'}</td>
                    <td className="px-5 py-4 text-sm font-medium text-slate-800">{c.title || '—'}</td>
                    <td className="px-5 py-4 text-sm text-slate-600">{c.company?.name || c.companyName || '—'}</td>
                    <td className="px-5 py-4 text-sm text-slate-600">{c.value != null ? `£${parseFloat(c.value).toLocaleString()}` : '—'}</td>
                    <td className="px-5 py-4"><StatusBadge status={c.status} /></td>
                    <td className="px-5 py-4">
                      <div className="flex items-center gap-3">
                        <button onClick={() => setDetailModal(c)} className="text-xs text-blue-600 hover:text-blue-800 font-medium">View</button>
                        <button onClick={() => handleDelete(c.id)} disabled={deleting === c.id} className="text-xs text-red-500 hover:text-red-700 font-medium disabled:opacity-40">Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between px-5 py-3 border-t border-gray-100">
                <p className="text-xs text-slate-500">Page {page + 1} of {totalPages}</p>
                <div className="flex gap-2">
                  <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0} className="px-3 py-1 text-xs border rounded-lg disabled:opacity-40 hover:bg-gray-50">Previous</button>
                  <button onClick={() => setPage(Math.min(totalPages - 1, page + 1))} disabled={page >= totalPages - 1} className="px-3 py-1 text-xs border rounded-lg disabled:opacity-40 hover:bg-gray-50">Next</button>
                </div>
              </div>
            )}
          </>
        )}
      </div>

      {/* Create Modal */}
      {showModal && (
        <Modal title="New Contract" onClose={() => setShowModal(false)}>
          <form onSubmit={handleCreate} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Contract Reference">
                <Input value={form.contractRef} onChange={formChange('contractRef')} placeholder="CTR-001" />
              </FormField>
              <FormField label="Contract Title *">
                <Input value={form.title} onChange={formChange('title')} required placeholder="Main Works Contract" />
              </FormField>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Contract Type">
                <Select value={form.type} onChange={formChange('type')}>
                  <option value="MAIN_CONTRACT">Main Contract</option>
                  <option value="SUB_CONTRACT">Sub-contract</option>
                  <option value="FRAMEWORK">Framework Agreement</option>
                  <option value="TERMINABLE">Terminable</option>
                </Select>
              </FormField>
              <FormField label="Status">
                <Select value={form.status} onChange={formChange('status')}>
                  <option value="DRAFT">Draft</option>
                  <option value="ACTIVE">Active</option>
                  <option value="SUSPENDED">Suspended</option>
                </Select>
              </FormField>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Contract Value (£)">
                <Input type="number" step="0.01" value={form.value} onChange={formChange('value')} placeholder="0.00" />
              </FormField>
              <FormField label="Company ID">
                <Input type="number" value={form.companyId} onChange={formChange('companyId')} placeholder="1" />
              </FormField>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Start Date">
                <Input type="date" value={form.startDate} onChange={formChange('startDate')} />
              </FormField>
              <FormField label="End Date">
                <Input type="date" value={form.endDate} onChange={formChange('endDate')} />
              </FormField>
            </div>
            <FormField label="Description">
              <Textarea value={form.description} onChange={formChange('description')} placeholder="Contract description…" />
            </FormField>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setShowModal(false)} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
              <button type="submit" className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700">Create Contract</button>
            </div>
          </form>
        </Modal>
      )}

      {/* Detail Modal */}
      {detailModal && (
        <Modal title="Contract Details" onClose={() => setDetailModal(null)}>
          <div className="space-y-3 text-sm">
            <div className="grid grid-cols-2 gap-4">
              <div><p className="text-slate-500">Reference</p><p className="font-medium text-slate-800">{detailModal.contractRef || '—'}</p></div>
              <div><p className="text-slate-500">Status</p><p className="mt-0.5"><StatusBadge status={detailModal.status} /></p></div>
            </div>
            <div><p className="text-slate-500">Title</p><p className="font-medium text-slate-800">{detailModal.title || '—'}</p></div>
            <div><p className="text-slate-500">Company</p><p className="font-medium text-slate-800">{detailModal.company?.name || '—'}</p></div>
            <div className="grid grid-cols-2 gap-4">
              <div><p className="text-slate-500">Value</p><p className="font-medium text-slate-800">{detailModal.value != null ? `£${parseFloat(detailModal.value).toLocaleString()}` : '—'}</p></div>
              <div><p className="text-slate-500">Type</p><p className="font-medium text-slate-800">{detailModal.type || '—'}</p></div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div><p className="text-slate-500">Start Date</p><p className="font-medium text-slate-800">{detailModal.startDate || '—'}</p></div>
              <div><p className="text-slate-500">End Date</p><p className="font-medium text-slate-800">{detailModal.endDate || '—'}</p></div>
            </div>
            {detailModal.description && <div><p className="text-slate-500">Description</p><p className="text-slate-800">{detailModal.description}</p></div>}
          </div>
          <div className="flex justify-end mt-6">
            <button onClick={() => setDetailModal(null)} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Close</button>
          </div>
        </Modal>
      )}
    </div>
  );
}
