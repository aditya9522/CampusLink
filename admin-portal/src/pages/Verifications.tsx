import { useEffect, useState } from 'react';
import { CheckCircle, XCircle, Eye, ShieldCheck, AlertCircle } from 'lucide-react';
import api from '../api';

const Verifications = () => {
    const [requests, setRequests] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    const fetchRequests = async () => {
        try {
            const res = await api.get('/verifications/');
            setRequests(res.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchRequests();
    }, []);

    const handleApprove = async (id: number) => {
        try {
            await api.post(`/verifications/${id}/approve`);
            setRequests(requests.filter(r => r.id !== id));
        } catch (err) {
            alert('Failed to approve');
        }
    };

    const handleReject = async (id: number) => {
        const note = prompt('Rejection reason (optional):');
        try {
            await api.post(`/verifications/${id}/reject`, null, { params: note ? { note } : {} });
            setRequests(requests.filter(r => r.id !== id));
        } catch (err) {
            alert('Failed to reject');
        }
    };

    return (
        <div className="space-y-8 animate-in fade-in duration-500">
            <div>
                <h2 className="text-3xl font-bold text-white tracking-tight">ID Verifications</h2>
                <p className="text-slate-400 mt-1">Review student ID cards to grant official badge</p>
            </div>

            <div className="bg-slate-900/50 backdrop-blur-sm rounded-3xl border border-slate-800 overflow-hidden shadow-sm">
                {loading ? (
                    <div className="p-20 text-center"><div className="w-10 h-10 border-4 border-indigo-600 border-t-transparent rounded-full animate-spin mx-auto"></div></div>
                ) : requests.length === 0 ? (
                    <div className="p-20 text-center">
                        <ShieldCheck className="w-16 h-16 text-slate-800 mx-auto mb-4" />
                        <p className="text-slate-500 font-medium">No pending verification requests</p>
                    </div>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="w-full text-left">
                            <thead className="bg-slate-900/80 text-slate-500 text-[10px] uppercase font-black tracking-[0.2em] border-b border-slate-800">
                                <tr>
                                    <th className="p-8">Student</th>
                                    <th>Submitted At</th>
                                    <th>ID Proof</th>
                                    <th className="text-right p-8">Actions</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-800/50">
                                {requests.map((req) => (
                                    <tr key={req.id} className="hover:bg-slate-800/40 transition-colors group">
                                        <td className="p-8">
                                            <span className="font-bold text-white block">{req.full_name}</span>
                                            <span className="text-slate-500 text-xs">UID: {req.user_id}</span>
                                        </td>
                                        <td className="text-slate-400 text-sm">{new Date(req.created_at).toLocaleString()}</td>
                                        <td>
                                            <a
                                                href={`${import.meta.env.VITE_BASE_URL}/${req.id_card_url}`}
                                                target="_blank"
                                                rel="noreferrer"
                                                className="flex items-center gap-2 text-indigo-400 hover:text-indigo-300 font-bold text-sm bg-indigo-500/10 px-4 py-2 rounded-xl border border-indigo-500/20 w-fit"
                                            >
                                                <Eye className="w-4 h-4" /> View ID Card
                                            </a>
                                        </td>
                                        <td className="p-8 text-right">
                                            <div className="flex justify-end gap-3 opacity-0 group-hover:opacity-100 transition-opacity">
                                                <button
                                                    onClick={() => handleApprove(req.id)}
                                                    className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-500 text-white px-4 py-2 rounded-xl font-bold transition-all shadow-lg active:scale-95"
                                                >
                                                    <CheckCircle className="w-4 h-4" /> Approve
                                                </button>
                                                <button
                                                    onClick={() => handleReject(req.id)}
                                                    className="flex items-center gap-2 bg-rose-600 hover:bg-rose-500 text-white px-4 py-2 rounded-xl font-bold transition-all shadow-lg active:scale-95"
                                                >
                                                    <XCircle className="w-4 h-4" /> Reject
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            <div className="bg-indigo-600/10 border border-indigo-500/20 p-6 rounded-3xl flex items-start gap-4">
                <AlertCircle className="w-6 h-6 text-indigo-400 mt-1 shrink-0" />
                <div>
                    <h4 className="text-indigo-400 font-bold mb-1 tracking-tight">Admin Note</h4>
                    <p className="text-slate-400 text-sm leading-relaxed">
                        Verifying a student grants them the "Verified Student" badge on their profile and increases their trustworthiness for marketplace listings. Please check names against the provided ID card carefully.
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Verifications;
