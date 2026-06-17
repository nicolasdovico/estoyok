'use client';

import { useState, useEffect } from 'react';
import dynamic from 'next/dynamic';
import Link from 'next/link';
import EmergencyContacts from '@/components/EmergencyContacts';
import SecuritySettings from '@/components/SecuritySettings';

const TrackingMap = dynamic(() => import('@/components/EmergencyMap'), {
  ssr: false,
  loading: () => <div className="h-full w-full bg-gray-100 flex items-center justify-center">Cargando mapa...</div>
});

interface UserData {
  id: number;
  name: string;
  email: string;
  is_premium: boolean;
  checkin_interval_hours: number;
  last_check_in_at: string | null;
  quiet_hours_enabled?: boolean;
  quiet_hours_start?: string | null;
  quiet_hours_end?: string | null;
  allow_sms_whatsapp_checkin?: boolean;
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
  const [isCheckingIn, setIsCheckingIn] = useState(false);
  const [activeTab, setActiveTab] = useState<'wellbeing' | 'tracking'>('wellbeing');
  const [notification, setNotification] = useState<{ message: string; type: 'success' | 'error' } | null>(null);
  const [justCheckedIn, setJustCheckedIn] = useState(false);
  const [checkIns, setCheckIns] = useState<Array<{ id: number; created_at: string }>>([]);
  const [isLoadingCheckIns, setIsLoadingCheckIns] = useState(false);

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setNotification({ message, type });
  };

  useEffect(() => {
    if (notification) {
      const timer = setTimeout(() => {
        setNotification(null);
      }, 4000);
      return () => clearTimeout(timer);
    }
  }, [notification]);

  useEffect(() => {
    if (justCheckedIn) {
      const timer = setTimeout(() => {
        setJustCheckedIn(false);
      }, 8000);
      return () => clearTimeout(timer);
    }
  }, [justCheckedIn]);

  const getStatus = () => {
    if (!userData?.last_check_in_at) return { safe: false, lastCheckInTime: null, nextCheckInTime: null };
    
    const lastCheckInTime = new Date(userData.last_check_in_at).getTime();
    const intervalMs = userData.checkin_interval_hours * (app_env === 'local' ? 60 * 1000 : 60 * 60 * 1000);
    const nextCheckInTime = lastCheckInTime + intervalMs;
    const now = new Date().getTime();
    
    return {
      safe: nextCheckInTime > now,
      lastCheckInTime,
      nextCheckInTime
    };
  };

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

      if (response.ok) {
        const data = await response.json();
        setUserData(data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const fetchCheckIns = async () => {
    const token = localStorage.getItem('auth_token');
    if (!token) return;

    setIsLoadingCheckIns(true);
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/check-ins`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/json',
        },
      });

      if (response.ok) {
        const data = await response.json();
        setCheckIns(data);
      }
    } catch (err) {
      console.error('Error fetching check-ins history:', err);
    } finally {
      setIsLoadingCheckIns(false);
    }
  };

  useEffect(() => {
    fetchUserData();
    fetchCheckIns();
    const interval = setInterval(fetchUserData, 30000);
    return () => clearInterval(interval);
  }, []);

  const handleCheckIn = async () => {
    setIsCheckingIn(true);
    const token = localStorage.getItem('auth_token');
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/check-in`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/json',
        },
      });

      if (response.ok) {
        setJustCheckedIn(true);
        fetchUserData();
        fetchCheckIns();
      } else {
        showToast('Error al realizar el check-in.', 'error');
      }
    } catch {
      showToast('Error al realizar el check-in.', 'error');
    } finally {
      setIsCheckingIn(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('auth_token');
    window.location.reload();
  };

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-white">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-red-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col">
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

      <div className="flex-1 flex flex-col md:flex-row">
        <aside className="w-full md:w-72 bg-white border-b md:border-b-0 md:border-r border-gray-200 p-4 space-y-2">
          <button 
            onClick={() => setActiveTab('wellbeing')}
            className={`w-full text-left px-4 py-4 rounded-2xl text-sm font-bold transition-all flex items-center gap-3 ${activeTab === 'wellbeing' ? 'bg-blue-50 text-blue-600 shadow-sm' : 'text-gray-500 hover:bg-gray-50'}`}
          >
            <span className="text-xl">🛡️</span> Mi Bienestar Diario
          </button>
          <button 
            onClick={() => setActiveTab('tracking')}
            className={`w-full text-left px-4 py-4 rounded-2xl text-sm font-bold transition-all flex items-center gap-3 ${activeTab === 'tracking' ? 'bg-red-50 text-red-600 shadow-sm' : 'text-gray-500 hover:bg-gray-50'}`}
          >
            <span className="text-xl">📍</span> Rastreo y Círculos
          </button>

          <div className="pt-8 px-4 border-t border-gray-50 mt-4">
            <p className="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-4">Suscripción</p>
            {userData?.is_premium ? (
              <div className="p-3 bg-yellow-50 rounded-xl border border-yellow-100">
                <p className="text-xs font-bold text-yellow-800 uppercase">Premium Activo ⭐</p>
              </div>
            ) : (
              <Link href="/#premium" className="block p-4 bg-gray-50 hover:bg-gray-100 rounded-2xl border border-gray-200 transition-colors text-center">
                <p className="text-xs font-bold text-gray-700 uppercase">Mejorar a PRO →</p>
              </Link>
            )}
          </div>
        </aside>

        <main className="flex-1 p-4 md:p-8">
          <div className="max-w-4xl mx-auto space-y-8">
            {activeTab === 'wellbeing' && (
              <div className="space-y-8">
                {/* Temporary Success Banner */}
                {justCheckedIn && (
                  <div className="bg-gradient-to-r from-emerald-500 to-teal-600 text-white p-6 rounded-3xl flex items-center gap-4 shadow-lg animate-bounce-short">
                    <div className="w-12 h-12 bg-white/20 rounded-2xl flex items-center justify-center text-2xl">
                      🎉
                    </div>
                    <div className="flex-1">
                      <h3 className="font-black text-lg">¡Reporte de Bienestar Recibido!</h3>
                      <p className="text-emerald-100 text-sm mt-0.5">
                        Tu estado ha sido actualizado con éxito. Tus contactos de emergencia están notificados de que te encuentras bien.
                      </p>
                    </div>
                    <button 
                      onClick={() => setJustCheckedIn(false)}
                      className="text-white/85 hover:text-white text-xs font-bold bg-white/10 hover:bg-white/20 px-3.5 py-2 rounded-xl transition-all cursor-pointer"
                    >
                      Entendido
                    </button>
                  </div>
                )}

                {/* Status Banner */}
                {userData && (() => {
                  const status = getStatus();
                  return status.lastCheckInTime ? (
                    status.safe ? (
                      <div className="bg-emerald-50 border border-emerald-100 p-6 rounded-3xl flex items-start gap-4 shadow-sm">
                        <div className="p-3 bg-emerald-500 text-white rounded-2xl text-xl">
                          🛡️
                        </div>
                        <div className="flex-1">
                          <h3 className="text-emerald-950 font-black text-lg">Estado: Protegido y a Salvo</h3>
                          <p className="text-emerald-700 text-sm mt-1 leading-relaxed">
                            Has confirmado tu bienestar recientemente. Tu temporizador está activo y tu red de contactos de emergencia está tranquila.
                          </p>
                          <div className="flex flex-wrap gap-x-6 gap-y-2 mt-3 text-xs font-bold text-emerald-800">
                            <span>Último reporte: <strong className="text-emerald-950 font-extrabold">{new Date(status.lastCheckInTime).toLocaleString()}</strong></span>
                            <span className="hidden sm:inline">•</span>
                            <span>Próximo vencimiento: <strong className="text-emerald-950 font-extrabold">{new Date(status.nextCheckInTime).toLocaleString()}</strong></span>
                          </div>
                        </div>
                      </div>
                    ) : (
                      <div className="bg-rose-50 border border-rose-100 p-6 rounded-3xl flex items-start gap-4 shadow-sm animate-pulse">
                        <div className="p-3 bg-rose-500 text-white rounded-2xl text-xl">
                          ⚠️
                        </div>
                        <div className="flex-1">
                          <h3 className="text-rose-950 font-black text-lg">Estado: Reporte Vencido</h3>
                          <p className="text-rose-700 text-sm mt-1 leading-relaxed">
                            El tiempo límite para reportar tu bienestar ha expirado. Por favor, presiona el botón &quot;Estoy OK&quot; para actualizar tu estado y avisar a tus contactos que estás bien.
                          </p>
                          <div className="flex flex-wrap gap-x-6 gap-y-2 mt-3 text-xs font-bold text-rose-800">
                            <span>Último reporte: <strong className="text-rose-950 font-extrabold">{new Date(status.lastCheckInTime).toLocaleString()}</strong></span>
                            <span className="hidden sm:inline">•</span>
                            <span className="text-rose-600 font-extrabold">Venció el: {new Date(status.nextCheckInTime).toLocaleString()}</span>
                          </div>
                        </div>
                      </div>
                    )
                  ) : (
                    <div className="bg-blue-50 border border-blue-100 p-6 rounded-3xl flex items-start gap-4 shadow-sm">
                      <div className="p-3 bg-blue-500 text-white rounded-2xl text-xl">
                        ℹ️
                      </div>
                      <div className="flex-1">
                        <h3 className="text-blue-950 font-black text-lg">Estado: Sin Reportes</h3>
                        <p className="text-blue-700 text-sm mt-1 leading-relaxed">
                          Aún no has enviado tu primer reporte de bienestar. Presiona el botón &quot;Estoy OK&quot; a continuación para activar tu sistema de protección pasiva.
                        </p>
                      </div>
                    </div>
                  );
                })()}

                {/* Check-in Module */}
                <div className="bg-white p-10 rounded-3xl shadow-sm border border-gray-100 flex flex-col md:flex-row items-center gap-10">
                  <div className="flex-1 text-center md:text-left">
                    <h2 className="text-3xl font-black text-gray-900 mb-4">Control de Bienestar</h2>
                    <p className="text-gray-500 mb-6 leading-relaxed">
                      Confirma que estás bien una vez cada cierto tiempo. Si olvidas hacerlo, avisaremos automáticamente a tus contactos elegidos.
                    </p>
                    <div className="inline-block px-4 py-2 bg-blue-50 text-blue-700 rounded-full text-sm font-bold">
                      Reporte cada {userData?.checkin_interval_hours} {app_env === 'local' ? 'minutos' : 'horas'}
                    </div>
                  </div>
                  
                  <div className="flex flex-col items-center">
                    <button
                      onClick={handleCheckIn}
                      disabled={isCheckingIn}
                      className={`
                        w-44 h-44 rounded-full flex flex-col items-center justify-center gap-2 transition-all shadow-2xl
                        ${isCheckingIn ? 'bg-gray-200 scale-95' : 'bg-red-600 hover:bg-red-700 hover:scale-105 active:scale-95 shadow-red-200'}
                      `}
                    >
                      {isCheckingIn ? (
                        <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-white"></div>
                      ) : (
                        <>
                          <span className="text-white font-black text-xl tracking-tighter">ESTOY OK</span>
                        </>
                      )}
                    </button>
                    {userData?.last_check_in_at && (
                      <p className="mt-4 text-xs text-gray-400 font-medium italic">
                        Último reporte: {new Date(userData.last_check_in_at).toLocaleString()}
                      </p>
                    )}
                  </div>
                </div>

                {/* Configuration of Wellbeing */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                  <EmergencyContacts />
                  <SecuritySettings 
                    initialInterval={userData?.checkin_interval_hours || 24}
                    initialQuietHoursEnabled={userData?.quiet_hours_enabled || false}
                    initialQuietHoursStart={userData?.quiet_hours_start || '23:00'}
                    initialQuietHoursEnd={userData?.quiet_hours_end || '07:00'}
                    initialAllowSmsWhatsappCheckin={userData?.allow_sms_whatsapp_checkin || false}
                  />
                </div>

                {/* Historial de Reportes */}
                <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
                  <div className="flex items-center gap-3 mb-6">
                    <span className="text-2xl">📋</span>
                    <div>
                      <h3 className="text-xl font-black text-gray-900">Historial de Reportes</h3>
                      <p className="text-xs text-gray-500">Últimos registros de check-in para verificar que el sistema está activo.</p>
                    </div>
                  </div>

                  {isLoadingCheckIns && checkIns.length === 0 ? (
                    <div className="py-8 flex items-center justify-center">
                      <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-600"></div>
                    </div>
                  ) : checkIns.length === 0 ? (
                    <div className="py-8 text-center bg-gray-50 rounded-2xl border border-dashed border-gray-200">
                      <p className="text-sm text-gray-500 italic">No hay reportes de bienestar registrados aún.</p>
                    </div>
                  ) : (
                    <div className="max-h-80 overflow-y-auto space-y-3 pr-2 scrollbar-thin scrollbar-thumb-gray-200">
                      {checkIns.map((checkIn) => (
                        <div 
                          key={checkIn.id} 
                          className="flex items-center justify-between p-4 bg-gray-50 rounded-2xl border border-transparent hover:border-emerald-100 hover:bg-emerald-50/20 transition-all"
                        >
                          <div className="flex items-center gap-3">
                            <div className="w-8 h-8 bg-emerald-100 rounded-full flex items-center justify-center text-emerald-600 text-xs font-bold">
                              ✓
                            </div>
                            <span className="text-sm font-semibold text-gray-700">Reporte de Bienestar Confirmado</span>
                          </div>
                          <span className="text-xs font-bold text-gray-500">
                            {new Date(checkIn.created_at).toLocaleString()}
                          </span>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            )}

            {activeTab === 'tracking' && (
              <div className="space-y-8">
                <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
                  <div className="flex items-center justify-between mb-6">
                    <div>
                      <h2 className="text-2xl font-black text-gray-900">Ubicación Actual</h2>
                      <p className="text-sm text-gray-500">Visible solo para tus círculos de seguridad.</p>
                    </div>
                    {userData?.current_location && (
                      <span className="text-[10px] text-gray-400 font-bold uppercase tracking-widest bg-gray-50 px-3 py-1 rounded-full">
                        Actualizado: {new Date(userData.current_location.updated_at).toLocaleTimeString()}
                      </span>
                    )}
                  </div>
                  <div className="h-[500px] w-full relative rounded-2xl overflow-hidden border border-gray-100">
                    {userData?.current_location ? (
                      <TrackingMap 
                        center={[userData.current_location.latitude, userData.current_location.longitude]} 
                      />
                    ) : (
                      <div className="h-full w-full bg-gray-50 flex flex-col items-center justify-center text-center p-8">
                        <div className="text-4xl mb-4">📍</div>
                        <h4 className="text-gray-900 font-bold">Sin datos de ubicación</h4>
                        <p className="text-xs text-gray-500 mt-2 max-w-[200px]">
                          Activa el rastreo en tu app móvil para ver tu ubicación en este mapa.
                        </p>
                      </div>
                    )}
                  </div>
                </div>

                <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100">
                  <h3 className="text-xl font-black text-gray-900 mb-6">Mis Círculos de Confianza</h3>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    {userData?.circles.length === 0 ? (
                      <div className="col-span-2 py-10 text-center bg-gray-50 rounded-2xl border border-dashed border-gray-200">
                        <p className="text-sm text-gray-500 italic">Aún no participas en círculos de seguridad.</p>
                      </div>
                    ) : (
                      userData?.circles.map(circle => (
                        <div key={circle.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-2xl border border-transparent hover:border-red-100 transition-all">
                          <div className="flex items-center gap-3">
                            <div className="w-10 h-10 bg-red-100 rounded-full flex items-center justify-center text-red-600 font-bold">
                              {circle.name.charAt(0)}
                            </div>
                            <span className="text-sm font-bold text-gray-800">{circle.name}</span>
                          </div>
                          <span className="text-[10px] bg-white px-3 py-1 rounded-full font-black text-gray-400 uppercase shadow-sm">Miembro</span>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
        </main>
        {notification && (
          <div className={`fixed bottom-6 right-6 z-50 flex items-center gap-3 px-4 py-3 rounded-2xl shadow-xl border transition-all duration-300 ${
            notification.type === 'success' 
              ? 'bg-emerald-50 border-emerald-100 text-emerald-800' 
              : 'bg-red-50 border-red-100 text-red-800'
          }`}>
            <span className="text-lg">{notification.type === 'success' ? '✅' : '❌'}</span>
            <div className="flex-1">
              <p className="text-xs font-bold">{notification.message}</p>
            </div>
            <button 
              onClick={() => setNotification(null)}
              className="text-gray-400 hover:text-gray-600 text-xs font-bold ml-2 cursor-pointer"
            >
              ✕
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

const app_env = process.env.NEXT_PUBLIC_APP_ENV || 'production';
