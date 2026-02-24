import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AdminLayout from './layouts/AdminLayout';
import Dashboard from './pages/Dashboard';
import Events from './pages/Events';
import UsersPage from './pages/Users';
import Verifications from './pages/Verifications';
import Alerts from './pages/Alerts';
import Colleges from './pages/Colleges';
import Marketplace from './pages/Marketplace';
import Login from './pages/Login';

// Placeholder pages to be implemented
const PlaceholderPage = ({ title }: { title: string }) => (
  <div className="flex items-center justify-center min-h-[50vh]">
    <div className="text-center p-12 bg-slate-900/50 rounded-3xl border border-slate-800 backdrop-blur-sm shadow-xl">
      <h2 className="text-3xl font-bold mb-4">{title}</h2>
      <p className="text-slate-400 max-w-md mx-auto leading-relaxed">
        This portal module is currently being finalized. Check back soon for full database management capabilities.
      </p>
    </div>
  </div>
);

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Auth Route */}
        <Route path="/login" element={<Login />} />

        {/* Protected Dashboard Routes */}
        <Route path="/" element={<AdminLayout />}>
          <Route index element={<Dashboard />} />
          <Route path="users" element={<UsersPage />} />
          <Route path="events" element={<Events />} />
          <Route path="verifications" element={<Verifications />} />
          <Route path="notifications" element={<Alerts />} />
          <Route path="colleges" element={<Colleges />} />
          <Route path="marketplace" element={<Marketplace />} />
          <Route path="settings" element={<PlaceholderPage title="Portal Settings" />} />
        </Route>

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
