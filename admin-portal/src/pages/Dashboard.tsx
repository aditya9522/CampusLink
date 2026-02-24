import { useEffect, useState } from 'react';
import { Users, Calendar, Clock, CheckCircle, XCircle, TrendingUp, AlertTriangle } from 'lucide-react';
import api from '../api';

const Dashboard = () => {
    const [stats, setStats] = useState({ users: 0, events: 0, pending: 0, alerts: 3 });
    const [requests, setRequests] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const [eventsRes, usersRes, verRes] = await Promise.all([
                    api.get('/events/'),
                    api.get('/users/'),
                    api.get('/verifications/')
                ]);
                setStats(prev => ({
                    ...prev,
                    events: eventsRes.data.length,
                    users: usersRes.data.length,
                    pending: verRes.data.length
                }));
                setRequests(verRes.data);
            } catch (err) {
                console.error('Failed to fetch dashboard data', err);
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, []);

    const statCards = [
        { label: 'Total Students', value: stats.users, change: '+12%', icon: Users, color: 'text-blue-500', bg: 'bg-blue-500/10' },
        { label: 'Active Events', value: stats.events, change: '+5%', icon: Calendar, color: 'text-green-500', bg: 'bg-green-500/10' },
        { label: 'Pending Verification', value: stats.pending, change: '-2', icon: Clock, color: 'text-amber-500', bg: 'bg-amber-500/10' },
        { label: 'System Alerts', value: stats.alerts, change: 'Action Required', icon: AlertTriangle, color: 'text-red-500', bg: 'bg-red-500/10' },
    ];

    return (
        <div className="space-y-10 animate-in fade-in slide-in-from-bottom-4 duration-700">
            {/* Welcome Banner */}
            <div className="bg-gradient-to-r from-indigo-600 to-purple-600 rounded-3xl p-8 text-white relative overflow-hidden shadow-2xl shadow-indigo-600/20">
                <div className="absolute top-0 right-0 p-8 opacity-20 transform translate-x-10 -translate-y-10 rotate-12">
                    <TrendingUp className="w-64 h-64" />
                </div>
                <div className="relative z-10">
                    <h1 className="text-4xl font-bold mb-3 tracking-tight">System Status: Optimal</h1>
                    <p className="max-w-xl text-indigo-100/80 leading-relaxed font-medium">
                        CampusLink growth is up 24% this month. You have {stats.pending} pending student verifications to review.
                    </p>
                    <button className="mt-8 bg-white text-indigo-600 px-8 py-3 rounded-2xl font-bold hover:shadow-xl transition-all active:scale-95 shadow-lg">
                        View Analytics
                    </button>
                </div>
            </div>

            {/* Stats Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {statCards.map((stat, i) => (
                    <div key={i} className="bg-slate-900/50 backdrop-blur-sm p-6 rounded-3xl border border-slate-800 hover:border-slate-700 hover:bg-slate-800/50 transition-all group shadow-sm">
                        <div className="flex justify-between items-start mb-6">
                            <div className={`p-4 ${stat.bg} ${stat.color} rounded-2xl group-hover:scale-110 transition-transform duration-300`}>
                                <stat.icon className="w-6 h-6" />
                            </div>
                            <span className={`text-[10px] uppercase tracking-widest font-black ${stat.change.includes('+') ? 'text-green-500' : 'text-red-500'} bg-slate-950 px-3 py-1.5 rounded-full border border-slate-800`}>
                                {stat.change}
                            </span>
                        </div>
                        <h3 className="text-4xl font-bold mb-1 tracking-tighter text-white">
                            {loading ? <div className="h-10 w-24 bg-slate-800 rounded-lg animate-pulse"></div> : stat.value.toLocaleString()}
                        </h3>
                        <p className="text-slate-400 text-sm font-medium tracking-wide">{stat.label}</p>
                    </div>
                ))}
            </div>

            {/* Recent Requests */}
            <div className="bg-slate-900/50 backdrop-blur-sm rounded-3xl border border-slate-800 overflow-hidden shadow-sm">
                <div className="p-8 border-b border-slate-800 flex justify-between items-center bg-slate-900/30">
                    <div>
                        <h3 className="font-bold text-xl text-white">Pending verifications</h3>
                        <p className="text-slate-500 text-sm font-medium mt-1">Review student credentials for college access</p>
                    </div>
                    <button className="text-indigo-400 text-sm font-bold bg-indigo-400/10 px-6 py-2.5 rounded-2xl hover:bg-indigo-400/20 transition-all border border-indigo-400/20">
                        Export List
                    </button>
                </div>
                <div className="overflow-x-auto">
                    <table className="w-full text-left">
                        <thead className="bg-slate-900/80 text-slate-500 text-[10px] uppercase font-black tracking-[0.2em] border-b border-slate-800">
                            <tr>
                                <th className="p-8">Student</th>
                                <th>College ID</th>
                                <th>Admission Date</th>
                                <th>Status</th>
                                <th className="text-right p-8">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-800/50">
                            {requests.length === 0 ? (
                                <tr>
                                    <td colSpan={5} className="p-20 text-center text-slate-500 font-medium">No pending verifications</td>
                                </tr>
                            ) : requests.map((req: any) => (
                                <tr key={req.id} className="hover:bg-slate-800/40 transition-colors group">
                                    <td className="p-8 flex items-center gap-4">
                                        <div className={`w-12 h-12 rounded-2xl bg-indigo-600 flex items-center justify-center text-white font-black text-xl shadow-lg shadow-black/20`}>
                                            {req.full_name?.[0] || 'S'}
                                        </div>
                                        <div>
                                            <span className="font-bold text-white block">{req.full_name}</span>
                                            <span className="text-slate-500 text-xs">Verified Email Address</span>
                                        </div>
                                    </td>
                                    <td className="text-slate-400 font-mono text-sm tracking-tighter">USR-{req.user_id}</td>
                                    <td className="text-slate-400 text-sm font-medium">{new Date(req.created_at).toLocaleDateString()}</td>
                                    <td>
                                        <span className="inline-flex items-center gap-2 text-amber-500 text-xs font-black bg-amber-500/10 px-4 py-2 rounded-full border border-amber-500/20">
                                            <Clock className="w-3.5 h-3.5" /> PENDING
                                        </span>
                                    </td>
                                    <td className="p-8 text-right">
                                        <div className="flex justify-end gap-3 opacity-0 group-hover:opacity-100 transition-opacity">
                                            <button
                                                onClick={() => {
                                                    api.post(`/verifications/${req.id}/approve`).then(() => {
                                                        setRequests(requests.filter((r: any) => r.id !== req.id));
                                                        setStats(prev => ({ ...prev, pending: prev.pending - 1 }));
                                                    });
                                                }}
                                                className="h-10 w-10 flex items-center justify-center bg-slate-900 hover:bg-emerald-600 text-emerald-500 hover:text-white border border-slate-800 rounded-xl transition-all shadow-lg active:scale-90"
                                            >
                                                <CheckCircle className="w-5 h-5" />
                                            </button>
                                            <button
                                                onClick={() => {
                                                    api.post(`/verifications/${req.id}/reject`).then(() => {
                                                        setRequests(requests.filter((r: any) => r.id !== req.id));
                                                        setStats(prev => ({ ...prev, pending: prev.pending - 1 }));
                                                    });
                                                }}
                                                className="h-10 w-10 flex items-center justify-center bg-slate-900 hover:bg-rose-600 text-rose-500 hover:text-white border border-slate-800 rounded-xl transition-all shadow-lg active:scale-90"
                                            >
                                                <XCircle className="w-5 h-5" />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
