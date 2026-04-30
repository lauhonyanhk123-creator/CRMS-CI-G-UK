import { useEffect, useState } from 'react';
import axios from 'axios';

const API = '/api/v1';

function StatCard({ label, value, icon, color = 'blue' }) {
  const colors = {
    blue: 'bg-blue-50 text-blue-700',
    green: 'bg-green-50 text-green-700',
    amber: 'bg-amber-50 text-amber-700',
    red: 'bg-red-50 text-red-700',
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

export default function Dashboard() {
  const [stats, setStats] = useState({});
  const [recent, setRecent] = useState([]);
  const [expiring, setExpiring] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function load() {
      try {
        const [summaryRes, recentRes, expiringRes] = await Promise.all([
          axios.get(`${API}/dashboard/stats`).catch(() => ({ data: {} })),
          axios.get(`${API}/dashboard/activity-feed?limit=5`).catch(() => ({ data: [] })),
          axios.get(`${API}/dashboard/expiring-items?days=90`).catch(() => ({ data: {} })),
        ]);
        setStats(summaryRes.data.data || summaryRes.data || {});
        setRecent(recentRes.data.data || recentRes.data || []);
        setExpiring(expiringRes.data.data || expiringRes.data || null);
      } catch (err) {
        setError('Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  if (loading) return <div className="p-8 text-slate-500">Loading dashboard…</div>;
  if (error) return <div className="p-8 text-red-500">{error}</div>;

  return (
    <div className="p-8">
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-slate-800">Dashboard</h2>
        <p className="text-sm text-slate-500 mt-1">Overview of your contract management system</p>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard label="Active Contracts" value={stats.activeContracts} icon="📄" color="blue" />
        <StatCard label="Active Sites" value={stats.activeSites} icon="📍" color="green" />
        <StatCard label="Open Tenders" value={stats.openTenders} icon="📋" color="amber" />
        <StatCard label="Pending Applications" value={stats.pendingApplications} icon="💰" color="purple" />
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <SectionCard title="Tender Pipeline">
          {stats.pipelineSummary ? (
            <div className="space-y-3">
              {Object.entries(stats.pipelineSummary).map(([stage, count]) => (
                <div key={stage} className="flex justify-between items-center">
                  <span className="text-sm text-slate-600">{stage}</span>
                  <span className="text-sm font-semibold text-slate-800">{count}</span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-slate-400">No pipeline data available</p>
          )}
        </SectionCard>

        <SectionCard title="Contract Summary">
          {stats.contractSummary ? (
            <div className="space-y-3">
              {Object.entries(stats.contractSummary).map(([status, count]) => (
                <div key={status} className="flex justify-between items-center">
                  <span className="text-sm text-slate-600">{status}</span>
                  <span className="text-sm font-semibold text-slate-800">{count}</span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-slate-400">No contract data available</p>
          )}
        </SectionCard>

        <SectionCard title="Expiring Items">
          {expiring ? (
            <div className="space-y-3">
              {Object.entries(expiring).map(([key, count]) => (
                <div key={key} className="flex justify-between items-center">
                  <span className="text-sm text-slate-600">{key.replace(/([A-Z])/g, ' $1').trim()}</span>
                  <span className={`text-sm font-semibold ${count > 0 ? 'text-amber-600' : 'text-slate-800'}`}>{count}</span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-slate-400">No expiring items</p>
          )}
        </SectionCard>
      </div>

      {/* Recent activity */}
      <SectionCard title="Recent Activity">
        {recent.length > 0 ? (
          <div className="space-y-3">
            {recent.map((item, i) => (
              <div key={i} className="flex items-start gap-3 pb-3 border-b border-gray-50 last:border-0 last:pb-0">
                <div className="w-2 h-2 rounded-full bg-blue-500 mt-2 flex-shrink-0" />
                <div>
                  <p className="text-sm text-slate-700">{item.description || item.action || item.message || JSON.stringify(item)}</p>
                  <p className="text-xs text-slate-400 mt-0.5">{item.timestamp || item.date || item.createdAt || ''}</p>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-slate-400">No recent activity</p>
        )}
      </SectionCard>
    </div>
  );
}
