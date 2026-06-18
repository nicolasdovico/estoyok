'use client';

import { useState, useEffect } from 'react';
import dynamic from 'next/dynamic';
import Link from 'next/link';
import EmergencyContacts from '@/components/EmergencyContacts';
import SecuritySettings from '@/components/SecuritySettings';

const TrackingMap = dynamic(() => import('@/components/CircleMap'), {
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
  escalation_enabled?: boolean;
  escalation_interval_minutes?: number;
  share_contact_responses?: boolean;
  wifi_checkin_enabled?: boolean;
  safe_wifi_ssid?: string | null;
  sensor_checkin_enabled?: boolean;
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

interface CircleData {
  id: number;
  name: string;
  invite_code: string;
  owner_id: number;
  owner: {
    id: number;
    name: string;
    email: string;
  };
  users: Array<{
    id: number;
    name: string;
    email: string;
    is_premium: boolean;
    current_location?: {
      latitude: number;
      longitude: number;
      updated_at: string;
    } | null;
    pivot: {
      role: string;
    };
  }>;
  geofences: Array<{
    id: number;
    name: string;
    radius: number;
    type: string;
    latitude: number;
    longitude: number;
    user_id?: number | null;
    user?: {
      id: number;
      name: string;
    } | null;
  }>;
}

export default function Dashboard() {
  const [userData, setUserData] = useState<UserData | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isCheckingIn, setIsCheckingIn] = useState(false);
  const [activeTab, setActiveTab] = useState<'wellbeing' | 'tracking'>('wellbeing');
  const [notification, setNotification] = useState<{ message: string; type: 'success' | 'error' } | null>(null);
  const [justCheckedIn, setJustCheckedIn] = useState(false);
  const [checkIns, setCheckIns] = useState<Array<{ id: number; source?: string; created_at: string }>>([]);
  const [isLoadingCheckIns, setIsLoadingCheckIns] = useState(false);

  // Estados para Círculos y Geocercas
  const [circlesList, setCirclesList] = useState<CircleData[]>([]);
  const [selectedCircleId, setSelectedCircleId] = useState<number | null>(null);
  const [newCircleName, setNewCircleName] = useState('');
  const [inviteCodeInput, setInviteCodeInput] = useState('');
  const [isCreatingCircle, setIsCreatingCircle] = useState(false);
  const [isJoiningCircle, setIsJoiningCircle] = useState(false);

  // Estados para Geocercas
  const [geofenceName, setGeofenceName] = useState('');
  const [geofenceRadius, setGeofenceRadius] = useState(200);
  const [geofenceType, setGeofenceType] = useState('entry_exit');
  const [geofenceUserId, setGeofenceUserId] = useState<string>(''); // vacio = toda la familia
  const [geofenceLatitude, setGeofenceLatitude] = useState<number | null>(null);
  const [geofenceLongitude, setGeofenceLongitude] = useState<number | null>(null);
  const [isCreatingGeofence, setIsCreatingGeofence] = useState(false);

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

  const fetchCircles = async () => {
    const token = localStorage.getItem('auth_token');
    if (!token) return;

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/circles`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/json',
        },
      });

      if (response.ok) {
        const data = await response.json();
        setCirclesList(data);
        if (data.length > 0 && !selectedCircleId) {
          setSelectedCircleId(data[0].id);
        }
      }
    } catch (err) {
      console.error('Error fetching circles:', err);
    }
  };

  const handleCreateCircle = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCircleName.trim()) return;

    setIsCreatingCircle(true);
    const token = localStorage.getItem('auth_token');
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/circles`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({ name: newCircleName }),
      });

      if (response.ok) {
        const newCircle = await response.json();
        showToast('Círculo creado exitosamente.', 'success');
        setNewCircleName('');
        await fetchCircles();
        setSelectedCircleId(newCircle.id);
      } else {
        const errData = await response.json();
        showToast(errData.message || 'Error al crear el círculo.', 'error');
      }
    } catch {
      showToast('Error de red al crear el círculo.', 'error');
    } finally {
      setIsCreatingCircle(false);
    }
  };

  const handleJoinCircle = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!inviteCodeInput.trim()) return;

    setIsJoiningCircle(true);
    const token = localStorage.getItem('auth_token');
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/circles/join`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({ invite_code: inviteCodeInput }),
      });

      if (response.ok) {
        const joinedCircle = await response.json();
        showToast('Te has unido al círculo exitosamente.', 'success');
        setInviteCodeInput('');
        await fetchCircles();
        setSelectedCircleId(joinedCircle.id);
      } else {
        const errData = await response.json();
        showToast(errData.message || 'Error al unirte al círculo.', 'error');
      }
    } catch {
      showToast('Error de red al unirte al círculo.', 'error');
    } finally {
      setIsJoiningCircle(false);
    }
  };

  const handleRemoveMember = async (circleId: number, memberId: number) => {
    const token = localStorage.getItem('auth_token');
    const isSelf = userData?.id === memberId;
    
    if (!confirm(isSelf ? '¿Estás seguro de que quieres salir de este círculo?' : '¿Estás seguro de que quieres expulsar a este miembro?')) {
      return;
    }

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/circles/${circleId}/members/${memberId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/json',
        },
      });

      if (response.ok) {
        showToast(isSelf ? 'Has salido del círculo.' : 'Miembro expulsado.', 'success');
        if (isSelf || circlesList.find(c => c.id === circleId)?.owner_id === memberId) {
          setSelectedCircleId(null);
        }
        await fetchCircles();
      } else {
        const errData = await response.json();
        showToast(errData.message || 'Error al realizar la acción.', 'error');
      }
    } catch {
      showToast('Error de red.', 'error');
    }
  };

  const handleCreateGeofence = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCircleId) return;
    if (!geofenceName.trim()) {
      showToast('Ingresa un nombre para el perímetro.', 'error');
      return;
    }
    if (geofenceLatitude === null || geofenceLongitude === null) {
      showToast('Haz clic en el mapa para marcar el centro del perímetro.', 'error');
      return;
    }

    setIsCreatingGeofence(true);
    const token = localStorage.getItem('auth_token');
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/circles/${selectedCircleId}/geofences`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
        body: JSON.stringify({
          name: geofenceName,
          radius: Number(geofenceRadius),
          type: geofenceType,
          latitude: geofenceLatitude,
          longitude: geofenceLongitude,
          user_id: geofenceUserId ? Number(geofenceUserId) : null,
        }),
      });

      if (response.ok) {
        showToast('Perímetro creado exitosamente.', 'success');
        setGeofenceName('');
        setGeofenceLatitude(null);
        setGeofenceLongitude(null);
        setGeofenceUserId('');
        await fetchCircles();
      } else {
        const errData = await response.json();
        showToast(errData.message || 'Error al crear el perímetro.', 'error');
      }
    } catch {
      showToast('Error de red al crear el perímetro.', 'error');
    } finally {
      setIsCreatingGeofence(false);
    }
  };

  const handleDeleteGeofence = async (geofenceId: number) => {
    if (!confirm('¿Estás seguro de que quieres eliminar este perímetro?')) return;
    
    const token = localStorage.getItem('auth_token');
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/geofences/${geofenceId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Accept': 'application/json',
        },
      });

      if (response.ok) {
        showToast('Perímetro eliminado.', 'success');
        await fetchCircles();
      } else {
        const errData = await response.json();
        showToast(errData.message || 'Error al eliminar el perímetro.', 'error');
      }
    } catch {
      showToast('Error de red.', 'error');
    }
  };

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchUserData();
      fetchCheckIns();
      fetchCircles();
    }, 0);

    const interval = setInterval(() => {
      fetchUserData();
      fetchCircles();
    }, 30000);

    return () => {
      clearTimeout(timer);
      clearInterval(interval);
    };
  }, [selectedCircleId]); // eslint-disable-line react-hooks/exhaustive-deps

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

        <main className="flex-1 p-4 md:p-8 pb-32">
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
                    initialEscalationEnabled={userData?.escalation_enabled || false}
                    initialEscalationIntervalMinutes={userData?.escalation_interval_minutes || 15}
                    initialShareContactResponses={userData?.share_contact_responses ?? true}
                    initialWifiCheckinEnabled={userData?.wifi_checkin_enabled || false}
                    initialSafeWifiSsid={userData?.safe_wifi_ssid || ''}
                    initialSensorCheckinEnabled={userData?.sensor_checkin_enabled || false}
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
                            <div>
                              <span className="text-sm font-semibold text-gray-700">Reporte de Bienestar Confirmado</span>
                              {checkIn.source && (
                                <p className="text-[10px] text-gray-400 mt-0.5 font-bold">
                                  Vía {
                                    checkIn.source === 'wifi' ? 'Wi-Fi seguro' :
                                    checkIn.source === 'movement' ? 'Sensor de movimiento' :
                                    checkIn.source === 'sms' ? 'SMS' :
                                    checkIn.source === 'whatsapp' ? 'WhatsApp' : 'Manual'
                                  }
                                </p>
                              )}
                            </div>
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
                {/* Selector / Acciones de Círculos */}
                <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 flex flex-col md:flex-row md:items-center justify-between gap-6">
                  <div>
                    <h2 className="text-2xl font-black text-gray-900">Mis Círculos de Seguridad</h2>
                    <p className="text-xs text-gray-500 font-medium">Coordina la ubicación y seguridad de tu familia o amigos.</p>
                  </div>
                  <div className="flex flex-wrap items-center gap-4">
                    {circlesList.length > 0 && (
                      <div className="flex items-center gap-2">
                        <label className="text-xs font-bold text-gray-400 uppercase tracking-wider">Ver Círculo:</label>
                        <select
                          value={selectedCircleId || ''}
                          onChange={(e) => {
                            setSelectedCircleId(Number(e.target.value));
                            setGeofenceLatitude(null);
                            setGeofenceLongitude(null);
                          }}
                          className="px-4 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm font-bold text-gray-800 focus:outline-none focus:border-red-500"
                        >
                          {circlesList.map((circle) => (
                            <option key={circle.id} value={circle.id}>
                              {circle.name}
                            </option>
                          ))}
                        </select>
                      </div>
                    )}
                  </div>
                </div>

                {circlesList.length === 0 ? (
                  // Estado vacío: Crear o Unirse
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100 flex flex-col justify-between min-h-[320px]">
                      <div>
                        <div className="w-12 h-12 bg-red-50 rounded-2xl flex items-center justify-center text-xl mb-6">➕</div>
                        <h3 className="text-lg font-black text-gray-900 mb-2">Crear un Nuevo Círculo</h3>
                        <p className="text-xs text-gray-500 leading-relaxed mb-6 font-medium">
                          Crea un círculo privado de seguridad familiar. Podrás invitar a tus seres queridos y monitorear su bienestar en tiempo real.
                        </p>
                      </div>
                      <form onSubmit={handleCreateCircle} className="space-y-4">
                        <input
                          type="text"
                          placeholder="Nombre del círculo (ej: Familia Pérez)"
                          value={newCircleName}
                          onChange={(e) => setNewCircleName(e.target.value)}
                          className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-red-500 font-semibold"
                        />
                        <button
                          type="submit"
                          disabled={isCreatingCircle}
                          className="w-full bg-red-600 hover:bg-red-700 disabled:bg-gray-300 text-white py-3 rounded-xl text-sm font-bold shadow-md transition-colors cursor-pointer"
                        >
                          {isCreatingCircle ? 'Creando...' : 'Crear Círculo'}
                        </button>
                      </form>
                    </div>

                    <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100 flex flex-col justify-between min-h-[320px]">
                      <div>
                        <div className="w-12 h-12 bg-blue-50 rounded-2xl flex items-center justify-center text-xl mb-6">🔑</div>
                        <h3 className="text-lg font-black text-gray-900 mb-2">Unirse a un Círculo Existente</h3>
                        <p className="text-xs text-gray-500 leading-relaxed mb-6 font-medium">
                          Si un familiar ya creó un círculo, pídele su código de invitación de 10 caracteres e ingrésalo a continuación.
                        </p>
                      </div>
                      <form onSubmit={handleJoinCircle} className="space-y-4">
                        <input
                          type="text"
                          maxLength={10}
                          placeholder="Código de invitación (10 caracteres)"
                          value={inviteCodeInput}
                          onChange={(e) => setInviteCodeInput(e.target.value)}
                          className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-red-500 uppercase font-semibold font-mono"
                        />
                        <button
                          type="submit"
                          disabled={isJoiningCircle}
                          className="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 text-white py-3 rounded-xl text-sm font-bold shadow-md transition-colors cursor-pointer"
                        >
                          {isJoiningCircle ? 'Uniéndose...' : 'Unirse al Círculo'}
                        </button>
                      </form>
                    </div>
                  </div>
                ) : (
                  // Círculo Seleccionado: Mapa, Miembros y Geocercas
                  (() => {
                    const circle = circlesList.find(c => c.id === selectedCircleId);
                    if (!circle) return null;

                    // Encontrar centro inicial del mapa
                    let mapCenter: [number, number] = [-34.6037, -58.3816];
                    const membersWithLocation = circle.users.filter((u: CircleData['users'][number]) => u.current_location);
                    if (membersWithLocation.length > 0) {
                      mapCenter = [
                        membersWithLocation[0].current_location.latitude,
                        membersWithLocation[0].current_location.longitude
                      ];
                    } else if (userData?.current_location) {
                      mapCenter = [userData.current_location.latitude, userData.current_location.longitude];
                    }

                    return (
                      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                        {/* Columna Principal: Mapa y Perímetros (2/3 de ancho) */}
                        <div className="lg:col-span-2 space-y-8">
                          {/* Mapa */}
                          <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100">
                            <div className="flex items-center justify-between mb-4">
                              <h3 className="text-lg font-black text-gray-900">Ubicaciones del Círculo: {circle.name}</h3>
                              <span className="text-[10px] text-gray-400 font-bold uppercase tracking-wider">
                                Haz clic en el mapa para ubicar un perímetro
                              </span>
                            </div>
                            <div className="h-[480px] w-full relative rounded-2xl overflow-hidden border border-gray-100">
                              <TrackingMap
                                members={circle.users}
                                geofences={circle.geofences}
                                center={mapCenter}
                                onMapClick={(lat, lng) => {
                                  setGeofenceLatitude(lat);
                                  setGeofenceLongitude(lng);
                                  showToast(`Ubicación marcada: ${lat.toFixed(5)}, ${lng.toFixed(5)}`, 'success');
                                }}
                                clickedCoords={geofenceLatitude && geofenceLongitude ? [geofenceLatitude, geofenceLongitude] : null}
                              />
                            </div>
                          </div>

                          {/* Gestión de Perímetros (Geocercas) */}
                          <div className="bg-white p-8 rounded-3xl shadow-sm border border-gray-100 space-y-6">
                            <div>
                              <h3 className="text-lg font-black text-gray-900">Perímetros de Seguridad (Geocercas)</h3>
                              <p className="text-xs text-gray-500 font-medium">Recibe alertas automáticas cuando tus familiares entren o salgan.</p>
                            </div>

                            {/* Formulario de Perímetro */}
                            <form onSubmit={handleCreateGeofence} className="p-6 bg-gray-50 rounded-2xl border border-gray-100 grid grid-cols-1 md:grid-cols-2 gap-4">
                              <div className="md:col-span-2">
                                <h4 className="text-xs font-bold text-gray-400 uppercase tracking-widest">Crear Nuevo Perímetro</h4>
                              </div>
                              
                              <div>
                                <label className="text-xs font-bold text-gray-700 block mb-1">Nombre</label>
                                <input
                                  type="text"
                                  placeholder="Ej: Escuela, Casa, Trabajo"
                                  value={geofenceName}
                                  onChange={(e) => setGeofenceName(e.target.value)}
                                  className="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-red-500 font-semibold"
                                />
                              </div>

                              <div>
                                <label className="text-xs font-bold text-gray-700 block mb-1">Miembro (Alerta Personalizada)</label>
                                <select
                                  value={geofenceUserId}
                                  onChange={(e) => setGeofenceUserId(e.target.value)}
                                  className="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-red-500 font-bold text-gray-700"
                                >
                                  <option value="">Toda la Familia (Cualquiera)</option>
                                  {circle.users.map((m: CircleData['users'][number]) => (
                                    <option key={m.id} value={m.id}>
                                      Solo: {m.name}
                                    </option>
                                  ))}
                                </select>
                              </div>

                              <div>
                                <label className="text-xs font-bold text-gray-700 block mb-1">Radio (Metros)</label>
                                <input
                                  type="number"
                                  min={10}
                                  max={5000}
                                  value={geofenceRadius}
                                  onChange={(e) => setGeofenceRadius(Number(e.target.value))}
                                  className="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-red-500 font-semibold"
                                />
                              </div>

                              <div>
                                <label className="text-xs font-bold text-gray-700 block mb-1">Tipo de Evento</label>
                                <select
                                  value={geofenceType}
                                  onChange={(e) => setGeofenceType(e.target.value)}
                                  className="w-full px-4 py-2.5 bg-white border border-gray-200 rounded-xl text-sm focus:outline-none focus:border-red-500 font-bold text-gray-700"
                                >
                                  <option value="entry_exit">Al Entrar y Salir</option>
                                  <option value="entry">Solo al Entrar</option>
                                  <option value="exit">Solo al Salir</option>
                                </select>
                              </div>

                              <div className="md:col-span-2 flex flex-col md:flex-row md:items-center justify-between gap-4 py-2 border-t border-gray-200/50 mt-2">
                                <span className="text-xs font-bold text-gray-500">
                                  {geofenceLatitude && geofenceLongitude 
                                    ? `📍 Centro marcado: ${geofenceLatitude.toFixed(5)}, ${geofenceLongitude.toFixed(5)}`
                                    : '⚠️ Haz clic en el mapa de arriba para marcar el centro del perímetro.'
                                  }
                                </span>
                                <button
                                  type="submit"
                                  disabled={isCreatingGeofence}
                                  className="bg-red-600 hover:bg-red-700 disabled:bg-gray-300 text-white px-5 py-2.5 rounded-xl text-xs font-bold shadow-md transition-colors cursor-pointer"
                                >
                                  {isCreatingGeofence ? 'Creando...' : 'Guardar Perímetro'}
                                </button>
                              </div>
                            </form>

                            {/* Lista de Geocercas */}
                            <div className="space-y-3">
                              <h4 className="text-xs font-bold text-gray-400 uppercase tracking-widest">Perímetros Configurados</h4>
                              {circle.geofences.length === 0 ? (
                                <div className="py-4 text-center bg-gray-50 rounded-2xl border border-dashed border-gray-200">
                                  <p className="text-xs text-gray-400 italic font-medium">No hay perímetros configurados en este círculo.</p>
                                </div>
                              ) : (
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                  {circle.geofences.map((gf: CircleData['geofences'][number]) => (
                                    <div key={gf.id} className="p-4 bg-gray-50 rounded-2xl border border-transparent hover:border-red-100 flex items-center justify-between gap-4 transition-all">
                                      <div className="flex-1">
                                        <div className="flex items-center gap-2 flex-wrap">
                                          <span className="text-sm font-bold text-gray-900">{gf.name}</span>
                                          <span className="text-[9px] bg-red-100 text-red-700 font-extrabold px-2 py-0.5 rounded-full uppercase">
                                            {gf.radius}m
                                          </span>
                                        </div>
                                        <p className="text-[10px] text-gray-500 mt-0.5 font-semibold">
                                          Tipo: {gf.type === 'entry' ? 'Entrada' : gf.type === 'exit' ? 'Salida' : 'Entrada/Salida'}
                                        </p>
                                        {gf.user && (
                                          <p className="text-[10px] text-blue-600 font-bold mt-1 bg-blue-50 px-2 py-0.5 rounded-full inline-block">
                                            👤 Monitorea solo a: {gf.user.name}
                                          </p>
                                        )}
                                      </div>
                                      <button
                                        onClick={() => handleDeleteGeofence(gf.id)}
                                        className="text-xs text-gray-400 hover:text-red-600 font-bold transition-colors cursor-pointer"
                                      >
                                        Eliminar
                                      </button>
                                    </div>
                                  ))}
                                </div>
                              )}
                            </div>
                          </div>
                        </div>

                        {/* Columna Lateral: Miembros y Código de Invitación (1/3 de ancho) */}
                        <div className="space-y-8">
                          {/* Compartir Código */}
                          <div className="bg-gradient-to-br from-red-500 to-rose-600 text-white p-6 rounded-3xl shadow-sm border border-transparent">
                            <h3 className="font-black text-lg mb-2">Invitar a un Familiar</h3>
                            <p className="text-xs text-red-100 leading-relaxed mb-4 font-medium">
                              Comparte este código para que tus familiares se unan a este círculo.
                            </p>
                            <div className="flex items-center gap-2 bg-white/10 p-3 rounded-2xl backdrop-blur-sm">
                              <span className="flex-1 text-center font-mono font-black text-xl tracking-wider select-all">
                                {circle.invite_code}
                              </span>
                              <button
                                onClick={() => {
                                  navigator.clipboard.writeText(circle.invite_code);
                                  showToast('¡Código copiado al portapapeles!', 'success');
                                }}
                                className="bg-white/20 hover:bg-white/30 px-3.5 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer"
                              >
                                Copiar
                              </button>
                            </div>
                          </div>

                          {/* Lista de Miembros */}
                          <div className="bg-white p-6 rounded-3xl shadow-sm border border-gray-100 space-y-6">
                            <div>
                              <h3 className="text-lg font-black text-gray-900">Miembros ({circle.users.length})</h3>
                              <p className="text-xs text-gray-500 font-medium">Personas en este círculo de seguridad.</p>
                            </div>

                            <div className="space-y-4">
                              {circle.users.map((member: CircleData['users'][number]) => {
                                const isSelf = member.id === userData?.id;
                                const isOwner = circle.owner_id === member.id;
                                const isCurrentUserAdmin = circle.users.find((u: CircleData['users'][number]) => u.id === userData?.id)?.pivot?.role === 'admin';

                                return (
                                  <div key={member.id} className="flex items-center justify-between gap-4 p-3 bg-gray-50 rounded-2xl border border-transparent hover:border-red-100 transition-all">
                                    <div className="flex items-center gap-3">
                                      <div className="w-10 h-10 bg-red-50 rounded-full flex items-center justify-center text-red-600 font-bold border border-red-100">
                                        {member.name.charAt(0)}
                                      </div>
                                      <div>
                                        <div className="flex items-center gap-1.5">
                                          <span className="text-sm font-bold text-gray-800">
                                            {member.name} {isSelf && '(Tú)'}
                                          </span>
                                          {member.is_premium && (
                                            <span className="text-[10px] text-yellow-600" title="Premium">⭐</span>
                                          )}
                                        </div>
                                        <p className="text-[10px] text-gray-400 font-semibold">
                                          {isOwner ? 'Dueño' : member.pivot.role === 'admin' ? 'Administrador' : 'Miembro'}
                                        </p>
                                        {member.current_location && (
                                          <p className="text-[9px] text-emerald-600 font-bold mt-0.5">
                                            Ubicación Activa
                                          </p>
                                        )}
                                      </div>
                                    </div>

                                    {/* Botón de Salida / Expulsión */}
                                    {isSelf ? (
                                      <button
                                        onClick={() => handleRemoveMember(circle.id, member.id)}
                                        className="text-[10px] bg-red-50 text-red-600 hover:bg-red-100 px-3 py-1.5 rounded-xl font-extrabold transition-all cursor-pointer"
                                      >
                                        Salir
                                      </button>
                                    ) : (
                                      (circle.owner_id === userData?.id || (isCurrentUserAdmin && !isOwner)) && (
                                        <button
                                          onClick={() => handleRemoveMember(circle.id, member.id)}
                                          className="text-[10px] text-gray-400 hover:text-red-600 px-2 py-1.5 font-bold transition-all cursor-pointer"
                                        >
                                          Expulsar
                                        </button>
                                      )
                                    )}
                                  </div>
                                );
                              })}
                            </div>
                            
                            {/* Borrar círculo completo (Solo dueño) */}
                            {circle.owner_id === userData?.id && (
                              <button
                                onClick={async () => {
                                  if (confirm('¿Estás seguro de que quieres disolver el círculo completo? Todos los miembros y geocercas se eliminarán permanentemente.')) {
                                    const token = localStorage.getItem('auth_token');
                                    try {
                                      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/circles/${circle.id}`, {
                                        method: 'DELETE',
                                        headers: {
                                          'Authorization': `Bearer ${token}`,
                                          'Accept': 'application/json',
                                        },
                                      });
                                      if (response.ok) {
                                        showToast('Círculo eliminado con éxito.', 'success');
                                        setSelectedCircleId(null);
                                        await fetchCircles();
                                      } else {
                                        showToast('Error al eliminar el círculo.', 'error');
                                      }
                                    } catch {
                                      showToast('Error de red al intentar eliminar el círculo.', 'error');
                                    }
                                  }
                                }}
                                className="w-full py-3 bg-red-50 hover:bg-red-100 text-red-600 text-xs font-bold rounded-2xl transition-colors mt-6 border border-dashed border-red-200 cursor-pointer text-center"
                              >
                                Disolver Círculo de Seguridad
                              </button>
                            )}
                          </div>
                        </div>
                      </div>
                    );
                  })()
                )}
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
