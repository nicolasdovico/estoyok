'use client';

import { useState, useEffect } from 'react';

interface Contact {
  id: number;
  name: string;
  phone: string;
  email?: string;
  relationship?: string;
  is_active: boolean;
}

export default function EmergencyContacts() {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingContact, setEditingContact] = useState<Contact | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    email: '',
    relationship: '',
  });

  const fetchContacts = async () => {
    const token = localStorage.getItem('auth_token');
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/emergency-contacts`, {
        headers: { 'Authorization': `Bearer ${token}` },
      });
      const data = await response.json();
      setContacts(data);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchContacts();
  }, []);

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    const token = localStorage.getItem('auth_token');
    const method = editingContact ? 'PUT' : 'POST';
    const url = editingContact 
      ? `${process.env.NEXT_PUBLIC_API_URL}/emergency-contacts/${editingContact.id}`
      : `${process.env.NEXT_PUBLIC_API_URL}/emergency-contacts`;

    try {
      const response = await fetch(url, {
        method,
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        setIsModalOpen(false);
        setEditingContact(null);
        setFormData({ name: '', phone: '', email: '', relationship: '' });
        fetchContacts();
      }
    } catch (err) {
      console.error(err);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('¿Estás seguro de eliminar este contacto?')) return;
    const token = localStorage.getItem('auth_token');
    try {
      await fetch(`${process.env.NEXT_PUBLIC_API_URL}/emergency-contacts/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` },
      });
      fetchContacts();
    } catch (err) {
      console.error(err);
    }
  };

  if (isLoading) return <div className="p-4 text-center">Cargando contactos...</div>;

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
      <div className="p-6 border-b border-gray-50 flex justify-between items-center">
        <div>
          <h3 className="text-lg font-bold text-gray-900">Contactos de Emergencia</h3>
          <p className="text-sm text-gray-500">A quienes avisaremos si algo sucede.</p>
        </div>
        <button 
          onClick={() => {
            setEditingContact(null);
            setFormData({ name: '', phone: '', email: '', relationship: '' });
            setIsModalOpen(true);
          }}
          className="bg-red-600 text-white px-4 py-2 rounded-xl text-sm font-bold hover:bg-red-700 transition-colors"
        >
          Añadir Contacto
        </button>
      </div>

      <div className="divide-y divide-gray-50">
        {contacts.length === 0 ? (
          <div className="p-12 text-center text-gray-400 italic">No tienes contactos configurados.</div>
        ) : (
          contacts.map((contact) => (
            <div key={contact.id} className="p-6 flex justify-between items-start hover:bg-gray-50 transition-colors">
              <div>
                <p className="font-bold text-gray-900">{contact.name}</p>
                <p className="text-xs font-bold text-red-600 uppercase tracking-wider">{contact.relationship || 'Contacto'}</p>
                <div className="mt-2 space-y-1">
                  <p className="text-sm text-gray-600 flex items-center gap-2">
                    <span className="opacity-50 text-xs">📞</span> {contact.phone}
                  </p>
                  {contact.email && (
                    <p className="text-sm text-gray-600 flex items-center gap-2">
                      <span className="opacity-50 text-xs">✉️</span> {contact.email}
                    </p>
                  )}
                </div>
              </div>
              <div className="flex gap-2">
                <button 
                  onClick={() => {
                    setEditingContact(contact);
                    setFormData({
                      name: contact.name,
                      phone: contact.phone,
                      email: contact.email || '',
                      relationship: contact.relationship || '',
                    });
                    setIsModalOpen(true);
                  }}
                  className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                >
                  ✏️
                </button>
                <button 
                  onClick={() => handleDelete(contact.id)}
                  className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                >
                  🗑️
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl w-full max-w-md p-8 shadow-2xl">
            <h2 className="text-2xl font-black text-gray-900 mb-6">
              {editingContact ? 'Editar Contacto' : 'Nuevo Contacto'}
            </h2>
            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-gray-500 uppercase tracking-widest mb-1 ml-1">Nombre</label>
                <input 
                  required
                  className="w-full bg-gray-50 border-none rounded-2xl px-5 py-4 text-gray-900 focus:ring-2 focus:ring-red-600 outline-none transition-all"
                  value={formData.name}
                  onChange={(e) => setFormData({...formData, name: e.target.value})}
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-500 uppercase tracking-widest mb-1 ml-1">Teléfono</label>
                <input 
                  required
                  placeholder="+5411..."
                  className="w-full bg-gray-50 border-none rounded-2xl px-5 py-4 text-gray-900 focus:ring-2 focus:ring-red-600 outline-none transition-all"
                  value={formData.phone}
                  onChange={(e) => setFormData({...formData, phone: e.target.value})}
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-500 uppercase tracking-widest mb-1 ml-1">Email</label>
                <input 
                  type="email"
                  className="w-full bg-gray-50 border-none rounded-2xl px-5 py-4 text-gray-900 focus:ring-2 focus:ring-red-600 outline-none transition-all"
                  value={formData.email}
                  onChange={(e) => setFormData({...formData, email: e.target.value})}
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-500 uppercase tracking-widest mb-1 ml-1">Relación</label>
                <input 
                  placeholder="Madre, Amigo, etc."
                  className="w-full bg-gray-50 border-none rounded-2xl px-5 py-4 text-gray-900 focus:ring-2 focus:ring-red-600 outline-none transition-all"
                  value={formData.relationship}
                  onChange={(e) => setFormData({...formData, relationship: e.target.value})}
                />
              </div>
              <div className="flex gap-4 pt-4">
                <button 
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="flex-1 bg-gray-100 text-gray-900 py-4 rounded-2xl font-bold hover:bg-gray-200 transition-colors"
                >
                  Cancelar
                </button>
                <button 
                  type="submit"
                  className="flex-1 bg-red-600 text-white py-4 rounded-2xl font-bold hover:bg-red-700 shadow-lg shadow-red-200 transition-colors"
                >
                  Guardar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
