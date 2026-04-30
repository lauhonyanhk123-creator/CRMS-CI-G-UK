import { useEffect, useState, useCallback } from 'react';
import axios from 'axios';

const API = '/api/v1';

const APPLICATION_STATUS_COLORS = {
  DRAFT: 'bg-gray-100 text-gray-700',
  SUBMITTED: 'bg-blue-100 text-blue-700',
  NOTIFIED: 'bg-amber-100 text-amber-700',
  PAID: 'bg-green-100 text-green-700',
  PARTIAL: 'bg-orange-100 text-orange-700',
  DISPUTED: 'bg-red-100 text-red-700',
};

const VARIATION_STATUS_COLORS = {
  PROPOSED: 'bg-amber-100 text-amber-700',
  APPROVED: 'bg-green-100 text-green-700',
  REJECTED: 'bg-red-100 text-red-700',
  INSTRUCTED: 'bg-blue-100 text-blue-700',
  VALUED: 'bg-purple-100 text-purple-700',
};

const RETENTION_STATUS_COLORS = {
  HELD: 'bg-blue-100 text-blue-700',
  RELEASABLE: 'bg-green-100 text-green-700',
  RELEASED: 'bg-slate-100 text-slate-600',
};

function StatusBadge({ status, colors }) {
  return (
    <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${colors[status] || 'bg-gray-100 text-gray-600'}`}>
      {status || '—'}
    </span>
  );
}

function Modal({ title, onClose, children, size = 'max-w-2xl' }) {
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className={`bg-white rounded-xl w-full ${size} max-h-[90vh] overflow-y-auto shadow-2xl`}>
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

function formatCurrency(value) {
  if (value === null || value === undefined) return '—';
  return new Intl.NumberFormat('en-GB', { style: 'currency', currency: 'GBP' }).format(value);
}

function formatDate(date) {
  if (!date) return '—';
  return new Date(date).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
}

function LoadingSpinner() {
  return (
    <div className="flex justify-center items-center py-12">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
    </div>
  );
}

export default function Payments() {
  const [activeTab, setActiveTab] = useState('applications');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  const [contracts, setContracts] = useState([]);
  const [selectedContractId, setSelectedContractId] = useState('');
  
  const [applications, setApplications] = useState([]);
  const [applicationsPage, setApplicationsPage] = useState(0);
  const [applicationsTotalPages, setApplicationsTotalPages] = useState(0);
  
  const [variations, setVariations] = useState([]);
  const [variationsPage, setVariationsPage] = useState(0);
  const [variationsTotalPages, setVariationsTotalPages] = useState(0);
  
  const [dayworks, setDayworks] = useState([]);
  const [dayworksPage, setDayworksPage] = useState(0);
  const [dayworksTotalPages, setDayworksTotalPages] = useState(0);
  
  const [retentions, setRetentions] = useState([]);
  
  const [stats, setStats] = useState({
    totalCertifiedYTD: 0,
    awaitingPayment: 0,
    inDispute: 0,
    retentionHeld: 0,
  });
  
  const [showAppModal, setShowAppModal] = useState(false);
  const [showVariationModal, setShowVariationModal] = useState(false);
  const [showDayworkModal, setShowDayworkModal] = useState(false);
  const [detailApplication, setDetailApplication] = useState(null);
  
  const [appForm, setAppForm] = useState({
    contractId: '',
    applicationNumber: '',
    applicationDate: '',
    periodStart: '',
    periodEnd: '',
    notes: '',
  });
  
  const [variationForm, setVariationForm] = useState({
    contractId: '',
    variationNumber: '',
    reference: '',
    description: '',
    instructionType: 'JCT_EI',
    status: 'PROPOSED',
    value: '',
    approvedValue: '',
    instructionDate: '',
    approvedDate: '',
    notes: '',
  });
  
  const [dayworkForm, setDayworkForm] = useState({
    contractId: '',
    ticketDate: '',
    operativeId: '',
    operativeName: '',
    plantId: '',
    plantDescription: '',
    trade: '',
    hours: '',
    hourlyRate: '',
    description: '',
    notes: '',
  });
  
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(null);

  const fetchContracts = useCallback(async () => {
    try {
      const { data } = await axios.get(`${API}/contracts`, { params: { size: 100 } });
      setContracts(data.data?.content || data.data || data || []);
    } catch (err) {
      console.error('Failed to fetch contracts:', err);
    }
  }, []);

  const fetchApplications = useCallback(async (pageNum = 0) => {
    setLoading(true);
    try {
      const params = { page: pageNum, size: 15 };
      if (selectedContractId) params.contractId = selectedContractId;
      const { data } = await axios.get(`${API}/applications`, { params });
      const payload = data.data || data;
      setApplications(payload.content || payload || []);
      setApplicationsTotalPages(payload.totalPages || 1);
    } catch (err) {
      setError('Failed to load applications');
    } finally {
      setLoading(false);
    }
  }, [selectedContractId]);

  const fetchVariations = useCallback(async (pageNum = 0) => {
    setLoading(true);
    try {
      const params = { page: pageNum, size: 15 };
      if (selectedContractId) params.contractId = selectedContractId;
      const { data } = await axios.get(`${API}/variations`, { params });
      const payload = data.data || data;
      setVariations(payload.content || payload || []);
      setVariationsTotalPages(payload.totalPages || 1);
    } catch (err) {
      setError('Failed to load variations');
    } finally {
      setLoading(false);
    }
  }, [selectedContractId]);

  const fetchDayworks = useCallback(async (pageNum = 0) => {
    setLoading(true);
    try {
      const params = { page: pageNum, size: 15 };
      if (selectedContractId) params.contractId = selectedContractId;
      const { data } = await axios.get(`${API}/dayworks`, { params });
      const payload = data.data || data;
      setDayworks(payload.content || payload || []);
      setDayworksTotalPages(payload.totalPages || 1);
    } catch (err) {
      setError('Failed to load dayworks');
    } finally {
      setLoading(false);
    }
  }, [selectedContractId]);

  const fetchRetentions = useCallback(async () => {
    try {
      const params = {};
      if (selectedContractId) params.contractId = selectedContractId;
      const { data } = await axios.get(`${API}/retentions`, { params });
      const payload = data.data || data;
      setRetentions(payload.content || payload || []);
    } catch (err) {
      console.error('Failed to load retentions:', err);
    }
  }, [selectedContractId]);

  const fetchStats = useCallback(async () => {
    try {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      
      let totalCertified = 0;
      let awaiting = 0;
      let dispute = 0;
      let held = 0;
      
      const appsResponse = await axios.get(`${API}/applications`, { params: { size: 500 } });
      const allApps = appsResponse.data.data?.content || appsResponse.data.data || appsResponse.data || [];
      
      allApps.forEach(app => {
        if (app.certifiedValue) totalCertified += app.certifiedValue;
        if (app.status === 'NOTIFIED' || app.status === 'SUBMITTED') awaiting += 1;
        if (app.status === 'DISPUTED') dispute += 1;
      });
      
      const retResponse = await axios.get(`${API}/retentions`, { params: { size: 500 } });
      const allRet = retResponse.data.data?.content || retResponse.data.data || retResponse.data || [];
      allRet.forEach(r => {
        if (r.retentionHeld) held += r.retentionHeld;
      });
      
      setStats({
        totalCertifiedYTD: totalCertified,
        awaitingPayment: awaiting,
        inDispute: dispute,
        retentionHeld: held,
      });
    } catch (err) {
      console.error('Failed to fetch stats:', err);
    }
  }, []);

  useEffect(() => {
    fetchContracts();
  }, [fetchContracts]);

  useEffect(() => {
    if (activeTab === 'applications') {
      fetchApplications(applicationsPage);
    } else if (activeTab === 'variations') {
      fetchVariations(variationsPage);
    } else if (activeTab === 'dayworks') {
      fetchDayworks(dayworksPage);
    } else if (activeTab === 'retention') {
      fetchRetentions();
    }
  }, [activeTab, selectedContractId, applicationsPage, variationsPage, dayworksPage, fetchApplications, fetchVariations, fetchDayworks, fetchRetentions]);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  const handleContractChange = (contractId) => {
    setSelectedContractId(contractId);
    setApplicationsPage(0);
    setVariationsPage(0);
    setDayworksPage(0);
  };

  const openAppModal = (app = null) => {
    if (app) {
      setAppForm({
        id: app.id,
        contractId: app.contractId,
        applicationNumber: app.applicationNumber,
        applicationDate: app.applicationDate?.split('T')[0] || '',
        periodStart: app.periodStart?.split('T')[0] || '',
        periodEnd: app.periodEnd?.split('T')[0] || '',
        notes: app.notes || '',
      });
    } else {
      setAppForm({
        contractId: selectedContractId || '',
        applicationNumber: '',
        applicationDate: new Date().toISOString().split('T')[0],
        periodStart: '',
        periodEnd: '',
        notes: '',
      });
    }
    setShowAppModal(true);
  };

  const handleAppSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (appForm.id) {
        await axios.patch(`${API}/applications/${appForm.id}`, appForm);
      } else {
        await axios.post(`${API}/applications`, { ...appForm, status: 'DRAFT' });
      }
      setShowAppModal(false);
      fetchApplications(applicationsPage);
      fetchStats();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to save application');
    } finally {
      setSaving(false);
    }
  };

  const handleAppSubmitForPayment = async (app) => {
    if (!confirm('Submit this application for payment?')) return;
    try {
      await axios.patch(`${API}/applications/${app.id}`, {
        status: 'SUBMITTED',
        submittedDate: new Date().toISOString(),
      });
      fetchApplications(applicationsPage);
      fetchStats();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to submit application');
    }
  };

  const handleAppDelete = async (id) => {
    if (!confirm('Delete this application?')) return;
    setDeleting(id);
    try {
      await axios.delete(`${API}/applications/${id}`);
      fetchApplications(applicationsPage);
      fetchStats();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete application');
    } finally {
      setDeleting(null);
    }
  };

  const handleUpdatePaymentDates = async (app, field, value) => {
    try {
      await axios.patch(`${API}/applications/${app.id}`, { [field]: value });
      fetchApplications(applicationsPage);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update date');
    }
  };

  const openVariationModal = (variation = null) => {
    if (variation) {
      setVariationForm({
        id: variation.id,
        contractId: variation.contractId,
        variationNumber: variation.variationNumber,
        reference: variation.reference,
        description: variation.description,
        instructionType: variation.instructionType,
        status: variation.status,
        value: variation.value || '',
        approvedValue: variation.approvedValue || '',
        instructionDate: variation.instructionDate?.split('T')[0] || '',
        approvedDate: variation.approvedDate?.split('T')[0] || '',
        notes: variation.notes || '',
      });
    } else {
      setVariationForm({
        contractId: selectedContractId || '',
        variationNumber: '',
        reference: '',
        description: '',
        instructionType: 'JCT_EI',
        status: 'PROPOSED',
        value: '',
        approvedValue: '',
        instructionDate: '',
        approvedDate: '',
        notes: '',
      });
    }
    setShowVariationModal(true);
  };

  const handleVariationSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...variationForm,
        value: variationForm.value ? parseFloat(variationForm.value) : null,
        approvedValue: variationForm.approvedValue ? parseFloat(variationForm.approvedValue) : null,
      };
      if (variationForm.id) {
        await axios.patch(`${API}/variations/${variationForm.id}`, payload);
      } else {
        await axios.post(`${API}/variations`, payload);
      }
      setShowVariationModal(false);
      fetchVariations(variationsPage);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to save variation');
    } finally {
      setSaving(false);
    }
  };

  const openDayworkModal = () => {
    setDayworkForm({
      contractId: selectedContractId || '',
      ticketDate: new Date().toISOString().split('T')[0],
      operativeId: '',
      operativeName: '',
      plantId: '',
      plantDescription: '',
      trade: '',
      hours: '',
      hourlyRate: '',
      description: '',
      notes: '',
    });
    setShowDayworkModal(true);
  };

  const handleDayworkSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...dayworkForm,
        hours: dayworkForm.hours ? parseFloat(dayworkForm.hours) : 0,
        hourlyRate: dayworkForm.hourlyRate ? parseFloat(dayworkForm.hourlyRate) : 0,
        totalValue: (parseFloat(dayworkForm.hours) || 0) * (parseFloat(dayworkForm.hourlyRate) || 0),
      };
      await axios.post(`${API}/dayworks`, payload);
      setShowDayworkModal(false);
      fetchDayworks(dayworksPage);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to save daywork');
    } finally {
      setSaving(false);
    }
  };

  const isApproachingRelease = (defectsEndDate) => {
    if (!defectsEndDate) return false;
    const endDate = new Date(defectsEndDate);
    const now = new Date();
    const daysUntilRelease = Math.ceil((endDate - now) / (1000 * 60 * 60 * 24));
    return daysUntilRelease > 0 && daysUntilRelease <= 90;
  };

  const getDaysUntilRelease = (defectsEndDate) => {
    if (!defectsEndDate) return null;
    const endDate = new Date(defectsEndDate);
    const now = new Date();
    return Math.ceil((endDate - now) / (1000 * 60 * 60 * 24));
  };

  const tabs = [
    { id: 'applications', label: 'Applications' },
    { id: 'variations', label: 'Variations' },
    { id: 'dayworks', label: 'Dayworks' },
    { id: 'retention', label: 'Retention' },
  ];

  const instructionTypes = [
    { value: 'JCT_EI', label: 'JCT Early Warning' },
    { value: 'NEC4_CE', label: 'NEC4 Compensation Event' },
    { value: 'OTHER', label: 'Other' },
  ];

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Payments</h1>
          <p className="text-sm text-slate-500 mt-1">Applications for payment, variations, dayworks and retentions</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
        <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
          <p className="text-sm text-slate-500 mb-1">Total Certified YTD</p>
          <p className="text-2xl font-bold text-slate-800">{formatCurrency(stats.totalCertifiedYTD)}</p>
        </div>
        <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
          <p className="text-sm text-slate-500 mb-1">Awaiting Payment</p>
          <p className="text-2xl font-bold text-amber-600">{stats.awaitingPayment}</p>
        </div>
        <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
          <p className="text-sm text-slate-500 mb-1">In Dispute</p>
          <p className="text-2xl font-bold text-red-600">{stats.inDispute}</p>
        </div>
        <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
          <p className="text-sm text-slate-500 mb-1">Retention Held</p>
          <p className="text-2xl font-bold text-slate-800">{formatCurrency(stats.retentionHeld)}</p>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100">
        <div className="border-b border-gray-200">
          <div className="flex items-center justify-between px-4">
            <nav className="flex space-x-1">
              {tabs.map(tab => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
                    activeTab === tab.id
                      ? 'border-blue-600 text-blue-600'
                      : 'border-transparent text-slate-500 hover:text-slate-700'
                  }`}
                >
                  {tab.label}
                </button>
              ))}
            </nav>
            <div className="flex items-center gap-3">
              <Select
                value={selectedContractId}
                onChange={(e) => handleContractChange(e.target.value)}
                className="text-sm"
              >
                <option value="">All Contracts</option>
                {contracts.map(c => (
                  <option key={c.id} value={c.id}>{c.contractRef || c.title}</option>
                ))}
              </Select>
              {activeTab === 'applications' && (
                <button
                  onClick={() => openAppModal()}
                  className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
                >
                  New Application
                </button>
              )}
              {activeTab === 'variations' && (
                <button
                  onClick={() => openVariationModal()}
                  className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
                >
                  New Variation
                </button>
              )}
              {activeTab === 'dayworks' && (
                <button
                  onClick={() => openDayworkModal()}
                  className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 transition-colors"
                >
                  New Daywork
                </button>
              )}
            </div>
          </div>
        </div>

        <div className="p-4">
          {activeTab === 'applications' && (
            <div>
              {loading ? (
                <LoadingSpinner />
              ) : applications.length === 0 ? (
                <div className="text-center py-12 text-slate-500">
                  <p>No applications found</p>
                </div>
              ) : (
                <>
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                          <th className="pb-3 pr-4">App No.</th>
                          <th className="pb-3 pr-4">Period</th>
                          <th className="pb-3 pr-4">Contract</th>
                          <th className="pb-3 pr-4 text-right">Gross</th>
                          <th className="pb-3 pr-4 text-right">Retention</th>
                          <th className="pb-3 pr-4 text-right">Net</th>
                          <th className="pb-3 pr-4">Status</th>
                          <th className="pb-3 pr-4">Notice Dates</th>
                          <th className="pb-3">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100">
                        {applications.map(app => (
                          <tr key={app.id} className="hover:bg-gray-50">
                            <td className="py-3 pr-4 font-medium text-slate-800">{app.applicationNumber}</td>
                            <td className="py-3 pr-4 text-sm text-slate-600">
                              <div>{formatDate(app.periodStart)}</div>
                              <div className="text-xs text-slate-400">to {formatDate(app.periodEnd)}</div>
                            </td>
                            <td className="py-3 pr-4 text-sm text-slate-600">
                              <div className="font-medium">{app.contractRef}</div>
                              <div className="text-xs text-slate-400 truncate max-w-32">{app.contractTitle}</div>
                            </td>
                            <td className="py-3 pr-4 text-right font-medium text-slate-800">{formatCurrency(app.grossValue)}</td>
                            <td className="py-3 pr-4 text-right text-slate-600">{formatCurrency(app.retention)}</td>
                            <td className="py-3 pr-4 text-right font-medium text-slate-800">{formatCurrency(app.netValue)}</td>
                            <td className="py-3 pr-4">
                              <StatusBadge status={app.status} colors={APPLICATION_STATUS_COLORS} />
                            </td>
                            <td className="py-3 pr-4 text-xs text-slate-500">
                              <div>Pay: {formatDate(app.paymentNoticeDate)}</div>
                              <div>Less: {formatDate(app.payLessNoticeDate)}</div>
                            </td>
                            <td className="py-3">
                              <div className="flex items-center gap-2">
                                <button
                                  onClick={() => setDetailApplication(app)}
                                  className="p-1.5 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded"
                                  title="View Details"
                                >
                                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                  </svg>
                                </button>
                                {app.status === 'DRAFT' && (
                                  <button
                                    onClick={() => handleAppSubmitForPayment(app)}
                                    className="p-1.5 text-slate-400 hover:text-green-600 hover:bg-green-50 rounded"
                                    title="Submit for Payment"
                                  >
                                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                    </svg>
                                  </button>
                                )}
                                <button
                                  onClick={() => openAppModal(app)}
                                  className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-gray-100 rounded"
                                  title="Edit"
                                >
                                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                                  </svg>
                                </button>
                                <button
                                  onClick={() => handleAppDelete(app.id)}
                                  disabled={deleting === app.id}
                                  className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded disabled:opacity-50"
                                  title="Delete"
                                >
                                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                  </svg>
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  {applicationsTotalPages > 1 && (
                    <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-100">
                      <p className="text-sm text-slate-500">Page {applicationsPage + 1} of {applicationsTotalPages}</p>
                      <div className="flex gap-2">
                        <button
                          onClick={() => setApplicationsPage(p => Math.max(0, p - 1))}
                          disabled={applicationsPage === 0}
                          className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50"
                        >
                          Previous
                        </button>
                        <button
                          onClick={() => setApplicationsPage(p => p + 1)}
                          disabled={applicationsPage >= applicationsTotalPages - 1}
                          className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50"
                        >
                          Next
                        </button>
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          {activeTab === 'variations' && (
            <div>
              {loading ? (
                <LoadingSpinner />
              ) : variations.length === 0 ? (
                <div className="text-center py-12 text-slate-500">
                  <p>No variations found</p>
                </div>
              ) : (
                <>
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                          <th className="pb-3 pr-4">Ref</th>
                          <th className="pb-3 pr-4">Description</th>
                          <th className="pb-3 pr-4">Type</th>
                          <th className="pb-3 pr-4">Status</th>
                          <th className="pb-3 pr-4 text-right">Value</th>
                          <th className="pb-3 pr-4">Instruction Date</th>
                          <th className="pb-3">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100">
                        {variations.map(v => (
                          <tr key={v.id} className="hover:bg-gray-50">
                            <td className="py-3 pr-4 font-medium text-slate-800">{v.variationNumber || v.reference}</td>
                            <td className="py-3 pr-4 text-sm text-slate-600 max-w-xs truncate">{v.description}</td>
                            <td className="py-3 pr-4 text-sm">
                              {instructionTypes.find(t => t.value === v.instructionType)?.label || v.instructionType}
                            </td>
                            <td className="py-3 pr-4">
                              <StatusBadge status={v.status} colors={VARIATION_STATUS_COLORS} />
                            </td>
                            <td className="py-3 pr-4 text-right font-medium text-slate-800">
                              {formatCurrency(v.approvedValue || v.value)}
                            </td>
                            <td className="py-3 pr-4 text-sm text-slate-600">{formatDate(v.instructionDate)}</td>
                            <td className="py-3">
                              <div className="flex items-center gap-2">
                                <button
                                  onClick={() => openVariationModal(v)}
                                  className="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-gray-100 rounded"
                                  title="Edit"
                                >
                                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                                  </svg>
                                </button>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  {variationsTotalPages > 1 && (
                    <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-100">
                      <p className="text-sm text-slate-500">Page {variationsPage + 1} of {variationsTotalPages}</p>
                      <div className="flex gap-2">
                        <button
                          onClick={() => setVariationsPage(p => Math.max(0, p - 1))}
                          disabled={variationsPage === 0}
                          className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50"
                        >
                          Previous
                        </button>
                        <button
                          onClick={() => setVariationsPage(p => p + 1)}
                          disabled={variationsPage >= variationsTotalPages - 1}
                          className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50"
                        >
                          Next
                        </button>
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          {activeTab === 'dayworks' && (
            <div>
              {loading ? (
                <LoadingSpinner />
              ) : dayworks.length === 0 ? (
                <div className="text-center py-12 text-slate-500">
                  <p>No dayworks found</p>
                </div>
              ) : (
                <>
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                          <th className="pb-3 pr-4">Date</th>
                          <th className="pb-3 pr-4">Operative / Plant</th>
                          <th className="pb-3 pr-4">Trade</th>
                          <th className="pb-3 pr-4 text-right">Hours</th>
                          <th className="pb-3 pr-4 text-right">Rate</th>
                          <th className="pb-3 pr-4 text-right">Total</th>
                          <th className="pb-3 pr-4">Approved</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-gray-100">
                        {dayworks.map(d => (
                          <tr key={d.id} className="hover:bg-gray-50">
                            <td className="py-3 pr-4 text-sm text-slate-600">{formatDate(d.ticketDate)}</td>
                            <td className="py-3 pr-4">
                              <div className="text-sm font-medium text-slate-800">{d.operativeName || 'Plant'}</div>
                              <div className="text-xs text-slate-400">{d.plantDescription}</div>
                            </td>
                            <td className="py-3 pr-4 text-sm text-slate-600">{d.trade}</td>
                            <td className="py-3 pr-4 text-right text-slate-600">{d.hours}</td>
                            <td className="py-3 pr-4 text-right text-slate-600">{formatCurrency(d.hourlyRate)}</td>
                            <td className="py-3 pr-4 text-right font-medium text-slate-800">{formatCurrency(d.totalValue)}</td>
                            <td className="py-3 pr-4">
                              {d.approved ? (
                                <span className="inline-flex items-center text-green-600">
                                  <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                  </svg>
                                  {d.approvedBy}
                                </span>
                              ) : (
                                <span className="text-slate-400">Pending</span>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                  {dayworksTotalPages > 1 && (
                    <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-100">
                      <p className="text-sm text-slate-500">Page {dayworksPage + 1} of {dayworksTotalPages}</p>
                      <div className="flex gap-2">
                        <button
                          onClick={() => setDayworksPage(p => Math.max(0, p - 1))}
                          disabled={dayworksPage === 0}
                          className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50"
                        >
                          Previous
                        </button>
                        <button
                          onClick={() => setDayworksPage(p => p + 1)}
                          disabled={dayworksPage >= dayworksTotalPages - 1}
                          className="px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50"
                        >
                          Next
                        </button>
                      </div>
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          {activeTab === 'retention' && (
            <div>
              {loading ? (
                <LoadingSpinner />
              ) : retentions.length === 0 ? (
                <div className="text-center py-12 text-slate-500">
                  <p>No retentions found</p>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="text-left text-xs font-medium text-slate-500 uppercase tracking-wider">
                        <th className="pb-3 pr-4">Contract</th>
                        <th className="pb-3 pr-4">Contract Value</th>
                        <th className="pb-3 pr-4">Defects End</th>
                        <th className="pb-3 pr-4">Retention %</th>
                        <th className="pb-3 pr-4 text-right">Held</th>
                        <th className="pb-3 pr-4">Status</th>
                        <th className="pb-3 pr-4 text-right">Release</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                      {retentions.map(r => {
                        const approachingRelease = isApproachingRelease(r.defectsEndDate);
                        const daysUntil = getDaysUntilRelease(r.defectsEndDate);
                        return (
                          <tr key={r.id} className={`hover:bg-gray-50 ${approachingRelease ? 'bg-amber-50' : ''}`}>
                            <td className="py-3 pr-4">
                              <div className="font-medium text-slate-800">{r.contractRef}</div>
                              <div className="text-xs text-slate-400 truncate max-w-48">{r.contractTitle}</div>
                            </td>
                            <td className="py-3 pr-4 text-right text-slate-600">{formatCurrency(r.contractValue)}</td>
                            <td className="py-3 pr-4">
                              <div className="text-sm text-slate-600">{formatDate(r.defectsEndDate)}</div>
                              {approachingRelease && daysUntil !== null && (
                                <div className="text-xs text-amber-600 font-medium">
                                  {daysUntil} days remaining
                                </div>
                              )}
                            </td>
                            <td className="py-3 pr-4 text-slate-600">{r.retentionPercent}%</td>
                            <td className="py-3 pr-4 text-right font-medium text-slate-800">{formatCurrency(r.retentionHeld)}</td>
                            <td className="py-3 pr-4">
                              <StatusBadge status={r.status} colors={RETENTION_STATUS_COLORS} />
                            </td>
                            <td className="py-3 pr-4 text-right">
                              {r.releaseAmount ? formatCurrency(r.releaseAmount) : '—'}
                              {r.releaseDate && (
                                <div className="text-xs text-slate-400">{formatDate(r.releaseDate)}</div>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {showAppModal && (
        <Modal title={appForm.id ? 'Edit Application' : 'New Application'} onClose={() => setShowAppModal(false)}>
          <form onSubmit={handleAppSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Contract">
                <Select
                  value={appForm.contractId}
                  onChange={(e) => setAppForm({ ...appForm, contractId: e.target.value })}
                  required
                >
                  <option value="">Select contract...</option>
                  {contracts.map(c => (
                    <option key={c.id} value={c.id}>{c.contractRef || c.title}</option>
                  ))}
                </Select>
              </FormField>
              <FormField label="Application Number">
                <Input
                  value={appForm.applicationNumber}
                  onChange={(e) => setAppForm({ ...appForm, applicationNumber: e.target.value })}
                  placeholder="e.g. App-001"
                  required
                />
              </FormField>
            </div>
            <div className="grid grid-cols-3 gap-4">
              <FormField label="Application Date">
                <Input
                  type="date"
                  value={appForm.applicationDate}
                  onChange={(e) => setAppForm({ ...appForm, applicationDate: e.target.value })}
                  required
                />
              </FormField>
              <FormField label="Period Start">
                <Input
                  type="date"
                  value={appForm.periodStart}
                  onChange={(e) => setAppForm({ ...appForm, periodStart: e.target.value })}
                />
              </FormField>
              <FormField label="Period End">
                <Input
                  type="date"
                  value={appForm.periodEnd}
                  onChange={(e) => setAppForm({ ...appForm, periodEnd: e.target.value })}
                />
              </FormField>
            </div>
            <FormField label="Notes">
              <Textarea
                value={appForm.notes}
                onChange={(e) => setAppForm({ ...appForm, notes: e.target.value })}
                placeholder="Additional notes..."
              />
            </FormField>
            <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
              <button
                type="button"
                onClick={() => setShowAppModal(false)}
                className="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-gray-100 rounded-lg"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={saving}
                className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {saving ? 'Saving...' : 'Save Application'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {showVariationModal && (
        <Modal title={variationForm.id ? 'Edit Variation' : 'New Variation'} size="max-w-3xl" onClose={() => setShowVariationModal(false)}>
          <form onSubmit={handleVariationSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Contract">
                <Select
                  value={variationForm.contractId}
                  onChange={(e) => setVariationForm({ ...variationForm, contractId: e.target.value })}
                  required
                >
                  <option value="">Select contract...</option>
                  {contracts.map(c => (
                    <option key={c.id} value={c.id}>{c.contractRef || c.title}</option>
                  ))}
                </Select>
              </FormField>
              <FormField label="Variation Number">
                <Input
                  value={variationForm.variationNumber}
                  onChange={(e) => setVariationForm({ ...variationForm, variationNumber: e.target.value })}
                  placeholder="e.g. V-001"
                />
              </FormField>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Reference">
                <Input
                  value={variationForm.reference}
                  onChange={(e) => setVariationForm({ ...variationForm, reference: e.target.value })}
                  placeholder="External reference"
                />
              </FormField>
              <FormField label="Instruction Type">
                <Select
                  value={variationForm.instructionType}
                  onChange={(e) => setVariationForm({ ...variationForm, instructionType: e.target.value })}
                >
                  {instructionTypes.map(t => (
                    <option key={t.value} value={t.value}>{t.label}</option>
                  ))}
                </Select>
              </FormField>
            </div>
            <FormField label="Description">
              <Textarea
                value={variationForm.description}
                onChange={(e) => setVariationForm({ ...variationForm, description: e.target.value })}
                placeholder="Variation description..."
                required
              />
            </FormField>
            <div className="grid grid-cols-4 gap-4">
              <FormField label="Proposed Value">
                <Input
                  type="number"
                  step="0.01"
                  value={variationForm.value}
                  onChange={(e) => setVariationForm({ ...variationForm, value: e.target.value })}
                  placeholder="0.00"
                />
              </FormField>
              <FormField label="Approved Value">
                <Input
                  type="number"
                  step="0.01"
                  value={variationForm.approvedValue}
                  onChange={(e) => setVariationForm({ ...variationForm, approvedValue: e.target.value })}
                  placeholder="0.00"
                />
              </FormField>
              <FormField label="Instruction Date">
                <Input
                  type="date"
                  value={variationForm.instructionDate}
                  onChange={(e) => setVariationForm({ ...variationForm, instructionDate: e.target.value })}
                />
              </FormField>
              <FormField label="Approved Date">
                <Input
                  type="date"
                  value={variationForm.approvedDate}
                  onChange={(e) => setVariationForm({ ...variationForm, approvedDate: e.target.value })}
                />
              </FormField>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Status">
                <Select
                  value={variationForm.status}
                  onChange={(e) => setVariationForm({ ...variationForm, status: e.target.value })}
                >
                  <option value="PROPOSED">Proposed</option>
                  <option value="APPROVED">Approved</option>
                  <option value="REJECTED">Rejected</option>
                  <option value="INSTRUCTED">Instructed</option>
                  <option value="VALUED">Valued</option>
                </Select>
              </FormField>
              <FormField label="Notes">
                <Textarea
                  value={variationForm.notes}
                  onChange={(e) => setVariationForm({ ...variationForm, notes: e.target.value })}
                  placeholder="Additional notes..."
                />
              </FormField>
            </div>
            <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
              <button
                type="button"
                onClick={() => setShowVariationModal(false)}
                className="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-gray-100 rounded-lg"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={saving}
                className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {saving ? 'Saving...' : 'Save Variation'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {showDayworkModal && (
        <Modal title="New Daywork Entry" onClose={() => setShowDayworkModal(false)}>
          <form onSubmit={handleDayworkSubmit} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Contract">
                <Select
                  value={dayworkForm.contractId}
                  onChange={(e) => setDayworkForm({ ...dayworkForm, contractId: e.target.value })}
                  required
                >
                  <option value="">Select contract...</option>
                  {contracts.map(c => (
                    <option key={c.id} value={c.id}>{c.contractRef || c.title}</option>
                  ))}
                </Select>
              </FormField>
              <FormField label="Ticket Date">
                <Input
                  type="date"
                  value={dayworkForm.ticketDate}
                  onChange={(e) => setDayworkForm({ ...dayworkForm, ticketDate: e.target.value })}
                  required
                />
              </FormField>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Operative Name">
                <Input
                  value={dayworkForm.operativeName}
                  onChange={(e) => setDayworkForm({ ...dayworkForm, operativeName: e.target.value })}
                  placeholder="Name of operative"
                />
              </FormField>
              <FormField label="Trade">
                <Input
                  value={dayworkForm.trade}
                  onChange={(e) => setDayworkForm({ ...dayworkForm, trade: e.target.value })}
                  placeholder="e.g. Bricklayer"
                />
              </FormField>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Plant Description">
                <Input
                  value={dayworkForm.plantDescription}
                  onChange={(e) => setDayworkForm({ ...dayworkForm, plantDescription: e.target.value })}
                  placeholder="If plant, describe equipment"
                />
              </FormField>
              <FormField label="Hours">
                <Input
                  type="number"
                  step="0.25"
                  value={dayworkForm.hours}
                  onChange={(e) => setDayworkForm({ ...dayworkForm, hours: e.target.value })}
                  placeholder="0.00"
                  required
                />
              </FormField>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Hourly Rate">
                <Input
                  type="number"
                  step="0.01"
                  value={dayworkForm.hourlyRate}
                  onChange={(e) => setDayworkForm({ ...dayworkForm, hourlyRate: e.target.value })}
                  placeholder="0.00"
                  required
                />
              </FormField>
              <FormField label="Description">
                <Input
                  value={dayworkForm.description}
                  onChange={(e) => setDayworkForm({ ...dayworkForm, description: e.target.value })}
                  placeholder="Work description"
                />
              </FormField>
            </div>
            <FormField label="Notes">
              <Textarea
                value={dayworkForm.notes}
                onChange={(e) => setDayworkForm({ ...dayworkForm, notes: e.target.value })}
                placeholder="Additional notes..."
              />
            </FormField>
            <div className="flex justify-end gap-3 pt-4 border-t border-gray-200">
              <button
                type="button"
                onClick={() => setShowDayworkModal(false)}
                className="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-gray-100 rounded-lg"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={saving}
                className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50"
              >
                {saving ? 'Saving...' : 'Save Daywork'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {detailApplication && (
        <Modal title={`Application ${detailApplication.applicationNumber}`} size="max-w-4xl" onClose={() => setDetailApplication(null)}>
          <div className="space-y-6">
            <div className="grid grid-cols-3 gap-4">
              <div>
                <p className="text-sm text-slate-500">Contract</p>
                <p className="font-medium text-slate-800">{detailApplication.contractRef}</p>
                <p className="text-xs text-slate-400">{detailApplication.contractTitle}</p>
              </div>
              <div>
                <p className="text-sm text-slate-500">Status</p>
                <StatusBadge status={detailApplication.status} colors={APPLICATION_STATUS_COLORS} />
              </div>
              <div>
                <p className="text-sm text-slate-500">Application Date</p>
                <p className="font-medium text-slate-800">{formatDate(detailApplication.applicationDate)}</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-slate-500">Period</p>
                <p className="font-medium text-slate-800">
                  {formatDate(detailApplication.periodStart)} to {formatDate(detailApplication.periodEnd)}
                </p>
              </div>
              <div>
                <p className="text-sm text-slate-500">Submitted Date</p>
                <p className="font-medium text-slate-800">{formatDate(detailApplication.submittedDate)}</p>
              </div>
            </div>

            <div className="grid grid-cols-4 gap-4 bg-gray-50 p-4 rounded-lg">
              <div>
                <p className="text-sm text-slate-500">Gross Value</p>
                <p className="text-lg font-bold text-slate-800">{formatCurrency(detailApplication.grossValue)}</p>
              </div>
              <div>
                <p className="text-sm text-slate-500">Retention</p>
                <p className="text-lg font-bold text-slate-800">{formatCurrency(detailApplication.retention)}</p>
              </div>
              <div>
                <p className="text-sm text-slate-500">Net Value</p>
                <p className="text-lg font-bold text-slate-800">{formatCurrency(detailApplication.netValue)}</p>
              </div>
              <div>
                <p className="text-sm text-slate-500">Certified</p>
                <p className="text-lg font-bold text-green-600">{formatCurrency(detailApplication.certifiedValue)}</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-slate-500 mb-2">Payment Notice Date</p>
                <Input
                  type="date"
                  value={detailApplication.paymentNoticeDate?.split('T')[0] || ''}
                  onChange={(e) => handleUpdatePaymentDates(detailApplication, 'paymentNoticeDate', e.target.value)}
                />
              </div>
              <div>
                <p className="text-sm text-slate-500 mb-2">Pay Less Notice Date</p>
                <Input
                  type="date"
                  value={detailApplication.payLessNoticeDate?.split('T')[0] || ''}
                  onChange={(e) => handleUpdatePaymentDates(detailApplication, 'payLessNoticeDate', e.target.value)}
                />
              </div>
            </div>

            {detailApplication.notes && (
              <div>
                <p className="text-sm text-slate-500 mb-1">Notes</p>
                <p className="text-sm text-slate-700 bg-gray-50 p-3 rounded-lg">{detailApplication.notes}</p>
              </div>
            )}

            {detailApplication.lineItems && detailApplication.lineItems.length > 0 && (
              <div>
                <p className="text-sm font-medium text-slate-700 mb-2">Line Items</p>
                <div className="border border-gray-200 rounded-lg overflow-hidden">
                  <table className="w-full text-sm">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-3 py-2 text-left text-xs font-medium text-slate-500">BOQ Item</th>
                        <th className="px-3 py-2 text-left text-xs font-medium text-slate-500">Description</th>
                        <th className="px-3 py-2 text-right text-xs font-medium text-slate-500">Prev Qty</th>
                        <th className="px-3 py-2 text-right text-xs font-medium text-slate-500">This Qty</th>
                        <th className="px-3 py-2 text-right text-xs font-medium text-slate-500">Total Qty</th>
                        <th className="px-3 py-2 text-right text-xs font-medium text-slate-500">Rate</th>
                        <th className="px-3 py-2 text-right text-xs font-medium text-slate-500">Gross</th>
                        <th className="px-3 py-2 text-right text-xs font-medium text-slate-500">Retention</th>
                        <th className="px-3 py-2 text-right text-xs font-medium text-slate-500">Net</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-100">
                      {detailApplication.lineItems.map((item, idx) => (
                        <tr key={idx}>
                          <td className="px-3 py-2 text-slate-600">{item.boqItem}</td>
                          <td className="px-3 py-2 text-slate-600 truncate max-w-32">{item.description}</td>
                          <td className="px-3 py-2 text-right text-slate-600">{item.previousQuantity}</td>
                          <td className="px-3 py-2 text-right text-slate-600">{item.thisQuantity}</td>
                          <td className="px-3 py-2 text-right text-slate-600">{item.totalQuantity}</td>
                          <td className="px-3 py-2 text-right text-slate-600">{formatCurrency(item.rate)}</td>
                          <td className="px-3 py-2 text-right font-medium text-slate-800">{formatCurrency(item.grossValue)}</td>
                          <td className="px-3 py-2 text-right text-slate-600">{formatCurrency(item.retention)}</td>
                          <td className="px-3 py-2 text-right font-medium text-slate-800">{formatCurrency(item.netValue)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            <div className="flex justify-end pt-4 border-t border-gray-200">
              <button
                onClick={() => setDetailApplication(null)}
                className="px-4 py-2 text-sm font-medium text-slate-600 hover:bg-gray-100 rounded-lg"
              >
                Close
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
}
