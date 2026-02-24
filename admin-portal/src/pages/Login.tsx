import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldCheck, Lock, Mail, ArrowRight } from 'lucide-react';
import api from '../api';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleLogin = async (e: FormEvent) => {
        e.preventDefault();
        setLoading(true);
        const formData = new FormData();
        formData.append('username', email);
        formData.append('password', password);

        try {
            const res = await api.post('/login/access-token', formData);
            localStorage.setItem('admin_token', res.data.access_token);

            // Verify role
            const userRes = await api.get('/users/me');
            const user = userRes.data;
            if (user.is_superuser || user.role === 'college_admin') {
                navigate('/');
            } else {
                alert('Access Denied: You are not authorized');
                localStorage.removeItem('admin_token');
            }
        } catch (err) {
            alert('Invalid login credentials');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
            <div className="absolute inset-0 overflow-hidden pointer-events-none">
                <div className="absolute -top-[10%] -left-[10%] w-[40%] h-[40%] bg-indigo-600/10 blur-[120px] rounded-full"></div>
                <div className="absolute -bottom-[10%] -right-[10%] w-[40%] h-[40%] bg-pink-600/10 blur-[120px] rounded-full"></div>
            </div>

            <div className="bg-slate-900/50 backdrop-blur-xl p-8 rounded-3xl border border-slate-800 w-full max-w-md shadow-2xl relative z-10">
                <div className="flex justify-center mb-8">
                    <div className="bg-indigo-600 p-4 rounded-2xl shadow-xl shadow-indigo-600/20">
                        <ShieldCheck className="w-10 h-10 text-white" />
                    </div>
                </div>

                <div className="text-center mb-10">
                    <h1 className="text-3xl font-bold text-white mb-2 tracking-tight">Admin Console</h1>
                    <p className="text-slate-400">Secure access to CampusLink backend</p>
                </div>

                <form onSubmit={handleLogin} className="space-y-6">
                    <div className="space-y-2">
                        <label className="text-sm font-medium text-slate-300 ml-1">Work Email</label>
                        <div className="relative group">
                            <Mail className="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 group-focus-within:text-indigo-500 transition-colors" />
                            <input
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                                className="w-full bg-slate-800/50 border border-slate-700 rounded-2xl pl-10 pr-4 py-3.5 outline-none focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 transition-all text-white placeholder:text-slate-600 shadow-inner"
                                placeholder="admin@campuslink.com"
                            />
                        </div>
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-medium text-slate-300 ml-1">Password</label>
                        <div className="relative group">
                            <Lock className="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 group-focus-within:text-indigo-500 transition-colors" />
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                                className="w-full bg-slate-800/50 border border-slate-700 rounded-2xl pl-10 pr-4 py-3.5 outline-none focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 transition-all text-white placeholder:text-slate-600 shadow-inner"
                                placeholder="••••••••"
                            />
                        </div>
                    </div>

                    <button
                        disabled={loading}
                        className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-bold py-4 rounded-2xl transition-all shadow-lg shadow-indigo-600/20 active:scale-[0.98] flex items-center justify-center gap-2"
                    >
                        {loading ? (
                            <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                        ) : (
                            <>
                                Continue to Dashboard
                                <ArrowRight className="w-5 h-5" />
                            </>
                        )}
                    </button>
                </form>

                <p className="mt-10 text-center text-slate-500 text-xs uppercase tracking-widest font-semibold">
                    Protected by CampusLink Security
                </p>
            </div>
        </div>
    );
};

export default Login;
