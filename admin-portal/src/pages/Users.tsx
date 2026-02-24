import { useEffect, useState } from 'react';
import { Shield, ShieldAlert, Search, Mail, Phone, MapPin } from 'lucide-react';
import api from '../api';

const UsersPage = () => {
    const [users, setUsers] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    const fetchUsers = async () => {
        try {
            const res = await api.get('/users/', {
                params: { limit: 100 }
            });
            setUsers(res.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const filteredUsers = users.filter(u =>
        u.full_name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        u.email.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="space-y-8 animate-in fade-in duration-500">
            <div className="flex justify-between items-end">
                <div>
                    <h2 className="text-3xl font-bold text-white tracking-tight">User Directory</h2>
                    <p className="text-slate-400 mt-1">Manage student roles and system access</p>
                </div>
            </div>

            <div className="relative">
                <Search className="w-5 h-5 absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                    type="text"
                    placeholder="Search by name or email..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-800 rounded-2xl py-4 pl-12 pr-4 outline-none focus:border-indigo-500 transition-all text-white"
                />
            </div>

            <div className="bg-slate-900/50 backdrop-blur-sm rounded-3xl border border-slate-800 overflow-hidden shadow-sm">
                <div className="overflow-x-auto">
                    <table className="w-full text-left">
                        <thead className="bg-slate-900 text-slate-500 text-[10px] uppercase font-bold tracking-[0.2em] border-b border-slate-800">
                            <tr>
                                <th className="p-8">Student</th>
                                <th>Contact</th>
                                <th>Location</th>
                                <th>Role</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-800/50">
                            {loading ? (
                                [1, 2, 3].map(i => (
                                    <tr key={i} className="animate-pulse">
                                        <td className="p-8"><div className="h-10 w-40 bg-slate-800 rounded-lg"></div></td>
                                        <td><div className="h-10 w-40 bg-slate-800 rounded-lg"></div></td>
                                        <td><div className="h-10 w-40 bg-slate-800 rounded-lg"></div></td>
                                        <td><div className="h-10 w-40 bg-slate-800 rounded-lg"></div></td>
                                    </tr>
                                ))
                            ) : (
                                filteredUsers.map((user) => (
                                    <tr key={user.id} className="hover:bg-slate-800/40 transition-colors group">
                                        <td className="p-8 flex items-center gap-4">
                                            <div className="w-12 h-12 rounded-2xl bg-indigo-600/20 text-indigo-400 flex items-center justify-center font-bold text-lg">
                                                {user.profile_image_url ? (
                                                    <img src={`${import.meta.env.VITE_BASE_URL}/${user.profile_image_url}`} className="w-full h-full object-cover rounded-2xl" />
                                                ) : user.full_name?.[0] || 'U'}
                                            </div>
                                            <div>
                                                <span className="font-bold text-white block">{user.full_name || 'Anonymous User'}</span>
                                                <span className="text-slate-500 text-xs">{user.college_name || 'No College Linked'}</span>
                                            </div>
                                        </td>
                                        <td>
                                            <div className="space-y-1">
                                                <div className="flex items-center gap-2 text-slate-400 text-xs">
                                                    <Mail className="w-3.5 h-3.5" /> {user.email}
                                                </div>
                                                {user.phone_number && (
                                                    <div className="flex items-center gap-2 text-slate-400 text-xs">
                                                        <Phone className="w-3.5 h-3.5" /> {user.phone_number}
                                                    </div>
                                                )}
                                            </div>
                                        </td>
                                        <td className="text-slate-400 text-sm font-medium">
                                            <div className="flex items-center gap-2">
                                                <MapPin className="w-4 h-4 text-slate-600" />
                                                {user.address || 'Not specified'}
                                            </div>
                                        </td>
                                        <td>
                                            {user.is_superuser ? (
                                                <span className="inline-flex items-center gap-2 text-rose-500 text-xs font-black bg-rose-500/10 px-4 py-2 rounded-full border border-rose-500/20">
                                                    <ShieldAlert className="w-3.5 h-3.5" /> SUPERUSER
                                                </span>
                                            ) : user.role === 'college_admin' ? (
                                                <span className="inline-flex items-center gap-2 text-indigo-500 text-xs font-black bg-indigo-500/10 px-4 py-2 rounded-full border border-indigo-500/20">
                                                    <Shield className="w-3.5 h-3.5" /> ADMIN
                                                </span>
                                            ) : (
                                                <span className="inline-flex items-center gap-2 text-emerald-500 text-xs font-black bg-emerald-500/10 px-4 py-2 rounded-full border border-emerald-500/20">
                                                    <Shield className="w-3.5 h-3.5" /> STUDENT
                                                </span>
                                            )}
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default UsersPage;
