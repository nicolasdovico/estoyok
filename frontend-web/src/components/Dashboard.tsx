'use client';

import { useState, useEffect } from 'react';
import dynamic from 'next/dynamic';
import Link from 'next/link';

const TrackingMap = dynamic(() => import('@/components/EmergencyMap'), {
  ssr: false,
  loading: () => <div className="h-full w-full bg-gray-100 flex items-center justify-center">Cargando mapa...</div>
});

interface UserData {
  id: number;
  name: string;
  email: string;
  is_premium: boolean;
  current_location?: {
    latitude: number;
    longitude: number;
    updated_at: string;
  } | null;
  circles: Array<{
    id: number;
    name: string;
  }>;
}

export default function Dashboard() {
  const [userData, setUserData] = useState<UserData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchUserData = async () => {
    const token = localStorage.getItem('auth_token');
    if (!token) return;

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/user`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/json',
        },
      });

      if (!response.ok) {
        if (response.status === 401) {
          localStorage.removeItem('auth_token');
          window.location.reload();
          return;
        }
        throw new Error('Error al obtener datos del usuario');
      }

      const data = await response.json();
      setUserData(data);
    } catch (err) {
      setError('No se pudo cargar la información');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUserData();
    // Actualizar cada 30 segundos
    const interval = setInterval(fetchUserData, 30000);
    return () => clearInterval(interval);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('auth_token');
    window.location.reload();
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-red-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
      {/* Navbar Dashboard */}
      <header className="bg-white border-b border-gray-200 px-4 h-16 flex items-center justify-between sticky top-0 z-40">
        <Link href="/" className="flex items-center">
          <span className="text-xl font-black text-red-600 tracking-tighter">ESTOY OK</span>
          <span className="ml-2 px-2 py-0.5 bg-gray-100 text-[10px] font-bold text-gray-500 rounded uppercase tracking-wider">Dashboard</span>
        </Link>
        <div className="flex items-center gap-4">
          <div className="text-right hidden sm:block">
            <p className="text-xs font-bold text-gray-900">{userData?.name}</p>
            <p className="text-[10px] text-gray-500">{userData?.email}</p>
          </div>
          <button 
            onClick={handleLogout}
            className="text-xs font-bold text-gray-500 hover:text-red-600 transition-colors"
          >
            Cerrar Sesión
          </button>
        </div>
      </header>

      <main className="flex-1 p-4 md:p-6 lg:p-8 max-w-7xl mx-auto w-full grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Columna Izquierda: Información y Círculos */}
        <div className="lg:col-span-1 space-y-6">
          <section className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
            <h2 className="text-lg font-bold text-gray-900 mb-4">Mi Estado</h2>
            <div className="space-y-4">
              <div className="flex items-center justify-between p-3 bg-green-50 rounded-xl border border-green-100">
                <span className="text-sm text-green-700 font-medium">Cuenta Activa</span>
                <div className="h-2 w-2 bg-green-500 rounded-full animate-pulse"></div>
              </div>
              
              {userData?.is_premium ? (
                <div className="p-3 bg-yellow-50 rounded-xl border border-yellow-100 flex items-center gap-3">
                  <span className="text-xl">⭐</span>
                  <div>
                    <p className="text-xs font-bold text-yellow-800 uppercase">Socio Premium</p>
                    <p className="text-[10px] text-yellow-700">Funciones avanzadas activas</p>
                  </div>
                </div>
              ) : (
                <Link href="/#premium" className="block p-3 bg-gray-50 hover:bg-gray-100 rounded-xl border border-gray-200 transition-colors">
                  <p className="text-xs font-bold text-gray-700 uppercase">Plan Gratuito</p>
                  <p className="text-[10px] text-red-600 font-bold mt-1 underline">Mejorar a PRO →</p>
                </Link>
              )}
            </div>
          </section>

          <section className="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-gray-900">Mis Círculos</h2>
              <button className="text-xs bg-red-600 text-white px-3 py-1 rounded-lg font-bold hover:bg-red-700">Nuevo</button>
            </div>
            
            {userData?.circles.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-sm text-gray-500 mb-4">Aún no tienes círculos de confianza</p>
                <button className="text-sm font-bold text-red-600 underline">Crear el primero</button>
              </div>
            ) : (
              <div className="space-y-2">
                {userData?.circles.map(circle => (
                  <div key={circle.id} className="flex items-center justify-between p-3 hover:bg-gray-50 rounded-xl border border-transparent hover:border-gray-200 transition-all cursor-pointer">
                    <span className="text-sm font-medium text-gray-700">{circle.name}</span>
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor" className="w-4 h-4 text-gray-400">
                      <path fillRule="evenodd" d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z" clipRule="evenodd" />
                    </svg>
                  </div>
                ))}
              </div>
            )}
          </section>
        </div>

        {/* Columna Derecha: Mapa */}
        <div className="lg:col-span-2 flex flex-col space-y-4 min-h-[500px]">
          <section className="bg-white rounded-2xl shadow-sm border border-gray-100 flex-1 flex flex-col overflow-hidden">
            <div className="p-4 border-b border-gray-100 flex items-center justify-between">
              <h3 className="font-bold text-gray-800 flex items-center gap-2">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" className="w-5 h-5 text-red-600">
                  <path fillRule="evenodd" d="m11.54 22.351.07.04.028.016a.76.76 0 0 0 .723 0l.028-.015.071-.041a16.975 16.975 0 0 0 1.144-.742 19.58 19.58 0 0 0 2.683-2.282c1.944-1.99 3.963-4.98 3.963-8.827a8.25 8.25 0 0 0-16.5 0c0 3.846 2.02 6.837 3.963 8.827a19.58 19.58 0 0 0 2.682 2.282 16.975 16.975 0 0 0 1.145.742ZM12 13.5a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" clipRule="evenodd" />
                </svg>
                Mi Última Ubicación
              </h3>
              {userData?.current_location && (
                <span className="text-[10px] text-gray-400">
                  Actualizado: {new Date(userData.current_location.updated_at).toLocaleTimeString()}
                </span>
              )}
            </div>
            
            <div className="flex-1 relative">
              {userData?.current_location && typeof userData.current_location.latitude === 'number' && typeof userData.current_location.longitude === 'number' ? (
                <TrackingMap center={[userData.current_location.latitude, userData.current_location.longitude]} />
              ) : (
                <div className="absolute inset-0 bg-gray-100 flex flex-col items-center justify-center text-center p-8">
                  <div className="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center mb-4">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-8 h-8 text-gray-400">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M15 10.5a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
                      <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25s-7.5-4.108-7.5-11.25a7.5 7.5 0 1 1 15 0Z" />
                    </svg>
                  </div>
                  <h4 className="text-gray-900 font-bold">Sin datos de ubicación</h4>
                  <p className="text-xs text-gray-500 mt-2 max-w-[200px]">
                    Activa el seguimiento en la aplicación móvil para ver tu ubicación aquí.
                  </p>
                </div>
              )}
            </div>
          </section>
        </div>
      </main>
    </div>
  );
}
