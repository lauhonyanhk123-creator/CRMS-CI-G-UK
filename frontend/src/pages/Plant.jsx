import { useEffect, useState } from 'react';
import axios from 'axios';

const API = '/api/v1';
const CATEGORIES = ['EXCAVATOR', 'DUMPER', 'ROLLER', 'PLANT_MISC', 'TOOL', 'VEHICLE', 'MATERIALS'];
const PLANT_STATUSES = ['AVAILABLE', 'ON_HIRE', 'OFF_HIRE', 'UNDER_REPAIR', 'DECOMMISSIONED'];
const HIRE_STATUSES = ['OWNED', 'HIRED_IN', 'HIRED_OUT', 'LEASED', 'CROSS_HIRED'];
const ALLOCATION_STATUSES = ['PLANNED', 'ALLOCATED', 'ACTIVE', 'COMPLETED', 'CANCELLED'];
const INSPECTION_RESULTS = ['SATISFACTORY', 'UNSATISFACTORY', 'CONDITIONAL'];

const STATUS_COLORS = {
  AVAILABLE: 'bg-green-100 text-green-700',
  ON_HIRE: 'bg-blue-100 text-blue-700',
  OFF_HIRE: 'bg-gray-100 text-gray-600',
  UNDER_REPAIR: 'bg-amber-100 text-amber-700',
  DECOMMISSIONED: 'bg-gray-200 text-gray-400',
};

const HIRE_COLORS = {
  OWNED: 'bg-purple-100 text-purple-700',
  HIRED_IN: 'bg-cyan-100 text-cyan-700',
  HIRED_OUT: 'bg-orange-100 text-orange-700',
  LEASED: 'bg-teal-100 text-teal-700',
};

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

function Badge({ children, color = 'gray' }) {
  const colors = { green: 'bg-green-100 text-green-700', red: 'bg-red-100 text-red-700', amber: 'bg-amber-100 text-amber-700', gray: 'bg-gray-100 text-gray-600', blue: 'bg-blue-100 text-blue-700', purple: 'bg-purple-100 text-purple-700', cyan: 'bg-cyan-100 text-cyan-700', orange: 'bg-orange-100 text-orange-700', teal: 'bg-teal-100 text-teal-700' };
  return <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${colors[color] || colors.gray}`}>{children}</span>;
}

function LolErBadge({ due, dueSoon }) {
  if (due) return <Badge color="red">Overdue</Badge>;
  if (dueSoon) return <Badge color="amber">Due Soon</Badge>;
  return <Badge color="green">OK</Badge>;
}

// ---- Plant Item Form ----
function PlantForm({ plant, onSubmit, onClose }) {
  const [form, setForm] = useState(plant || {
    plantRef: '', serialNumber: '', description: '', make: '', model: '', year: '',
    category: 'EXCAVATOR', weight: '', hireStatus: 'OWNED', supplierId: '',
    telematicsId: '', quickHitchType: '', status: 'AVAILABLE', dailyHireRate: '', notes: ''
  });
  const [submitting, setSubmitting] = useState(false);
  const fc = (f) => (e) => setForm({ ...form, [f]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const payload = { ...form };
      if (form.year) payload.year = parseInt(form.year);
      if (form.weight) payload.weight = parseFloat(form.weight);
      if (form.dailyHireRate) payload.dailyHireRate = parseFloat(form.dailyHireRate);
      if (form.supplierId === '') payload.supplierId = null;
      await onSubmit(payload);
      onClose();
    } catch (err) { alert(err.response?.data?.message || 'Failed'); }
    finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Plant Ref *"><Input value={form.plantRef} onChange={fc('plantRef')} required placeholder="e.g. JCB-001" /></Field>
        <Field label="Serial Number"><Input value={form.serialNumber} onChange={fc('serialNumber')} placeholder="S/N" /></Field>
      </div>
      <Field label="Description *"><Input value={form.description} onChange={fc('description')} required placeholder="JCB 3CX Backhoe Loader" /></Field>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Make"><Input value={form.make} onChange={fc('make')} placeholder="JCB" /></Field>
        <Field label="Model"><Input value={form.model} onChange={fc('model')} placeholder="3CX" /></Field>
      </div>
      <div className="grid grid-cols-3 gap-4">
        <Field label="Year"><Input type="number" value={form.year} onChange={fc('year')} placeholder="2022" /></Field>
        <Field label="Category *">
          <Select value={form.category} onChange={fc('category')}>
            {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
          </Select>
        </Field>
        <Field label="Weight (kg)"><Input type="number" step="0.01" value={form.weight} onChange={fc('weight')} placeholder="5000" /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Hire Status">
          <Select value={form.hireStatus} onChange={fc('hireStatus')}>
            {HIRE_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
          </Select>
        </Field>
        <Field label="Supplier ID"><Input type="number" value={form.supplierId} onChange={fc('supplierId')} placeholder="Company ID" /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Telematics ID"><Input value={form.telematicsId} onChange={fc('telematicsId')} placeholder="GPS/Telematics ID" /></Field>
        <Field label="Quick Hitch Type"><Input value={form.quickHitchType} onChange={fc('quickHitchType')} placeholder="Hydraulic" /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Status">
          <Select value={form.status} onChange={fc('status')}>
            {PLANT_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
          </Select>
        </Field>
        <Field label="Daily Hire Rate (£)"><Input type="number" step="0.01" value={form.dailyHireRate} onChange={fc('dailyHireRate')} placeholder="150.00" /></Field>
      </div>
      <Field label="Notes"><textarea value={form.notes} onChange={fc('notes')} rows={2} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" placeholder="Additional notes..." /></Field>
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
          {submitting ? 'Saving…' : plant ? 'Update Plant' : 'Create Plant'}
        </button>
      </div>
    </form>
  );
}

// ---- LOLER Form ----
function LOLERForm({ plantId, onClose, onAdded }) {
  const [form, setForm] = useState({ examinationDate: '', nextDueDate: '', examiner: '', examinerCompany: '', result: 'SATISFACTORY', reportRef: '', notes: '', documentRef: '' });
  const [submitting, setSubmitting] = useState(false);
  const fc = (f) => (e) => setForm({ ...form, [f]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await axios.post(`${API}/plant-items/${plantId}/loler-examinations`, form);
      onAdded();
      onClose();
    } catch (err) { alert(err.response?.data?.message || 'Failed to add LOLER examination'); }
    finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Examination Date *"><Input type="date" value={form.examinationDate} onChange={fc('examinationDate')} required /></Field>
        <Field label="Next Due Date *"><Input type="date" value={form.nextDueDate} onChange={fc('nextDueDate')} required /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Examiner"><Input value={form.examiner} onChange={fc('examiner')} placeholder="John Smith" /></Field>
        <Field label="Examiner Company"><Input value={form.examinerCompany} onChange={fc('examinerCompany')} placeholder="ABC Inspections Ltd" /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Result *">
          <Select value={form.result} onChange={fc('result')}>
            {INSPECTION_RESULTS.map(r => <option key={r} value={r}>{r}</option>)}
          </Select>
        </Field>
        <Field label="Report Reference"><Input value={form.reportRef} onChange={fc('reportRef')} placeholder="LOLER-2024-001" /></Field>
      </div>
      <Field label="Document Reference"><Input value={form.documentRef} onChange={fc('documentRef')} placeholder="DOC-12345" /></Field>
      <Field label="Notes"><textarea value={form.notes} onChange={fc('notes')} rows={2} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" /></Field>
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
          {submitting ? 'Saving…' : 'Add LOLER Examination'}
        </button>
      </div>
    </form>
  );
}

// ---- PUWER Form ----
function PUWERForm({ plantId, onClose, onAdded }) {
  const [form, setForm] = useState({ inspectionDate: '', nextDueDate: '', inspector: '', inspectorCompany: '', result: 'SATISFACTORY', reportRef: '', notes: '', documentRef: '' });
  const [submitting, setSubmitting] = useState(false);
  const fc = (f) => (e) => setForm({ ...form, [f]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await axios.post(`${API}/plant-items/${plantId}/puwer-inspections`, form);
      onAdded();
      onClose();
    } catch (err) { alert(err.response?.data?.message || 'Failed to add PUWER inspection'); }
    finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Inspection Date *"><Input type="date" value={form.inspectionDate} onChange={fc('inspectionDate')} required /></Field>
        <Field label="Next Due Date *"><Input type="date" value={form.nextDueDate} onChange={fc('nextDueDate')} required /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Inspector"><Input value={form.inspector} onChange={fc('inspector')} placeholder="Jane Doe" /></Field>
        <Field label="Inspector Company"><Input value={form.inspectorCompany} onChange={fc('inspectorCompany')} placeholder="XYZ Safety Ltd" /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Result *">
          <Select value={form.result} onChange={fc('result')}>
            {INSPECTION_RESULTS.map(r => <option key={r} value={r}>{r}</option>)}
          </Select>
        </Field>
        <Field label="Report Reference"><Input value={form.reportRef} onChange={fc('reportRef')} placeholder="PUWER-2024-001" /></Field>
      </div>
      <Field label="Document Reference"><Input value={form.documentRef} onChange={fc('documentRef')} placeholder="DOC-12345" /></Field>
      <Field label="Notes"><textarea value={form.notes} onChange={fc('notes')} rows={2} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" /></Field>
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
          {submitting ? 'Saving…' : 'Add PUWER Inspection'}
        </button>
      </div>
    </form>
  );
}

// ---- Allocation Form ----
function AllocationForm({ plantId, onClose, onAdded }) {
  const [form, setForm] = useState({ operativeId: '', siteId: '', startDate: '', endDate: '', status: 'ALLOCATED' });
  const [submitting, setSubmitting] = useState(false);
  const fc = (f) => (e) => setForm({ ...form, [f]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await axios.post(`${API}/plant-items/${plantId}/allocations`, form);
      onAdded();
      onClose();
    } catch (err) { alert(err.response?.data?.message || 'Failed to allocate plant'); }
    finally { setSubmitting(false); }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <Field label="Operative ID *"><Input type="number" value={form.operativeId} onChange={fc('operativeId')} required placeholder="Operative database ID" /></Field>
        <Field label="Site ID *"><Input type="number" value={form.siteId} onChange={fc('siteId')} required placeholder="Site database ID" /></Field>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <Field label="Start Date *"><Input type="date" value={form.startDate} onChange={fc('startDate')} required /></Field>
        <Field label="End Date"><Input type="date" value={form.endDate} onChange={fc('endDate')} /></Field>
      </div>
      <Field label="Status">
        <Select value={form.status} onChange={fc('status')}>
          {ALLOCATION_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
        </Select>
      </Field>
      <div className="flex justify-end gap-3 pt-2">
        <button type="button" onClick={onClose} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
        <button type="submit" disabled={submitting} className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50">
          {submitting ? 'Saving…' : 'Allocate Plant'}
        </button>
      </div>
    </form>
  );
}

// ---- Plant Detail ----
function PlantDetail({ plant, onClose, onDelete, onEdit }) {
  const [tab, setTab] = useState('overview');
  const [lolers, setLolers] = useState([]);
  const [puwers, setPuwers] = useState([]);
  const [loadingSafety, setLoadingSafety] = useState(false);

  const loadSafety = async () => {
    setLoadingSafety(true);
    try {
      const [lr, pr] = await Promise.all([
        axios.get(`${API}/plant-items/${plant.id}/loler-examinations`),
        axios.get(`${API}/plant-items/${plant.id}/puwer-inspections`)
      ]);
      setLolers(lr.data.data || []);
      setPuwers(pr.data.data || []);
    } catch {}
    finally { setLoadingSafety(false); }
  };

  const deleteLOLER = async (id) => {
    if (!confirm('Delete this LOLER examination?')) return;
    await axios.delete(`${API}/plant-items/${plant.id}/loler-examinations/${id}`);
    loadSafety();
  };

  const deletePUWER = async (id) => {
    if (!confirm('Delete this PUWER inspection?')) return;
    await axios.delete(`${API}/plant-items/${plant.id}/puwer-inspections/${id}`);
    loadSafety();
  };

  useEffect(() => { if (tab === 'safety') loadSafety(); }, [tab]);

  return (
    <Modal title={`Plant: ${plant.plantRef}`} onClose={onClose} size="max-w-3xl">
      <div className="flex items-center gap-4 mb-6 pb-4 border-b border-gray-200">
        <div className="w-14 h-14 rounded-xl bg-orange-100 flex items-center justify-center text-2xl">🚜</div>
        <div>
          <h2 className="text-xl font-bold text-slate-800">{plant.plantRef}</h2>
          <p className="text-sm text-slate-500">{plant.description} {plant.make && plant.model ? `· ${plant.make} ${plant.model}` : ''}</p>
        </div>
        <div className="ml-auto flex gap-2">
          <button onClick={() => { onEdit(); onClose(); }} className="px-3 py-1.5 text-xs border rounded-lg hover:bg-gray-50">Edit</button>
          <button onClick={() => { if (confirm('Delete this plant?')) { onDelete(plant.id); onClose(); } }} className="px-3 py-1.5 text-xs border border-red-200 text-red-600 rounded-lg hover:bg-red-50">Delete</button>
        </div>
      </div>

      <div className="flex gap-1 mb-5 bg-slate-100 p-1 rounded-lg w-fit">
        {['overview', 'safety', 'allocations'].map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-4 py-1.5 text-xs font-medium rounded-md transition-colors ${tab === t ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}>
            {t.charAt(0).toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {tab === 'overview' && (
        <div className="space-y-1">
          {[
            ['Description', plant.description],
            ['Category', plant.category],
            ['Status', plant.status],
            ['Hire Status', plant.hireStatus],
            ['Serial Number', plant.serialNumber],
            ['Year', plant.year],
            ['Weight', plant.weight ? `${plant.weight} kg` : null],
            ['Telematics ID', plant.telematicsId],
            ['Quick Hitch', plant.quickHitchType],
            ['Supplier', plant.supplierName],
            ['Daily Hire Rate', plant.dailyHireRate ? `£${plant.dailyHireRate}` : null],
          ].filter(([, v]) => v).map(([label, value]) => (
            <div key={label} className="flex justify-between py-2 border-b border-gray-50 last:border-0">
              <span className="text-sm text-slate-500">{label}</span>
              <span className="text-sm text-slate-800 font-medium text-right">{value}</span>
            </div>
          ))}
          {plant.notes && <div className="mt-3 p-3 bg-slate-50 rounded-lg text-sm text-slate-600">{plant.notes}</div>}
        </div>
      )}

      {tab === 'safety' && (
        <div>
          <div className="flex justify-between mb-4">
            <h4 className="text-sm font-semibold text-slate-600">LOLER Examinations</h4>
            <button onClick={() => setTab('add-loler')} className="px-3 py-1.5 text-xs bg-blue-600 text-white rounded-lg hover:bg-blue-700">+ Add LOLER</button>
          </div>
          {loadingSafety ? <p className="text-sm text-slate-400 mb-4">Loading…</p> : lolers.length > 0 ? (
            <div className="space-y-2 mb-6">
              {lolers.map(l => (
                <div key={l.id} className={`rounded-lg p-3 border flex justify-between items-center ${l.isDue ? 'bg-red-50 border-red-200' : l.isDueSoon ? 'bg-amber-50 border-amber-200' : 'bg-green-50 border-green-200'}`}>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-medium text-slate-800">Exam: {l.examinationDate}</span>
                      <LolErBadge due={l.isDue} dueSoon={l.isDueSoon} />
                    </div>
                    <p className="text-xs text-slate-500">Next due: {l.nextDueDate} · {l.examiner || 'N/A'} · {l.result}</p>
                    {l.reportRef && <p className="text-xs text-slate-400">Ref: {l.reportRef}</p>}
                  </div>
                  <button onClick={() => deleteLOLER(l.id)} className="px-2 py-1 text-xs border border-red-200 text-red-600 rounded hover:bg-red-50">Delete</button>
                </div>
              ))}
            </div>
          ) : <p className="text-sm text-slate-400 mb-4">No LOLER examinations recorded</p>}

          <div className="flex justify-between mb-4">
            <h4 className="text-sm font-semibold text-slate-600">PUWER Inspections</h4>
            <button onClick={() => setTab('add-puwer')} className="px-3 py-1.5 text-xs bg-blue-600 text-white rounded-lg hover:bg-blue-700">+ Add PUWER</button>
          </div>
          {loadingSafety ? <p className="text-sm text-slate-400">Loading…</p> : puwers.length > 0 ? (
            <div className="space-y-2">
              {puwers.map(p => (
                <div key={p.id} className={`rounded-lg p-3 border flex justify-between items-center ${p.isDue ? 'bg-red-50 border-red-200' : p.isDueSoon ? 'bg-amber-50 border-amber-200' : 'bg-green-50 border-green-200'}`}>
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-sm font-medium text-slate-800">Inspected: {p.inspectionDate}</span>
                      <LolErBadge due={p.isDue} dueSoon={p.isDueSoon} />
                    </div>
                    <p className="text-xs text-slate-500">Next due: {p.nextDueDate} · {p.inspector || 'N/A'} · {p.result}</p>
                    {p.reportRef && <p className="text-xs text-slate-400">Ref: {p.reportRef}</p>}
                  </div>
                  <button onClick={() => deletePUWER(p.id)} className="px-2 py-1 text-xs border border-red-200 text-red-600 rounded hover:bg-red-50">Delete</button>
                </div>
              ))}
            </div>
          ) : <p className="text-sm text-slate-400">No PUWER inspections recorded</p>}
        </div>
      )}

      {tab === 'add-loler' && <LOLERForm plantId={plant.id} onClose={() => setTab('safety')} onAdded={loadSafety} />}
      {tab === 'add-puwer' && <PUWERForm plantId={plant.id} onClose={() => setTab('safety')} onAdded={loadSafety} />}

      {tab === 'allocations' && (
        <div>
          <div className="flex justify-between mb-4">
            <h4 className="text-sm font-semibold text-slate-600">Plant Allocations</h4>
            <button onClick={() => setTab('add-alloc')} className="px-3 py-1.5 text-xs bg-blue-600 text-white rounded-lg hover:bg-blue-700">+ Allocate</button>
          </div>
          <p className="text-sm text-slate-400">Allocation records will appear here. Use the Gantt view for a timeline overview.</p>
        </div>
      )}
      {tab === 'add-alloc' && <AllocationForm plantId={plant.id} onClose={() => setTab('allocations')} onAdded={() => {}} />}
    </Modal>
  );
}

// ---- Main Plant Page ----
export default function Plant() {
  const [plants, setPlants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalPages, setTotalPages] = useState(1);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editPlant, setEditPlant] = useState(null);
  const [selectedPlant, setSelectedPlant] = useState(null);

  const fetchPlants = async (p = 0, s = search, st = statusFilter, ct = categoryFilter) => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page: p, size: 20, sort: 'plantRef' });
      if (s) params.append('search', s);
      if (st) params.append('status', st);
      if (ct) params.append('category', ct);
      const res = await axios.get(`${API}/plant-items?${params}`);
      const data = res.data.data;
      setPlants(data.content || data || []);
      setTotalPages(data.totalPages || 1);
      setTotal(data.totalElements || 0);
      setPage(p);
    } catch { setPlants([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { fetchPlants(); }, []);

  const debounce = (fn, ms = 400) => {
    let t; return (...a) => { clearTimeout(t); t = setTimeout(() => fn(...a), ms); };
  };
  const debouncedFetch = debounce((s) => fetchPlants(0, s, statusFilter, categoryFilter), 400);

  const handleCreate = async (form) => { await axios.post(`${API}/plant-items`, form); };
  const handleUpdate = async (form) => { await axios.patch(`${API}/plant-items/${editPlant.id}`, form); };
  const handleDelete = async (id) => { await axios.delete(`${API}/plant-items/${id}`); fetchPlants(page, search, statusFilter, categoryFilter); };

  const statsCards = [
    { label: 'Total Plant', value: total, icon: '🚜', color: 'bg-orange-50' },
    { label: 'Available', value: plants.filter(p => p.status === 'AVAILABLE').length, color: 'bg-green-50' },
    { label: 'On Hire', value: plants.filter(p => p.status === 'ON_HIRE').length, color: 'bg-blue-50' },
    { label: 'Under Repair', value: plants.filter(p => p.status === 'UNDER_REPAIR').length, color: 'bg-amber-50' },
  ];

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold text-slate-800">Plant & Equipment</h2>
          <p className="text-sm text-slate-500 mt-1">Plant items, LOLER/PUWER inspections and allocations</p>
        </div>
        <button onClick={() => { setEditPlant(null); setShowForm(true); }} className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 flex items-center gap-2">
          + New Plant Item
        </button>
      </div>

      <div className="grid grid-cols-4 gap-4 mb-6">
        {statsCards.map(s => (
          <div key={s.label} className={`${s.color} rounded-xl p-4`}>
            <p className="text-xs text-slate-500">{s.label}</p>
            <p className="text-2xl font-bold text-slate-800 mt-1">{loading ? '…' : s.value}</p>
          </div>
        ))}
      </div>

      <div className="flex gap-3 mb-6">
        <input value={search} onChange={e => { setSearch(e.target.value); debouncedFetch(e.target.value); }} placeholder="Search by plant ref or description…" className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
        <select value={statusFilter} onChange={e => { setStatusFilter(e.target.value); fetchPlants(0, search, e.target.value, categoryFilter); }} className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white">
          <option value="">All Status</option>
          {PLANT_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
        </select>
        <select value={categoryFilter} onChange={e => { setCategoryFilter(e.target.value); fetchPlants(0, search, statusFilter, e.target.value); }} className="px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white">
          <option value="">All Categories</option>
          {CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
        </select>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-slate-50 border-b border-gray-100">
                <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Plant Reference</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Description</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Category</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Status</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Hire</th>
                <th className="text-left px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Daily Rate</th>
                <th className="text-right px-5 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={7} className="text-center py-8 text-slate-400">Loading…</td></tr>
              ) : plants.length === 0 ? (
                <tr><td colSpan={7} className="text-center py-8 text-slate-400">No plant items found</td></tr>
              ) : plants.map(p => (
                <tr key={p.id} className="border-b border-gray-50 hover:bg-slate-50 transition-colors">
                  <td className="px-5 py-4">
                    <div className="flex items-center gap-2">
                      <span className="text-lg">🚜</span>
                      <div>
                        <p className="font-medium text-slate-800">{p.plantRef}</p>
                        {p.serialNumber && <p className="text-xs text-slate-400">S/N: {p.serialNumber}</p>}
                      </div>
                    </div>
                  </td>
                  <td className="px-5 py-4"><span className="text-sm text-slate-600">{p.description}</span></td>
                  <td className="px-5 py-4"><Badge color="purple">{p.category || '—'}</Badge></td>
                  <td className="px-5 py-4"><Badge color={p.status === 'AVAILABLE' ? 'green' : p.status === 'UNDER_REPAIR' ? 'amber' : 'blue'}>{p.status || '—'}</Badge></td>
                  <td className="px-5 py-4"><span className="text-sm text-slate-600">{p.hireStatus || '—'}</span></td>
                  <td className="px-5 py-4"><span className="text-sm text-slate-600">{p.dailyHireRate ? `£${p.dailyHireRate}` : '—'}</span></td>
                  <td className="px-5 py-4 text-right">
                    <button onClick={() => setSelectedPlant(p)} className="px-3 py-1 text-xs border rounded hover:bg-gray-50">View</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div className="flex justify-between items-center px-5 py-3 border-t border-gray-100">
            <p className="text-xs text-slate-500">Page {page + 1} of {totalPages} · {total} total</p>
            <div className="flex gap-1">
              <button disabled={page === 0} onClick={() => fetchPlants(page - 1, search, statusFilter, categoryFilter)} className="px-3 py-1 text-xs border rounded disabled:opacity-30 hover:bg-gray-50">Prev</button>
              <button disabled={page >= totalPages - 1} onClick={() => fetchPlants(page + 1, search, statusFilter, categoryFilter)} className="px-3 py-1 text-xs border rounded disabled:opacity-30 hover:bg-gray-50">Next</button>
            </div>
          </div>
        )}
      </div>

      {showForm && (
        <Modal title={editPlant ? `Edit Plant: ${editPlant.plantRef}` : 'New Plant Item'} onClose={() => setShowForm(false)} size="max-w-2xl">
          <PlantForm
            plant={editPlant}
            onSubmit={async (form) => {
              if (editPlant) { await handleUpdate(form); } else { await handleCreate(form); }
              setShowForm(false);
              fetchPlants(0, search, statusFilter, categoryFilter);
            }}
            onClose={() => setShowForm(false)}
          />
        </Modal>
      )}

      {selectedPlant && (
        <PlantDetail
          plant={selectedPlant}
          onClose={() => setSelectedPlant(null)}
          onDelete={handleDelete}
          onEdit={(p) => { setEditPlant(selectedPlant); setShowForm(true); }}
        />
      )}
    </div>
  );
}