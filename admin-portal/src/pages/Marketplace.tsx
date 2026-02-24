import { useEffect, useState } from 'react';
import { ShoppingBag, Search, Trash2 } from 'lucide-react';
import api from '../api';

const Marketplace = () => {
    const [items, setItems] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    const fetchItems = async () => {
        try {
            const res = await api.get('/marketplace/');
            setItems(res.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchItems();
    }, []);

    const handleDelete = async (id: number) => {
        if (!window.confirm('Remove this listing from the marketplace?')) return;
        try {
            await api.delete(`/marketplace/${id}`);
            setItems(items.filter(i => i.id !== id));
        } catch (err) {
            alert('Failed to delete item');
        }
    };

    const filtered = items.filter(i =>
        i.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        i.category.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="space-y-8 animate-in fade-in duration-500">
            <div className="flex justify-between items-end">
                <div>
                    <h2 className="text-3xl font-bold text-white tracking-tight">Campus Marketplace</h2>
                    <p className="text-slate-400 mt-1">Moderate peer-to-peer listings and campus commerce</p>
                </div>
            </div>

            <div className="relative">
                <Search className="w-5 h-5 absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                    type="text"
                    placeholder="Search listings by title or category..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-800 rounded-2xl py-4 pl-12 pr-4 outline-none focus:border-indigo-500 transition-all text-white"
                />
            </div>

            {loading ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {[1, 2, 3].map(i => <div key={i} className="h-64 bg-slate-900 rounded-3xl animate-pulse border border-slate-800"></div>)}
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {filtered.map((item) => (
                        <div key={item.id} className="bg-slate-900/50 backdrop-blur-sm rounded-3xl border border-slate-800 overflow-hidden hover:border-slate-700 transition-all group flex flex-col shadow-sm">
                            <div className="h-48 bg-slate-800 relative overflow-hidden">
                                {item.image_url ? (
                                    <img
                                        src={`${import.meta.env.VITE_BASE_URL}/${item.image_url}`}
                                        className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                                    />
                                ) : (
                                    <div className="w-full h-full flex items-center justify-center text-slate-700">
                                        <ShoppingBag className="w-12 h-12" />
                                    </div>
                                )}
                                <div className="absolute top-4 left-4">
                                    <span className="bg-indigo-600 text-white text-[10px] font-black uppercase tracking-widest px-3 py-1 rounded-full shadow-lg">
                                        {item.category}
                                    </span>
                                </div>
                                <div className="absolute top-4 right-4 opacity-0 group-hover:opacity-100 transition-opacity">
                                    <button
                                        onClick={() => handleDelete(item.id)}
                                        className="p-2 bg-rose-600 text-white rounded-xl shadow-lg hover:bg-rose-500 active:scale-90 transition-all"
                                    >
                                        <Trash2 className="w-4 h-4" />
                                    </button>
                                </div>
                            </div>

                            <div className="p-6 flex-1 flex flex-col">
                                <div className="flex justify-between items-start mb-2">
                                    <h3 className="text-xl font-bold text-white line-clamp-1">{item.title}</h3>
                                    <span className="text-emerald-500 font-black text-lg">₹{item.price}</span>
                                </div>
                                <p className="text-slate-500 text-sm line-clamp-2 mb-6 flex-1">{item.description}</p>

                                <div className="flex items-center justify-between pt-4 border-t border-slate-800/50">
                                    <div className="flex items-center gap-2">
                                        <div className="w-6 h-6 rounded-full bg-slate-800 flex items-center justify-center text-[10px] text-slate-400 font-bold uppercase">
                                            {item.owner_name?.[0] || 'U'}
                                        </div>
                                        <span className="text-xs text-slate-400 font-medium">{item.owner_name || 'Seller'}</span>
                                    </div>
                                    <span className="text-[10px] text-slate-600 font-bold uppercase tracking-widest">
                                        {new Date(item.created_at).toLocaleDateString()}
                                    </span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {!loading && filtered.length === 0 && (
                <div className="text-center py-20 bg-slate-900/30 rounded-[3rem] border border-dashed border-slate-800">
                    <ShoppingBag className="w-16 h-16 text-slate-800 mx-auto mb-4" />
                    <p className="text-slate-500 font-medium">No marketplace listings found</p>
                </div>
            )}
        </div>
    );
};

export default Marketplace;
