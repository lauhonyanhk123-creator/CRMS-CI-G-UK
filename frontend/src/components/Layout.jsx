import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

const navItems = [
  { to: '/', label: 'Dashboard', icon: '📊' },
  { to: '/contracts', label: 'Contracts', icon: '📄' },
  { to: '/sites', label: 'Sites', icon: '📍' },
  { to: '/companies', label: 'Companies', icon: '🏢' },
  { to: '/tenders', label: 'Tenders', icon: '📋' },
  { to: '/health-safety', label: 'Health & Safety', icon: '🦺' },
  { to: '/operatives', label: 'Operatives', icon: '👷' },
  { to: '/plant', label: 'Plant & Equipment', icon: '🚜' },
  { to: '/subcontractors', label: 'Subcontractors', icon: '🏗️' },
  { to: '/materials', label: 'Materials', icon: '🧱' },
  { to: '/reports', label: 'Reports', icon: '📊' },
];

export default function Layout() {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Sidebar */}
      <aside className="w-64 bg-slate-800 text-white flex flex-col">
        <div className="p-6 border-b border-slate-700">
          <h1 className="text-xl font-bold tracking-wide">CRMS</h1>
          <p className="text-xs text-slate-400 mt-1">Contract Management</p>
        </div>
        <nav className="flex-1 p-4 space-y-1">
          {navItems.map(({ to, label, icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-600 text-white'
                    : 'text-slate-300 hover:bg-slate-700 hover:text-white'
                }`
              }
            >
              <span>{icon}</span>
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="p-4 border-t border-slate-700">
          <div className="flex items-center justify-between">
            <div className="text-sm">
              <p className="font-medium">{user?.username || 'User'}</p>
              <p className="text-xs text-slate-400">{user?.role || '—'}</p>
            </div>
            <button
              onClick={handleLogout}
              className="text-xs text-slate-400 hover:text-white transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
