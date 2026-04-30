import { useEffect, useState } from 'react';
import axios from 'axios';

const API = '/api/v1';

const TAB_TYPES = ['dashboard', 'f10', 'cpp', 'rams', 'permits', 'incidents'];

const STAT_COLORS = {
  f10: { bg: 'bg-blue-50', text: 'text-blue-700', icon: '📋' },
  cpp: { bg: 'bg-purple-50', text: 'text-purple-700', icon: '📑' },
  rams: { bg: 'bg-green-50', text: 'text-green-700', icon: '📄' },
  permits: { bg: 'bg-amber-50', text: 'text-amber-700', icon: '🔍' },
  incidents: { bg: 'bg-red-50', text: 'text-red-700', icon: '🚨' },
};

function StatCard({ label, value, type }) {
  const c = STAT_COLORS[type] || STAT_COLORS.f10;
  return (
    <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-slate-500">{label}</p>
          <p className="text-2xl font-bold mt-1 text-slate-800">{value ?? '—'}</p>
        </div>
        <div className={`w-12 h-12 rounded-lg flex items-center justify-center text-xl ${c.bg}`}>
          {c.icon}
        </div>
      </div>
    </div>
  );
}

function SectionCard({ title, children }) {
  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100">
      <div className="px-5 py-4 border-b border-gray-100">
        <h3 className="font-semibold text-slate-800">{title}</h3>
      </div>
      <div className="p-5">{children}</div>
    </div>
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
      <label className="block text-sm font-medium text-slate-700 mb-1">{label}</label>
      {children}
    </div>
  );
}

function Input({ className = '', ...props }) {
  return <input {...props} className={`w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${className}`} />;
}

function Select({ children, ...props }) {
  return <select {...props} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500">{children}</select>;
}

function Textarea({ ...props }) {
  return <textarea {...props} rows={3} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 resize-none" />;
}

export default function HealthSafety() {
  const [tab, setTab] = useState('dashboard');
  const [f10, setF10] = useState([]);
  const [cpp, setCpp] = useState([]);
  const [rams, setRams] = useState([]);
  const [permits, setPermits] = useState([]);
  const [incidents, setIncidents] = useState([]);
  const [stats, setStats] = useState({});
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(null);
  const [contractId, setContractId] = useState('');

  const [f10Form, setF10Form] = useState({ projectName: '', descriptionOfWork: '', startDate: '', expectedCompletionDate: '', principalContractor: '', principalDesigner: '', moreThan30Days: false, moreThan500PersonDays: false });
  const [incidentForm, setIncidentForm] = useState({ siteId: '', contractId: '', title: '', incidentType: 'NEAR_MISS', dateTimeOfIncident: '', location: '', description: '', ridDORNotifiable: false, severity: 'MINOR' });

  useEffect(() => {
    async function load() {
      setLoading(true);
      try {
        const [f10Res, hsStats] = await Promise.all([
          axios.get(`${API}/f10-notifications`).catch(() => ({ data: { data: [] } })),
          axios.get(`${API}/dashboard/health-safety-stats`).catch(() => ({ data: { data: {} } })),
        ]);
        setF10(f10Res.data.data?.content || f10Res.data.data || f10Res.data || []);
        setStats(hsStats.data.data || hsStats.data || {});
      } catch { /* silent */ }
      finally { setLoading(false); }
    }
    load();
  }, []);

  const fc = (form, set) => (field) => (e) => set({ ...form, [field]: e.target.value });

  const handleCreateIncident = async (e) => {
    e.preventDefault();
    try {
      await axios.post(`${API}/healthsafety/incidents`, {
        ...incidentForm,
        siteId: parseInt(incidentForm.siteId),
        contractId: parseInt(incidentForm.contractId),
        ridDORNotifiable: incidentForm.ridDORNotifiable,
      });
      setShowModal(null);
    } catch (err) { alert(err.response?.data?.message || 'Failed to report incident'); }
  };

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-slate-800">Health & Safety</h2>
        <p className="text-sm text-slate-500 mt-1">H&S dashboard, permits, RAMS, incidents and more</p>
      </div>

      {/* Tab bar */}
      <div className="flex gap-1 mb-6 bg-slate-100 p-1 rounded-xl w-fit">
        {['dashboard', 'F10', 'CPP', 'RAMS', 'Permits', 'Incidents'].map((t) => (
          <button key={t} onClick={() => setTab(t.toLowerCase())}
            className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors ${tab === t.toLowerCase() ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}>
            {t}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="p-8 text-center text-slate-400">Loading…</div>
      ) : tab === 'dashboard' ? (
        <div>
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <StatCard label="F10 Notifications" value={stats.f10Count || f10.length} type="f10" />
            <StatCard label="CPP Documents" value={stats.cppCount || cpp.length} type="cpp" />
            <StatCard label="Active RAMS" value={stats.ramsCount || rams.length} type="rams" />
            <StatCard label="Incidents Reported" value={stats.incidentCount || incidents.length} type="incidents" />
          </div>
          <SectionCard title="Recent F10 Notifications">
            {f10.length > 0 ? (
              <div className="space-y-3">
                {f10.slice(0, 5).map((item) => (
                  <div key={item.id} className="flex justify-between items-center pb-3 border-b border-gray-50 last:border-0 last:pb-0">
                    <div>
                      <p className="text-sm font-medium text-slate-700">{item.notificationNumber || item.projectName || '—'}</p>
                      <p className="text-xs text-slate-400">{item.projectName || '—'}</p>
                    </div>
                    <span className="text-xs text-slate-500">{item.status || '—'}</span>
                  </div>
                ))}
              </div>
            ) : <p className="text-sm text-slate-400">No F10 notifications</p>}
          </SectionCard>
        </div>
      ) : tab === 'incidents' ? (
        <div>
          <div className="flex justify-between mb-6">
            <p className="text-sm text-slate-500 mt-1">{incidents.length} incidents</p>
            <button onClick={() => setShowModal('incident')} className="px-4 py-2 bg-red-600 text-white text-sm font-medium rounded-lg hover:bg-red-700">+ Report Incident</button>
          </div>
          {incidents.length > 0 ? (
            <div className="space-y-3">
              {incidents.map((inc) => (
                <div key={inc.id} className="bg-white rounded-xl shadow-sm border border-gray-100 p-5">
                  <div className="flex justify-between items-start">
                    <div>
                      <h4 className="font-medium text-slate-800">{inc.reportNumber || inc.title || `#${inc.id}`}</h4>
                      <p className="text-sm text-slate-500 mt-1">{inc.description || '—'}</p>
                      <p className="text-xs text-slate-400 mt-1">📅 {inc.incidentDate || '—'}</p>
                    </div>
                    <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${inc.severity === 'MAJOR' ? 'bg-red-100 text-red-700' : inc.severity === 'MINOR' ? 'bg-amber-100 text-amber-700' : 'bg-gray-100 text-gray-600'}`}>{inc.severity || '—'}</span>
                  </div>
                </div>
              ))}
            </div>
          ) : <p className="text-sm text-slate-400">No incidents reported</p>}
        </div>
      ) : (
        <SectionCard title={`${tab.toUpperCase()} Records`}>
          <p className="text-sm text-slate-400">Data for this section will load here. Create records via the API or a dedicated form.</p>
          <p className="text-xs text-slate-400 mt-2">Tip: Add Contract ID <code className="bg-slate-100 px-1 rounded">?contractId=</code> to link documents to contracts.</p>
        </SectionCard>
      )}

      {/* Incident Report Modal */}
      {showModal === 'incident' && (
        <Modal title="Report Incident" onClose={() => setShowModal(null)}>
          <form onSubmit={handleCreateIncident} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <Field label="Site ID *"><Input type="number" value={incidentForm.siteId} onChange={fc(incidentForm, setIncidentForm)('siteId')} required placeholder="1" /></Field>
              <Field label="Contract ID *"><Input type="number" value={incidentForm.contractId} onChange={fc(incidentForm, setIncidentForm)('contractId')} required placeholder="1" /></Field>
            </div>
            <Field label="Title *"><Input value={incidentForm.title} onChange={fc(incidentForm, setIncidentForm)('title')} required placeholder="Brief title of incident" /></Field>
            <div className="grid grid-cols-2 gap-4">
              <Field label="Type"><Select value={incidentForm.incidentType} onChange={fc(incidentForm, setIncidentForm)('incidentType')}><option value="NEAR_MISS">Near Miss</option><option value="INJURY">Injury</option><option value="DANGEROUS_OCCURRENCE">Dangerous Occurrence</option><option value="ILL_HEALTH">Ill Health</option></Select></Field>
              <Field label="Date & Time *"><Input type="datetime-local" value={incidentForm.dateTimeOfIncident} onChange={fc(incidentForm, setIncidentForm)('dateTimeOfIncident')} required /></Field>
            </div>
            <Field label="Location *"><Input value={incidentForm.location} onChange={fc(incidentForm, setIncidentForm)('location')} required placeholder="Where did it happen?" /></Field>
            <Field label="Description *"><Textarea value={incidentForm.description} onChange={fc(incidentForm, setIncidentForm)('description')} required placeholder="Detailed description of the incident…" /></Field>
            <div className="grid grid-cols-2 gap-4">
              <Field label="Severity"><Select value={incidentForm.severity} onChange={fc(incidentForm, setIncidentForm)('severity')}><option value="MINOR">Minor</option><option value="MAJOR">Major</option><option value="CRITICAL">Critical</option><option value="FATAL">Fatal</option></Select></Field>
              <div className="flex items-center gap-2 pt-6">
                <input type="checkbox" checked={incidentForm.ridDORNotifiable} onChange={(e) => setIncidentForm({ ...incidentForm, ridDORNotifiable: e.target.checked })} className="w-4 h-4" />
                <label className="text-sm text-slate-700">RIDDOR Notifiable</label>
              </div>
            </div>
            <div className="flex justify-end gap-3 pt-2">
              <button type="button" onClick={() => setShowModal(null)} className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50">Cancel</button>
              <button type="submit" className="px-4 py-2 text-sm bg-red-600 text-white rounded-lg hover:bg-red-700">Report Incident</button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
