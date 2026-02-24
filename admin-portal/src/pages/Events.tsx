import { useEffect, useState } from 'react';
import { Calendar, Trash2, Edit3, Plus, Search, MapPin, Clock as ClockIcon, X, QrCode, Download } from 'lucide-react';
import api from '../api';

const Events = () => {
    const [events, setEvents] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const [showCreate, setShowCreate] = useState(false);
    const [selectedQr, setSelectedQr] = useState<any>(null);

    // Create Form State
    const [form, setForm] = useState({
        title: '',
        description: '',
        location: '',
        date_time: '',
        type: 'club',
        seats_available: 50
    });

    const fetchEvents = async () => {
        try {
            const res = await api.get('/events/');
            setEvents(res.data);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchEvents();
    }, []);

    const handleDelete = async (id: number) => {
        if (!window.confirm('Are you sure you want to delete this event?')) return;
        try {
            await api.delete(`/events/${id}`);
            setEvents(events.filter(e => e.id !== id));
        } catch (err) {
            alert('Failed to delete event');
        }
    };

    const handleCreate = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await api.post('/events/', {
                ...form,
                date_time: new Date(form.date_time).toISOString()
            });
            setShowCreate(false);
            fetchEvents();
            alert(`Event broadcasted as ${form.type.toUpperCase()}`);
        } catch (err) {
            alert('Error creating event');
        }
    };

    const filteredEvents = events.filter(e =>
        e.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        e.location.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="space-y-8 animate-in fade-in duration-500">
            <div className="flex justify-between items-end">
                <div>
                    <h2 className="text-3xl font-bold text-white tracking-tight">Campus Events</h2>
                    <p className="text-slate-400 mt-1">Manage and moderate all active student gatherings</p>
                </div>
                <button
                    onClick={() => setShowCreate(true)}
                    className="bg-indigo-600 hover:bg-indigo-500 text-white px-6 py-3 rounded-2xl font-bold flex items-center gap-2 transition-all shadow-lg shadow-indigo-600/20 active:scale-95"
                >
                    <Plus className="w-5 h-5" />
                    Create Event
                </button>
            </div>

            {/* Create Modal */}
            {showCreate && (
                <div className="fixed inset-0 bg-slate-950/90 backdrop-blur-md z-50 flex items-center justify-center p-4">
                    <div className="bg-slate-900 border border-slate-800 p-8 rounded-[2rem] w-full max-w-xl shadow-2xl animate-in zoom-in-95 duration-200">
                        <div className="flex justify-between items-center mb-8">
                            <h3 className="text-2xl font-bold text-white">Schedule New Activity</h3>
                            <button onClick={() => setShowCreate(false)} className="text-slate-500 hover:text-white"><X /></button>
                        </div>

                        <form onSubmit={handleCreate} className="space-y-4">
                            <input
                                placeholder="Event Name"
                                value={form.title}
                                onChange={e => setForm({ ...form, title: e.target.value })}
                                className="w-full bg-slate-950 border border-slate-800 rounded-2xl p-4 text-white outline-none focus:border-indigo-500"
                            />
                            <textarea
                                placeholder="Short description..."
                                value={form.description}
                                onChange={e => setForm({ ...form, description: e.target.value })}
                                className="w-full bg-slate-950 border border-slate-800 rounded-2xl p-4 text-white outline-none focus:border-indigo-500 h-24"
                            />
                            <div className="grid grid-cols-2 gap-4">
                                <input
                                    type="datetime-local"
                                    value={form.date_time}
                                    onChange={e => setForm({ ...form, date_time: e.target.value })}
                                    className="bg-slate-950 border border-slate-800 rounded-2xl p-4 text-white outline-none focus:border-indigo-500"
                                />
                                <select
                                    value={form.type}
                                    onChange={e => setForm({ ...form, type: e.target.value })}
                                    className="bg-slate-950 border border-slate-800 rounded-2xl p-4 text-white outline-none"
                                >
                                    <option value="club">Club Event</option>
                                    <option value="placement">Placement Drive</option>
                                    <option value="workshop">Skill Workshop</option>
                                    <option value="sports">Sports Match</option>
                                </select>
                            </div>
                            <input
                                placeholder="Location (e.g. Ground Floor Hub)"
                                value={form.location}
                                onChange={e => setForm({ ...form, location: e.target.value })}
                                className="w-full bg-slate-950 border border-slate-800 rounded-2xl p-4 text-white outline-none focus:border-indigo-500"
                            />
                            <button className="w-full bg-indigo-600 hover:bg-indigo-500 text-white font-bold py-4 rounded-2xl mt-4 shadow-lg shadow-indigo-600/20 active:scale-[0.98] transition-all">
                                Broadcast Event
                            </button>
                        </form>
                    </div>
                </div>
            )}

            <div className="relative">
                <Search className="w-5 h-5 absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                    type="text"
                    placeholder="Search by title or location..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-full bg-slate-900 border border-slate-800 rounded-2xl py-4 pl-12 pr-4 outline-none focus:border-indigo-500 transition-all text-white shadow-sm"
                />
            </div>

            {loading ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {[1, 2, 3].map(i => <div key={i} className="h-64 bg-slate-900 rounded-3xl animate-pulse border border-slate-800"></div>)}
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {filteredEvents.map((event) => (
                        <div key={event.id} className="bg-slate-900/50 backdrop-blur-sm rounded-3xl border border-slate-800 overflow-hidden hover:border-slate-600 transition-all group flex flex-col shadow-sm">
                            <div className="h-40 bg-slate-800 relative overflow-hidden">
                                <div className="p-4 flex gap-4">
                                    <span className={`px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-widest ${event.type === 'placement' ? 'bg-amber-500 text-black' : 'bg-indigo-600 text-white'
                                        }`}>
                                        {event.type}
                                    </span>
                                </div>
                                <div className="absolute top-4 right-4 flex gap-2">
                                    <button
                                        onClick={() => setSelectedQr(event)}
                                        className="p-2 bg-slate-950/80 backdrop-blur-md rounded-xl text-indigo-400 hover:text-white transition-colors"
                                    >
                                        <QrCode className="w-4 h-4" />
                                    </button>
                                    <button className="p-2 bg-slate-950/80 backdrop-blur-md rounded-xl text-slate-400 hover:text-white transition-colors">
                                        <Edit3 className="w-4 h-4" />
                                    </button>
                                    <button
                                        onClick={() => handleDelete(event.id)}
                                        className="p-2 bg-slate-950/80 backdrop-blur-md rounded-xl text-rose-500 hover:bg-rose-600 hover:text-white transition-all"
                                    >
                                        <Trash2 className="w-4 h-4" />
                                    </button>
                                </div>
                            </div>

                            <div className="p-6 flex-1 flex flex-col">
                                <h3 className="text-xl font-bold text-white mb-2 line-clamp-1">{event.title}</h3>
                                <p className="text-slate-400 text-sm line-clamp-2 mb-4 flex-1">{event.description}</p>

                                <div className="space-y-2 mt-auto">
                                    <div className="flex items-center gap-2 text-slate-500 text-xs">
                                        <MapPin className="w-4 h-4 text-indigo-500" />
                                        {event.location}
                                    </div>
                                    <div className="flex items-center gap-2 text-slate-500 text-xs">
                                        <ClockIcon className="w-4 h-4 text-indigo-500" />
                                        {new Date(event.date_time).toLocaleDateString()}
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {!loading && filteredEvents.length === 0 && (
                <div className="text-center py-20">
                    <Calendar className="w-16 h-16 text-slate-800 mx-auto mb-4" />
                    <p className="text-slate-500 font-medium">No events found matching your search</p>
                </div>
            )}

            {/* QR Modal */}
            {selectedQr && (
                <div className="fixed inset-0 bg-slate-950/90 backdrop-blur-md z-50 flex items-center justify-center p-4">
                    <div className="bg-slate-900 border border-slate-800 p-8 rounded-[2rem] w-full max-w-sm shadow-2xl text-center animate-in zoom-in duration-200">
                        <div className="flex justify-between items-center mb-6">
                            <h3 className="text-xl font-bold text-white">Event Check-in QR</h3>
                            <button onClick={() => setSelectedQr(null)} className="text-slate-500 hover:text-white"><X /></button>
                        </div>

                        <div className="bg-white p-6 rounded-3xl inline-block mb-6 shadow-xl">
                            <img
                                src={`https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=CAMPUSLINK:EVENT:${selectedQr.id}`}
                                alt="Event QR"
                                className="w-48 h-48 mx-auto"
                            />
                        </div>

                        <h4 className="text-white font-bold mb-1">{selectedQr.title}</h4>
                        <p className="text-slate-500 text-sm mb-6">Students scan this with their CampusLink app to mark attendance.</p>

                        <button
                            onClick={() => window.open(`https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=CAMPUSLINK:EVENT:${selectedQr.id}`)}
                            className="w-full bg-slate-800 hover:bg-slate-700 text-white font-bold py-4 rounded-2xl flex items-center justify-center gap-2 transition-all"
                        >
                            <Download className="w-5 h-5" /> Download High-Res
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Events;
