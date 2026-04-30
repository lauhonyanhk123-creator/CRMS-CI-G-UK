import { useEffect, useState, useCallback } from 'react';
import axios from 'axios';

const API = '/api/v1';

const TABS = [
  { id: 'cvr', label: 'CVR Report' },
  { id: 'cashflow', label: 'Cashflow Forecast' },
  { id: 'retention', label: 'Retention Schedule' },
  { id: 'cis', label: 'CIS Summary' },
];

const STATUS_COLORS = {
  ACTIVE: 'bg-green-100 text-green-700',
  DRAFT: 'bg-gray-100 text-gray-700',
  COMPLETED: 'bg-blue-100 text-blue-700',
  TERMINATED: 'bg-red-100 text-red-700',
  SUSPENDED: 'bg-amber-100 text-amber-700',
};

function StatCard({ label, value, icon, color = 'blue' }) {
  const colors = {
    blue: 'bg-blue-50 text-blue-700',
    green: 'bg-green-50 text-green-700',
    amber: 'bg-amber-50 text-amber-700',
    purple: 'bg-purple-50 text-purple-700',
    slate: 'bg-slate-50 text-slate-700',
  };
  return (
    <div className="bg-white rounded-xl p-5 shadow-sm border border-gray-100">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-slate-500">{label}</p>
          <p className="text-2xl font-bold mt-1 text-slate-800">{value ?? '—'}</p>
        </div>
        <div className={`w-12 h-12 rounded-lg flex items-center justify-center text-xl ${colors[color] || colors.blue}`}>
          {icon}
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

function BarChart({ data, maxValue }) {
  const max = maxValue || Math.max(...data.map(d => d.amount), 1);
  return (
    <div className="flex items-end justify-between gap-2 h-64 px-2">
      {data.map((item, i) => {
        const height = max > 0 ? (item.amount / max) * 100 : 0;
        return (
          <div key={i} className="flex flex-col items-center flex-1">
            <div className="relative w-full flex items-end justify-center h-48">
              <div
                className="w-full max-w-12 bg-blue-500 rounded-t-md hover:bg-blue-600 transition-colors relative group"
                style={{ height: `${Math.max(height, 2)}%` }}
              >
                <div className="absolute -top-8 left-1/2 -translate-x-1/2 bg-slate-800 text-white text-xs px-2 py-1 rounded opacity-0 group-hover:opacity-100 whitespace-nowrap">
                  £{item.amount?.toLocaleString()}
                </div>
              </div>
            </div>
            <p className="text-xs text-slate-500 mt-2 text-center">{item.month}</p>
          </div>
        );
      })}
    </div>
  );
}

export default function Reports() {
  const [activeTab, setActiveTab] = useState('cvr');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [contracts, setContracts] = useState([]);
  const [cvrData, setCvrData] = useState([]);
  const [selectedContract, setSelectedContract] = useState('');
  const [cvrPeriod, setCvrPeriod] = useState('');

  const [cashflowData, setCashflowData] = useState(null);
  const [cashflowFrom, setCashflowFrom] = useState('');
  const [cashflowTo, setCashflowTo] = useState('');

  const [retentionData, setRetentionData] = useState([]);

  const [cisMonth, setCisMonth] = useState('');
  const [cisData, setCisData] = useState(null);

  const [stats, setStats] = useState({
    totalForecast: null,
    retentionHeld: null,
    openRetentions: null,
    cisMtd: null,
  });

  const loadContracts = useCallback(async () => {
    try {
      const { data } = await axios.get(`${API}/contracts`, { params: { page: 0, size: 100 } });
      const payload = data.data || data;
      setContracts(payload.content || payload || []);
    } catch (err) {
      console.error('Failed to load contracts', err);
    }
  }, []);

  useEffect(() => {
    loadContracts();
  }, [loadContracts]);

  useEffect(() => {
    if (activeTab === 'retention') {
      fetchRetention();
    } else if (activeTab === 'cis' && cisMonth) {
      fetchCisSummary();
    }
  }, [activeTab]);

  const fetchCvr = async () => {
    if (!selectedContract || !cvrPeriod) {
      setError('Please select a contract and period');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const { data } = await axios.get(`${API}/reports/cvr`, {
        params: { contract: selectedContract, period: cvrPeriod },
      });
      const payload = Array.isArray(data) ? data : (data.data || []);
      setCvrData(payload);
    } catch (err) {
      setError('Failed to load CVR report');
      setCvrData([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchCashflow = async () => {
    if (!cashflowFrom || !cashflowTo) {
      setError('Please select date range');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const { data } = await axios.get(`${API}/reports/cashflow`, {
        params: { from: cashflowFrom, to: cashflowTo },
      });
      const payload = data.data || data;
      setCashflowData(payload);
      setStats(s => ({
        ...s,
        totalForecast: payload.totalForecast,
        retentionHeld: payload.retentionHeld,
        retentionReleasable: payload.retentionReleasable,
      }));
    } catch (err) {
      setError('Failed to load cashflow forecast');
      setCashflowData(null);
    } finally {
      setLoading(false);
    }
  };

  const fetchRetention = async () => {
    setLoading(true);
    try {
      const { data } = await axios.get(`${API}/reports/retention-schedule`);
      const payload = Array.isArray(data) ? data : (data.data || []);
      setRetentionData(payload);
      setStats(s => ({
        ...s,
        retentionHeld: payload.reduce((sum, r) => sum + (r.retentionHeld || 0), 0),
        openRetentions: payload.filter(r => r.status !== 'COMPLETED').length,
      }));
    } catch (err) {
      setError('Failed to load retention schedule');
      setRetentionData([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchCisSummary = async () => {
    if (!cisMonth) return;
    setLoading(true);
    try {
      const { data } = await axios.get(`${API}/reports/cis-summary`, {
        params: { taxMonth: cisMonth },
      });
      const payload = data.data || data;
      setCisData(payload);
      setStats(s => ({
        ...s,
        cisMtd: payload.totalDeducted || payload.total || 0,
      }));
    } catch (err) {
      setError('Failed to load CIS summary');
      setCisData(null);
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (val) => {
    if (val == null) return '—';
    return `£${parseFloat(val).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  };

  const formatPercent = (val) => {
    if (val == null) return '—';
    return `${parseFloat(val).toFixed(2)}%`;
  };

  const getVarianceColor = (variance) => {
    if (variance == null) return 'text-slate-600';
    if (variance < 0) return 'text-red-600 bg-red-50';
    if (variance > 0) return 'text-green-600';
    return 'text-slate-600';
  };

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-bold text-slate-800">Reports</h2>
        <p className="text-sm text-slate-500 mt-1">Financial and operational reports</p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard
          label="Total Forecast"
          value={stats.totalForecast != null ? formatCurrency(stats.totalForecast) : '—'}
          icon="📊"
          color="blue"
        />
        <StatCard
          label="Retention Held"
          value={stats.retentionHeld != null ? formatCurrency(stats.retentionHeld) : '—'}
          icon="🔒"
          color="amber"
        />
        <StatCard
          label="Open Retentions"
          value={stats.openRetentions != null ? stats.openRetentions : '—'}
          icon="📋"
          color="purple"
        />
        <StatCard
          label="CIS MTD"
          value={stats.cisMtd != null ? formatCurrency(stats.cisMtd) : '—'}
          icon="🏗️"
          color="green"
        />
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 mb-6">
        <div className="flex border-b border-gray-100">
          {TABS.map(tab => (
            <button
              key={tab.id}
              onClick={() => { setActiveTab(tab.id); setError(null); }}
              className={`px-6 py-3 text-sm font-medium transition-colors ${
                activeTab === tab.id
                  ? 'text-blue-600 border-b-2 border-blue-600 bg-blue-50/50'
                  : 'text-slate-500 hover:text-slate-700 hover:bg-gray-50'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        <div className="p-6">
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
              {error}
            </div>
          )}

          {activeTab === 'cvr' && (
            <div>
              <div className="flex flex-wrap gap-4 mb-6">
                <div className="flex-1 min-w-48">
                  <label className="block text-sm font-medium text-slate-700 mb-1">Contract</label>
                  <select
                    value={selectedContract}
                    onChange={(e) => setSelectedContract(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
                  >
                    <option value="">Select contract…</option>
                    {contracts.map(c => (
                      <option key={c.id} value={c.id}>{c.contractRef || c.title}</option>
                    ))}
                  </select>
                </div>
                <div className="flex-1 min-w-40">
                  <label className="block text-sm font-medium text-slate-700 mb-1">Period (YYYY-MM)</label>
                  <input
                    type="month"
                    value={cvrPeriod}
                    onChange={(e) => setCvrPeriod(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div className="flex items-end">
                  <button
                    onClick={fetchCvr}
                    disabled={loading}
                    className="px-5 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
                  >
                    {loading ? 'Loading…' : 'Generate'}
                  </button>
                </div>
              </div>

              {cvrData.length > 0 ? (
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="bg-slate-50 border-b border-gray-100">
                        {['Ref', 'Title', 'Client', 'Period', 'App #', 'Gross', 'Retention', 'Net', 'Cumulative Gross', 'Cumulative Ret', 'Certified', 'Variance'].map(h => (
                          <th key={h} className="text-left px-4 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide whitespace-nowrap">{h}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-50">
                      {cvrData.map((item, i) => (
                        <tr key={i} className="hover:bg-slate-50/50 transition-colors">
                          <td className="px-4 py-3 text-sm font-mono text-slate-600 whitespace-nowrap">{item.contractRef || '—'}</td>
                          <td className="px-4 py-3 text-sm font-medium text-slate-800 max-w-32 truncate">{item.contractTitle || '—'}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 max-w-24 truncate">{item.client || '—'}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 whitespace-nowrap">{item.period || '—'}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 text-center">{item.applicationNumber || '—'}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 text-right whitespace-nowrap">{formatCurrency(item.grossValue)}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 text-right whitespace-nowrap">{formatCurrency(item.retention)}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 text-right whitespace-nowrap">{formatCurrency(item.netValue)}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 text-right whitespace-nowrap">{formatCurrency(item.cumulativeGrossValue)}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 text-right whitespace-nowrap">{formatCurrency(item.cumulativeRetention)}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 text-right whitespace-nowrap">{formatCurrency(item.certifiedValue)}</td>
                          <td className={`px-4 py-3 text-sm text-right font-medium whitespace-nowrap rounded ${getVarianceColor(item.variance)}`}>
                            {formatCurrency(item.variance)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : !loading && (
                <div className="text-center py-12 text-slate-400">
                  Select a contract and period, then click Generate to view CVR report
                </div>
              )}
            </div>
          )}

          {activeTab === 'cashflow' && (
            <div>
              <div className="flex flex-wrap gap-4 mb-6">
                <div className="flex-1 min-w-40">
                  <label className="block text-sm font-medium text-slate-700 mb-1">From Date</label>
                  <input
                    type="date"
                    value={cashflowFrom}
                    onChange={(e) => setCashflowFrom(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div className="flex-1 min-w-40">
                  <label className="block text-sm font-medium text-slate-700 mb-1">To Date</label>
                  <input
                    type="date"
                    value={cashflowTo}
                    onChange={(e) => setCashflowTo(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div className="flex items-end">
                  <button
                    onClick={fetchCashflow}
                    disabled={loading}
                    className="px-5 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
                  >
                    {loading ? 'Loading…' : 'Generate'}
                  </button>
                </div>
              </div>

              {cashflowData ? (
                <div>
                  <div className="grid grid-cols-3 gap-4 mb-6">
                    <div className="bg-slate-50 rounded-lg p-4 text-center">
                      <p className="text-xs text-slate-500 uppercase tracking-wide">Total Forecast</p>
                      <p className="text-xl font-bold text-slate-800 mt-1">{formatCurrency(cashflowData.totalForecast)}</p>
                    </div>
                    <div className="bg-amber-50 rounded-lg p-4 text-center">
                      <p className="text-xs text-slate-500 uppercase tracking-wide">Retention Held</p>
                      <p className="text-xl font-bold text-amber-700 mt-1">{formatCurrency(cashflowData.retentionHeld)}</p>
                    </div>
                    <div className="bg-green-50 rounded-lg p-4 text-center">
                      <p className="text-xs text-slate-500 uppercase tracking-wide">Retention Releasable</p>
                      <p className="text-xl font-bold text-green-700 mt-1">{formatCurrency(cashflowData.retentionReleasable)}</p>
                    </div>
                  </div>

                  {cashflowData.byMonth && cashflowData.byMonth.length > 0 ? (
                    <div className="mt-6">
                      <BarChart
                        data={cashflowData.byMonth}
                        maxValue={Math.max(...cashflowData.byMonth.map(d => d.amount))}
                      />
                    </div>
                  ) : (
                    <div className="text-center py-8 text-slate-400">No monthly data available</div>
                  )}
                </div>
              ) : !loading && (
                <div className="text-center py-12 text-slate-400">
                  Select date range and click Generate to view cashflow forecast
                </div>
              )}
            </div>
          )}

          {activeTab === 'retention' && (
            <div>
              <div className="flex items-center justify-between mb-4">
                <p className="text-sm text-slate-500">{retentionData.length} contracts in retention schedule</p>
                <button
                  onClick={fetchRetention}
                  disabled={loading}
                  className="px-4 py-2 text-sm text-slate-600 border rounded-lg hover:bg-gray-50 disabled:opacity-50"
                >
                  Refresh
                </button>
              </div>

              {retentionData.length > 0 ? (
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="bg-slate-50 border-b border-gray-100">
                        {['Contract Ref', 'Title', 'Client', 'Contract Value', 'Defects End Date', 'Retention %', 'Retention Held', 'Status'].map(h => (
                          <th key={h} className="text-left px-4 py-3 text-xs font-semibold text-slate-500 uppercase tracking-wide whitespace-nowrap">{h}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-50">
                      {retentionData.map((item, i) => (
                        <tr key={i} className="hover:bg-slate-50/50 transition-colors">
                          <td className="px-4 py-3 text-sm font-mono text-slate-600 whitespace-nowrap">{item.contractRef || '—'}</td>
                          <td className="px-4 py-3 text-sm font-medium text-slate-800 max-w-32 truncate">{item.title || '—'}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 max-w-24 truncate">{item.client || '—'}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 text-right whitespace-nowrap">{formatCurrency(item.contractValue)}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 whitespace-nowrap">{item.defectsEndDate || '—'}</td>
                          <td className="px-4 py-3 text-sm text-slate-600 text-right whitespace-nowrap">{formatPercent(item.retentionPercent)}</td>
                          <td className="px-4 py-3 text-sm text-amber-600 font-medium text-right whitespace-nowrap">{formatCurrency(item.retentionHeld)}</td>
                          <td className="px-4 py-3">
                            <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${STATUS_COLORS[item.status] || 'bg-gray-100 text-gray-600'}`}>
                              {item.status || '—'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : !loading && (
                <div className="text-center py-12 text-slate-400">
                  No retention schedule data available
                </div>
              )}
            </div>
          )}

          {activeTab === 'cis' && (
            <div>
              <div className="flex flex-wrap gap-4 mb-6">
                <div className="flex-1 min-w-40">
                  <label className="block text-sm font-medium text-slate-700 mb-1">Tax Month (YYYY-MM)</label>
                  <input
                    type="month"
                    value={cisMonth}
                    onChange={(e) => setCisMonth(e.target.value)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <div className="flex items-end">
                  <button
                    onClick={fetchCisSummary}
                    disabled={loading || !cisMonth}
                    className="px-5 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:opacity-50 transition-colors"
                  >
                    {loading ? 'Loading…' : 'Generate'}
                  </button>
                </div>
              </div>

              {cisData ? (
                <div className="space-y-6">
                  <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                    <div className="bg-blue-50 rounded-lg p-4">
                      <p className="text-xs text-slate-500 uppercase tracking-wide">Total Gross</p>
                      <p className="text-lg font-bold text-slate-800 mt-1">{formatCurrency(cisData.totalGross || cisData.grossValue)}</p>
                    </div>
                    <div className="bg-amber-50 rounded-lg p-4">
                      <p className="text-xs text-slate-500 uppercase tracking-wide">Total Deducted</p>
                      <p className="text-lg font-bold text-amber-700 mt-1">{formatCurrency(cisData.totalDeducted || cisData.deducted)}</p>
                    </div>
                    <div className="bg-green-50 rounded-lg p-4">
                      <p className="text-xs text-slate-500 uppercase tracking-wide">Net Paid</p>
                      <p className="text-lg font-bold text-green-700 mt-1">{formatCurrency(cisData.netPaid || cisData.netValue)}</p>
                    </div>
                    <div className="bg-purple-50 rounded-lg p-4">
                      <p className="text-xs text-slate-500 uppercase tracking-wide">CIS Rate</p>
                      <p className="text-lg font-bold text-purple-700 mt-1">{formatPercent(cisData.cisRate || cisData.rate)}</p>
                    </div>
                  </div>

                  {cisData.subcontractors && cisData.subcontractors.length > 0 && (
                    <div>
                      <h4 className="text-sm font-semibold text-slate-700 mb-3">Subcontractor Deductions</h4>
                      <table className="w-full">
                        <thead>
                          <tr className="bg-slate-50 border-b border-gray-100">
                            <th className="text-left px-4 py-2 text-xs font-semibold text-slate-500 uppercase">Name</th>
                            <th className="text-left px-4 py-2 text-xs font-semibold text-slate-500 uppercase">Reference</th>
                            <th className="text-right px-4 py-2 text-xs font-semibold text-slate-500 uppercase">Gross</th>
                            <th className="text-right px-4 py-2 text-xs font-semibold text-slate-500 uppercase">Deduction</th>
                            <th className="text-right px-4 py-2 text-xs font-semibold text-slate-500 uppercase">Net</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-50">
                          {cisData.subcontractors.map((sc, i) => (
                            <tr key={i} className="hover:bg-slate-50/50">
                              <td className="px-4 py-3 text-sm text-slate-700">{sc.name || '—'}</td>
                              <td className="px-4 py-3 text-sm font-mono text-slate-600">{sc.reference || sc.cisReference || '—'}</td>
                              <td className="px-4 py-3 text-sm text-slate-600 text-right">{formatCurrency(sc.gross)}</td>
                              <td className="px-4 py-3 text-sm text-amber-600 text-right font-medium">{formatCurrency(sc.deduction)}</td>
                              <td className="px-4 py-3 text-sm text-slate-600 text-right">{formatCurrency(sc.net)}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              ) : !loading && (
                <div className="text-center py-12 text-slate-400">
                  Select a tax month and click Generate to view CIS summary
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
