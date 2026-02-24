import { useState } from 'react';
import { Send, Bell, Info, AlertTriangle, CheckCircle, XCircle } from 'lucide-react';
import api from '../api';

const Alerts = () => {
    const [title, setTitle] = useState('');
    const [message, setMessage] = useState('');
    const [type, setType] = useState('info');
    const [loading, setLoading] = useState(false);

    const handleSend = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!title || !message) return;

        setLoading(true);
        try {
            await api.post('/notifications/send', {
                title,
                message,
                type
            });
            alert('Notification broadcasted to all active students!');
            setTitle('');
            setMessage('');
        } catch (err) {
            alert('Failed to send notification');
        } finally {
            setLoading(false);
        }
    };

    const types = [
        { id: 'info', label: 'Information', icon: Info, color: 'text-blue-400', bg: 'bg-blue-400/10', border: 'border-blue-400/20' },
        { id: 'success', label: 'Success', icon: CheckCircle, color: 'text-emerald-400', bg: 'bg-emerald-400/10', border: 'border-emerald-400/20' },
        { id: 'warning', label: 'Warning', icon: AlertTriangle, color: 'text-amber-400', bg: 'bg-amber-400/10', border: 'border-amber-400/20' },
        { id: 'error', label: 'Critical', icon: XCircle, color: 'text-rose-400', bg: 'bg-rose-400/10', border: 'border-rose-400/20' },
    ];

    return (
        <div className="max-w-4xl mx-auto space-y-10 animate-in fade-in slide-in-from-bottom-4 duration-500">
            <div>
                <h2 className="text-3xl font-bold text-white tracking-tight">System Broadcast</h2>
                <p className="text-slate-400 mt-1">Send real-time alerts and notifications to all CampusLink users</p>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                {/* Form */}
                <div className="lg:col-span-2 bg-slate-900/50 backdrop-blur-sm p-8 rounded-3xl border border-slate-800 shadow-xl">
                    <form onSubmit={handleSend} className="space-y-6">
                        <div className="space-y-2">
                            <label className="text-sm font-medium text-slate-400 ml-1">Notification Title</label>
                            <input
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                placeholder="e.g. Maintenance Alert or New Event!"
                                className="w-full bg-slate-950 border border-slate-800 rounded-2xl p-4 outline-none focus:border-indigo-500 transition-all text-white font-medium"
                            />
                        </div>

                        <div className="space-y-2">
                            <label className="text-sm font-medium text-slate-400 ml-1">Detailed Message</label>
                            <textarea
                                value={message}
                                onChange={(e) => setMessage(e.target.value)}
                                placeholder="Describe the alert in detail..."
                                rows={5}
                                className="w-full bg-slate-950 border border-slate-800 rounded-2xl p-4 outline-none focus:border-indigo-500 transition-all text-white resize-none"
                            />
                        </div>

                        <div className="space-y-4">
                            <label className="text-sm font-medium text-slate-400 ml-1">Alert Category</label>
                            <div className="grid grid-cols-2 gap-4">
                                {types.map((t) => (
                                    <button
                                        key={t.id}
                                        type="button"
                                        onClick={() => setType(t.id)}
                                        className={`
                      flex items-center gap-3 p-4 rounded-2xl border transition-all text-left
                      ${type === t.id
                                                ? `${t.bg} ${t.border} ${t.color} ring-2 ring-indigo-500/20`
                                                : 'bg-slate-950/50 border-slate-800 text-slate-500 hover:border-slate-700'}
                    `}
                                    >
                                        <t.icon className="w-5 h-5 shrink-0" />
                                        <span className="font-bold text-sm tracking-tight">{t.label}</span>
                                    </button>
                                ))}
                            </div>
                        </div>

                        <button
                            disabled={loading}
                            className="w-full bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-bold py-4 rounded-2xl transition-all shadow-lg shadow-indigo-600/20 flex items-center justify-center gap-3 active:scale-[0.98]"
                        >
                            {loading ? (
                                <div className="w-6 h-6 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                            ) : (
                                <>
                                    <Send className="w-5 h-5" />
                                    Broadcast to Everyone
                                </>
                            )}
                        </button>
                    </form>
                </div>

                {/* Live Preview */}
                <div className="space-y-6">
                    <h4 className="text-xs uppercase font-black tracking-[0.2em] text-slate-500 ml-1">Live Preview</h4>
                    <div className="bg-slate-950 border border-slate-800 p-6 rounded-3xl shadow-2xl relative overflow-hidden group">
                        <div className={`absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity`}>
                            <Bell className="w-24 h-24" />
                        </div>

                        <div className="flex items-center gap-3 mb-4">
                            {(() => {
                                const t = types.find(it => it.id === type) || types[0];
                                return (
                                    <>
                                        <div className={`p-2 ${t.bg} ${t.color} rounded-lg`}>
                                            <t.icon className="w-4 h-4" />
                                        </div>
                                        <span className={`text-[10px] font-black uppercase tracking-widest ${t.color}`}>
                                            System {t.id}
                                        </span>
                                    </>
                                )
                            })()}
                        </div>

                        <h3 className="text-xl font-bold text-white mb-2 leading-tight">
                            {title || 'Your Title Here'}
                        </h3>
                        <p className="text-slate-400 text-sm leading-relaxed">
                            {message || 'This is how your message will look on the mobile app. It will appear as a real-time Snackbar notification.'}
                        </p>

                        <div className="mt-8 pt-4 border-t border-slate-900 flex justify-between items-center text-[10px] text-slate-600 font-bold uppercase tracking-widest">
                            <span>Direct Broadcast</span>
                            <span>Active now</span>
                        </div>
                    </div>

                    <div className="p-6 bg-indigo-600/10 border border-indigo-500/20 rounded-3xl">
                        <p className="text-indigo-400/80 text-xs leading-relaxed font-medium text-center">
                            This will send a JSON payload via WebSocket to all currently connected mobile devices and save the notification in their history.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Alerts;
