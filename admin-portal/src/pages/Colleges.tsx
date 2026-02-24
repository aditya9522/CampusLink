import { useState, useEffect } from 'react';
import { Plus, Search, Building2, Mail, Copy, Check } from 'lucide-react';
import api from '../api';

interface College {
    id: number;
    name: string;
    slug: string;
    invite_code: string;
    is_active: boolean;
}

const Colleges = () => {
    const [colleges, setColleges] = useState<College[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [showAdd, setShowAdd] = useState(false);
    const [copiedCode, setCopiedCode] = useState<string | null>(null);

    const [newName, setNewName] = useState('');
    const [newSlug, setNewSlug] = useState('');

    const fetchColleges = async () => {
        try {
            const res = await api.get('/colleges/');
            setColleges(res.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchColleges();
    }, []);

    const handleAdd = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await api.post('/colleges/', { name: newName, slug: newSlug });
            setShowAdd(false);
            setNewName('');
            setNewSlug('');
            fetchColleges();
        } catch (err) {
            alert('Failed to add college');
        }
    };

    const copyToClipboard = (code: string) => {
        navigator.clipboard.writeText(code);
        setCopiedCode(code);
        setTimeout(() => setCopiedCode(null), 2000);
    };

    const filtered = colleges.filter(c =>
        c.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="space-y-8 animate-in fade-in duration-500">
            <div className="flex justify-between items-end">
                <div>
                    <h2 className="text-3xl font-bold text-white tracking-tight">University Partners</h2>
                    <p className="text-slate-400 mt-1">Manage institutional access and invite codes</p>
                </div>
                <button
                    onClick={() => setShowAdd(true)}
                    className="bg-indigo-600 hover:bg-indigo-500 text-white px-6 py-3 rounded-2xl font-bold flex items-center gap-2 transition-all shadow-lg active:scale-95"
                >
                    <Plus className="w-5 h-5" /> Add College
                </button>
            </div>

            <div className="relative">
                <Search className="w-5 h-5 absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                    type="text"
                    placeholder="Search universities..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-800 rounded-2xl py-4 pl-12 pr-4 outline-none focus:border-indigo-500 transition-all text-white"
                />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {loading ? (
                    [1, 2, 3].map(i => <div key={i} className="h-48 bg-slate-900 rounded-3xl animate-pulse border border-slate-800"></div>)
                ) : filtered.map(college => (
                    <div key={college.id} className="bg-slate-900/50 backdrop-blur-sm p-6 rounded-[2rem] border border-slate-800 hover:border-indigo-500/50 transition-all group">
                        <div className="flex justify-between items-start mb-6">
                            <div className="w-14 h-14 rounded-2xl bg-indigo-600/10 flex items-center justify-center text-indigo-500">
                                <Building2 className="w-8 h-8" />
                            </div>
                            <span className={`px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-widest ${college.is_active ? 'bg-emerald-500/10 text-emerald-500' : 'bg-slate-800 text-slate-500'
                                }`}>
                                {college.is_active ? 'Active' : 'Inactive'}
                            </span>
                        </div>

                        <h3 className="text-xl font-bold text-white mb-1">{college.name}</h3>
                        <p className="text-slate-500 text-xs font-medium mb-6 uppercase tracking-wider">/{college.slug}</p>

                        <div className="space-y-3">
                            <div className="bg-slate-950 p-4 rounded-2xl flex justify-between items-center group/code">
                                <div>
                                    <span className="text-[10px] font-black uppercase tracking-widest text-slate-600 block mb-1">Invite Code</span>
                                    <span className="text-indigo-400 font-mono font-bold tracking-wider">{college.invite_code}</span>
                                </div>
                                <button
                                    onClick={() => copyToClipboard(college.invite_code)}
                                    className="p-2 hover:bg-slate-800 rounded-lg transition-colors text-slate-500"
                                >
                                    {copiedCode === college.invite_code ? <Check className="w-4 h-4 text-emerald-500" /> : <Copy className="w-4 h-4" />}
                                </button>
                            </div>

                            <div className="flex gap-2">
                                <button className="flex-1 bg-slate-800 hover:bg-slate-700 text-white text-xs font-bold py-3 rounded-xl transition-all flex items-center justify-center gap-2">
                                    <Mail className="w-3.5 h-3.5" /> Invite Admin
                                </button>
                                <button className="flex-1 bg-slate-950 hover:bg-slate-900 text-slate-400 text-xs font-bold py-3 rounded-xl transition-all flex items-center justify-center gap-2">
                                    Analytics
                                </button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {showAdd && (
                <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-slate-900 border border-slate-800 p-8 rounded-[2.5rem] w-full max-w-md shadow-2xl animate-in zoom-in-95 duration-200">
                        <h3 className="text-2xl font-bold text-white mb-1">Register University</h3>
                        <p className="text-slate-500 text-sm mb-8">Onboard a new campus to the network</p>

                        <form onSubmit={handleAdd} className="space-y-5">
                            <div className="space-y-1.5">
                                <label className="text-[10px] uppercase font-black tracking-widest text-slate-500 ml-1">College Name</label>
                                <input
                                    placeholder="e.g. Stanford University"
                                    value={newName}
                                    onChange={(e) => setNewName(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-800 rounded-2xl p-4 text-white outline-none focus:border-indigo-500 transition-all font-medium"
                                    required
                                />
                            </div>

                            <div className="space-y-1.5">
                                <label className="text-[10px] uppercase font-black tracking-widest text-slate-500 ml-1">URL Slug</label>
                                <input
                                    placeholder="e.g. stanford"
                                    value={newSlug}
                                    onChange={(e) => setNewSlug(e.target.value)}
                                    className="w-full bg-slate-950 border border-slate-800 rounded-2xl p-4 text-white outline-none focus:border-indigo-500 transition-all font-medium"
                                    required
                                />
                            </div>

                            <div className="flex gap-3 pt-6">
                                <button type="button" onClick={() => setShowAdd(false)} className="flex-1 text-slate-500 font-bold hover:text-white transition-colors">Cancel</button>
                                <button type="submit" className="flex-[2] bg-indigo-600 hover:bg-indigo-500 text-white py-4 rounded-2xl font-bold shadow-lg shadow-indigo-600/20 active:scale-95 transition-all">Enable Campus</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Colleges;
