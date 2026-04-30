import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './store/authStore';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Layout from './components/Layout';
import Contracts from './pages/Contracts';
import Sites from './pages/Sites';
import Companies from './pages/Companies';
import Tenders from './pages/Tenders';
import HealthSafety from './pages/HealthSafety';
import Operatives from './pages/Operatives';
import Plant from './pages/Plant';

function ProtectedRoute({ children }) {
  return useAuthStore.getState().token ? children : <Navigate to="/login" />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/"
          element={
            <ProtectedRoute>
              <Layout />
            </ProtectedRoute>
          }
        >
          <Route index element={<Dashboard />} />
          <Route path="contracts" element={<Contracts />} />
          <Route path="sites" element={<Sites />} />
          <Route path="companies" element={<Companies />} />
          <Route path="tenders" element={<Tenders />} />
          <Route path="health-safety" element={<HealthSafety />} />
          <Route path="operatives" element={<Operatives />} />
          <Route path="plant" element={<Plant />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
