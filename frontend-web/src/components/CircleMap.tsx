'use client';

import { MapContainer, TileLayer, Marker, Popup, Circle as LeafletCircle, useMap, useMapEvents, Polyline, Tooltip } from 'react-leaflet';
import L from 'leaflet';
import { useEffect, useState, Fragment } from 'react';

// Fix for default marker icons in Leaflet + Next.js
const DefaultIcon = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
});

L.Marker.prototype.options.icon = DefaultIcon;

// Custom icon for other members (e.g. Red marker)
const MemberIcon = L.icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-red.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
});

// Gold icon for historical playback marker
const PlaybackIcon = L.icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-gold.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
});

// Custom icon builder for driving members
const getCarIcon = (exceeded: boolean) => L.divIcon({
  html: `
    <div class="relative flex items-center justify-center w-10 h-10 ${exceeded ? 'bg-red-600 animate-pulse border-red-300' : 'bg-emerald-600 border-white'} rounded-full border-2 shadow-xl">
      <span class="text-lg">🚗</span>
    </div>
  `,
  className: 'custom-car-icon',
  iconSize: [40, 40],
  iconAnchor: [20, 20],
  popupAnchor: [0, -20],
});

// Haversine distance calculator
export function haversineDistance(lat1: number, lon1: number, lat2: number, lon2: number): number {
  const R = 6371e3; // Earth radius in meters
  const phi1 = (lat1 * Math.PI) / 180;
  const phi2 = (lat2 * Math.PI) / 180;
  const deltaPhi = ((lat2 - lat1) * Math.PI) / 180;
  const deltaLambda = ((lon2 - lon1) * Math.PI) / 180;

  const a =
    Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
    Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c; // in meters
}

interface Member {
  id: number;
  name: string;
  email: string;
  current_location?: {
    latitude: number;
    longitude: number;
    updated_at: string;
    battery_level?: number | null;
    is_battery_low?: boolean;
    is_tracking_active?: boolean;
    gps_enabled?: boolean;
    last_seen_at?: string | null;
    recorded_at?: string | null;
    is_offline?: boolean;
    speed?: number | null;
    is_driving?: boolean;
  } | null;
  active_emergency_alerts?: Array<{
    id: string;
    user_id: number;
    type: string;
    status: string;
    audio_url?: string | null;
    expires_at: string;
  }>;
}

interface Geofence {
  id: number;
  name: string;
  radius: number;
  type: string;
  latitude: number;
  longitude: number;
  user?: {
    id: number;
    name: string;
  } | null;
}

interface DynamicGeofence {
  id: number;
  initiator_id: number;
  target_id: number;
  safe_radius_meters: number;
}

interface MapProps {
  members: Member[];
  geofences: Geofence[];
  activeGeofences?: DynamicGeofence[];
  center: [number, number];
  zoom?: number;
  onMapClick?: (lat: number, lng: number) => void;
  clickedCoords?: [number, number] | null;
  historyRoute?: Array<{
    id: number;
    latitude: number;
    longitude: number;
    recorded_at: string;
    accuracy?: number;
  }> | null;
  playbackIndex?: number | null;
  speedLimit?: number;
}

function RecenterMap({ center }: { center: [number, number] }) {
  const map = useMap();
  useEffect(() => {
    map.setView(center);
  }, [center, map]);
  return null;
}

function MapEventsHandler({ onMapClick }: { onMapClick?: (lat: number, lng: number) => void }) {
  useMapEvents({
    click(e) {
      if (onMapClick) {
        onMapClick(e.latlng.lat, e.latlng.lng);
      }
    },
  });
  return null;
}

function AnimatedMarker({ position, children, ...props }: any) {
  const [currentPos, setCurrentPos] = useState<[number, number]>(position);

  useEffect(() => {
    const startPos = currentPos;
    const endPos = position;
    if (startPos[0] === endPos[0] && startPos[1] === endPos[1]) return;

    const duration = 1500; // 1.5s
    const startTime = performance.now();
    let frameId: number;

    const animate = (time: number) => {
      const elapsed = time - startTime;
      const t = Math.min(elapsed / duration, 1);
      const lat = startPos[0] + (endPos[0] - startPos[0]) * t;
      const lng = startPos[1] + (endPos[1] - startPos[1]) * t;
      setCurrentPos([lat, lng]);

      if (t < 1) {
        frameId = requestAnimationFrame(animate);
      }
    };

    frameId = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(frameId);
  }, [position]);

  return (
    <Marker position={currentPos} {...props}>
      {children}
    </Marker>
  );
}

export default function CircleMap({ 
  members, 
  geofences, 
  activeGeofences: propActiveGeofences,
  center, 
  zoom = 13, 
  onMapClick,
  clickedCoords,
  historyRoute,
  playbackIndex,
  speedLimit
}: MapProps) {
  const [isMounted, setIsMounted] = useState(false);
  const [fetchedActiveGeofences, setFetchedActiveGeofences] = useState<DynamicGeofence[]>([]);
  const activeGeofences = propActiveGeofences || fetchedActiveGeofences;

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsMounted(true);
    }, 0);
    return () => clearTimeout(timer);
  }, []);

  useEffect(() => {
    if (propActiveGeofences) return;

    const fetchActiveGeofences = async () => {
      const token = localStorage.getItem('auth_token');
      if (!token) return;
      try {
        const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/dynamic-geofences/active`, {
          headers: {
            'Authorization': `Bearer ${token}`,
          }
        });
        if (response.ok) {
          const data = await response.json();
          setFetchedActiveGeofences(data);
        }
      } catch (err) {
        console.error('Failed to fetch active geofences on map:', err);
      }
    };

    fetchActiveGeofences();
    const interval = setInterval(fetchActiveGeofences, 10000);
    return () => clearInterval(interval);
  }, [propActiveGeofences]);

  if (!isMounted) return null;

  // Render geofence color based on type
  const getGeofenceColor = (type: string) => {
    switch (type) {
      case 'entry':
        return '#10B981'; // Green
      case 'exit':
        return '#F59E0B'; // Orange
      case 'entry_exit':
      default:
        return '#3B82F6'; // Blue
    }
  };

  return (
    <div className="h-full w-full rounded-2xl overflow-hidden shadow-inner border border-gray-100">
      <link
        rel="stylesheet"
        href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
        integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
        crossOrigin=""
      />
      <MapContainer 
        center={center} 
        zoom={zoom} 
        scrollWheelZoom={true}
        style={{ height: '100%', width: '100%' }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        {/* Member Markers */}
        {members.map(member => {
          if (!member.current_location) return null;
          const loc = member.current_location;
          if (loc.latitude === null || loc.latitude === undefined || loc.longitude === null || loc.longitude === undefined) return null;
          const isTrackingActive = loc.is_tracking_active !== false;
          const isGpsEnabled = loc.gps_enabled !== false;
          const isOffline = !!loc.is_offline;
          const activeSos = member.active_emergency_alerts?.find(alert => alert.type === 'silent_sos' && alert.status === 'active');
          const hasSilentSos = !!activeSos;
          const hasIssue = (!isTrackingActive || !isGpsEnabled || isOffline) && !hasSilentSos;

          const isDriving = !!loc.is_driving;
          const speed = loc.speed ?? 0;
          const isSpeeding = isDriving && speedLimit !== undefined && speed > speedLimit;

          let iconToUse: L.Icon | L.DivIcon = MemberIcon;
          if (isDriving) {
            iconToUse = getCarIcon(isSpeeding);
          }

          return (
            <AnimatedMarker 
              key={member.id} 
              position={[Number(loc.latitude), Number(loc.longitude)]}
              icon={iconToUse}
              opacity={hasSilentSos ? 1.0 : (hasIssue ? 0.5 : 1.0)}
            >
              <Popup>
                <div className="text-xs min-w-[180px] font-sans">
                  {hasSilentSos && activeSos && (
                    <div className="mb-2 p-2 bg-red-600 text-white font-extrabold text-[10px] rounded text-center animate-pulse">
                      🚨 SOS SILENCIOSO ACTIVO 🚨
                      {activeSos.audio_url && (
                        <div className="mt-1 pb-1">
                          <audio src={activeSos.audio_url} controls className="w-full h-8 scale-95" />
                        </div>
                      )}
                    </div>
                  )}

                  {isDriving && (
                    <div className={`mb-2 p-2 ${isSpeeding ? 'bg-red-600 text-white animate-pulse font-extrabold' : 'bg-emerald-600 text-white font-bold'} text-[10px] rounded text-center`}>
                      {isSpeeding ? '⚠️ EXCESO DE VELOCIDAD DETECTADO' : '🚗 EN TRAYECTO VEHICULAR'}
                    </div>
                  )}
                  <div className="flex items-center justify-between gap-2 border-b border-gray-100 pb-1.5 mb-1.5">
                    <p className="font-bold text-gray-900 leading-none">{member.name}</p>
                    {loc.battery_level !== undefined && loc.battery_level !== null && (
                      (() => {
                        const lvl = loc.battery_level;
                        const pct = Math.round(lvl * 100);
                        let colorClass = 'text-emerald-500 bg-emerald-50 border-emerald-100';
                        if (lvl < 0.15) {
                          colorClass = 'text-red-600 bg-red-50 border-red-200 animate-pulse font-black';
                        } else if (lvl < 0.50) {
                          colorClass = 'text-amber-600 bg-amber-50 border-amber-100';
                        }
                        return (
                          <span className={`text-[9px] px-1.5 py-0.5 rounded border ${colorClass} flex items-center gap-0.5 font-bold`}>
                            🔋 {pct}%
                          </span>
                        );
                      })()
                    )}
                  </div>
                  
                  {isDriving && (
                    <div className="mb-2 space-y-1">
                      <div className={`text-[9px] ${isSpeeding ? 'text-red-600 bg-red-50 border-red-200 font-extrabold' : 'text-emerald-600 bg-emerald-50 border-emerald-200 font-bold'} px-1.5 py-0.5 rounded border flex items-center gap-1`}>
                        ⚡ Velocidad: {Math.round(speed)} km/h {speedLimit !== undefined && `(Límite: ${speedLimit} km/h)`}
                      </div>
                    </div>
                  )}

                  {/* Alertas de Sensor en el Popup */}
                  {hasIssue && (
                    <div className="mb-2 space-y-1">
                      {!isTrackingActive && (
                        <div className="text-[9px] text-gray-500 bg-gray-100 border border-gray-200 px-1.5 py-0.5 rounded font-black flex items-center gap-1">
                          📴 Rastreo apagado por el usuario
                        </div>
                      )}
                      {isTrackingActive && !isGpsEnabled && (
                        <div className="text-[9px] text-amber-600 bg-amber-50 border border-amber-200 px-1.5 py-0.5 rounded font-black flex items-center gap-1">
                          ⚠️ GPS Desactivado en el dispositivo
                        </div>
                      )}
                      {isTrackingActive && isOffline && (
                        <div className="text-[9px] text-red-600 bg-red-50 border border-red-200 px-1.5 py-0.5 rounded font-black flex items-center gap-1 animate-pulse">
                          🌐 Sin señal (Desconectado/Modo Avión)
                        </div>
                      )}
                    </div>
                  )}

                  <p className="text-gray-500 text-[10px]">{member.email}</p>
                  <p className="text-[10px] text-gray-400 mt-1">
                    Visto por última vez: {loc.last_seen_at ? new Date(loc.last_seen_at).toLocaleString() : new Date(loc.updated_at).toLocaleString()}
                  </p>
                </div>
              </Popup>
            </AnimatedMarker>
          );
        })}

        {/* Geofences circles */}
        {geofences.map(geofence => (
          <LeafletCircle
            key={geofence.id}
            center={[Number(geofence.latitude), Number(geofence.longitude)]}
            radius={geofence.radius}
            pathOptions={{
              color: getGeofenceColor(geofence.type),
              fillColor: getGeofenceColor(geofence.type),
              fillOpacity: 0.15,
              weight: 2
            }}
          >
            <Popup>
              <div className="text-xs">
                <p className="font-bold text-gray-950">Perímetro: {geofence.name}</p>
                <p className="text-[10px] text-gray-600">Radio: {geofence.radius}m | Tipo: {
                  geofence.type === 'entry' ? 'Entrada' : geofence.type === 'exit' ? 'Salida' : 'Entrada y Salida'
                }</p>
                {geofence.user && (
                  <p className="text-[10px] text-indigo-600 font-semibold mt-1">
                    Solo para: {geofence.user.name}
                  </p>
                )}
              </div>
            </Popup>
          </LeafletCircle>
        ))}

        {/* Active Dynamic Geofences */}
        {activeGeofences.map(geofence => {
          const initiator = members.find(m => m.id === geofence.initiator_id);
          const target = members.find(m => m.id === geofence.target_id);

          if (!initiator?.current_location || !target?.current_location) return null;

          const initLoc = initiator.current_location;
          const targLoc = target.current_location;

          if (initLoc.latitude === null || initLoc.latitude === undefined || initLoc.longitude === null || initLoc.longitude === undefined) return null;
          if (targLoc.latitude === null || targLoc.latitude === undefined || targLoc.longitude === null || targLoc.longitude === undefined) return null;

          const distance = haversineDistance(
            Number(initLoc.latitude),
            Number(initLoc.longitude),
            Number(targLoc.latitude),
            Number(targLoc.longitude)
          );
          const isBreached = distance > geofence.safe_radius_meters;

          const color = isBreached ? '#ef4444' : '#3b82f6';

          return (
            <Fragment key={`dynamic-gf-${geofence.id}`}>
              {/* Círculo de Seguridad sobre el Tutor */}
              <LeafletCircle
                center={[Number(initLoc.latitude), Number(initLoc.longitude)]}
                radius={geofence.safe_radius_meters}
                pathOptions={{
                  color: color,
                  fillColor: color,
                  fillOpacity: 0.05,
                  weight: 1.5,
                  dashArray: '5, 10',
                }}
              />

              {/* Línea Conectora entre Tutor y Familiar */}
              <Polyline
                positions={[
                  [Number(initLoc.latitude), Number(initLoc.longitude)],
                  [Number(targLoc.latitude), Number(targLoc.longitude)]
                ]}
                pathOptions={{
                  color: color,
                  weight: 2,
                  dashArray: '4, 4',
                }}
              >
                <Tooltip permanent direction="center" className="bg-white px-2 py-1 rounded shadow-md border border-gray-100 font-sans text-[10px] font-bold text-gray-700">
                  <span>📡 Radar: {Math.round(distance)}m</span>
                  {isBreached && <span className="ml-1 text-red-600 block text-[8px] animate-pulse uppercase">¡Fuera de Rango!</span>}
                </Tooltip>
              </Polyline>
            </Fragment>
          );
        })}

        {/* Temporary clicked location marker */}
        {clickedCoords && (
          <Marker position={clickedCoords}>
            <Popup>
              <p className="text-xs font-bold text-gray-900">Ubicación seleccionada para geocerca</p>
            </Popup>
          </Marker>
        )}

        {/* Route History Rendering */}
        {historyRoute && historyRoute.length > 0 && (
          <>
            <Polyline
              positions={historyRoute.map(pt => [Number(pt.latitude), Number(pt.longitude)])}
              pathOptions={{
                color: '#6366F1', // Indigo
                weight: 4,
                opacity: 0.8,
                dashArray: '5, 10'
              }}
            />
            {historyRoute.map((pt, idx) => (
              <LeafletCircle
                key={`pt-${pt.id}-${idx}`}
                center={[Number(pt.latitude), Number(pt.longitude)]}
                radius={3}
                pathOptions={{
                  color: '#4F46E5',
                  fillColor: '#4F46E5',
                  fillOpacity: 1
                }}
              />
            ))}
          </>
        )}

        {/* Route History Playback Marker */}
        {historyRoute && playbackIndex !== undefined && playbackIndex !== null && historyRoute[playbackIndex] && (
          (() => {
            const activePt = historyRoute[playbackIndex];
            let speedText = 'Calculando...';
            if (playbackIndex > 0) {
              const prev = historyRoute[playbackIndex - 1];
              const dist = haversineDistance(
                Number(prev.latitude),
                Number(prev.longitude),
                Number(activePt.latitude),
                Number(activePt.longitude)
              );
              const timeDiff = (new Date(activePt.recorded_at).getTime() - new Date(prev.recorded_at).getTime()) / 1000;
              if (timeDiff > 0) {
                const speedKmh = (dist / timeDiff) * 3.6;
                speedText = `${speedKmh.toFixed(1)} km/h`;
              } else {
                speedText = '0.0 km/h';
              }
            } else {
              speedText = 'Inicio del recorrido';
            }
            return (
              <Marker
                position={[Number(activePt.latitude), Number(activePt.longitude)]}
                icon={PlaybackIcon}
              >
                <Popup>
                  <div className="text-xs font-sans p-1">
                    <p className="font-bold text-indigo-700">Reproducción de Trayecto</p>
                    <p className="text-gray-900 mt-1 font-semibold">🕒 {new Date(activePt.recorded_at).toLocaleTimeString()}</p>
                    <p className="text-gray-600">📅 {new Date(activePt.recorded_at).toLocaleDateString()}</p>
                    <p className="text-gray-800 font-bold mt-1">⚡ Velocidad: {speedText}</p>
                    {activePt.accuracy && (
                      <p className="text-gray-400 text-[10px]">Precisión: {Number(activePt.accuracy).toFixed(1)}m</p>
                    )}
                  </div>
                </Popup>
              </Marker>
            );
          })()
        )}

        <RecenterMap center={center} />
        <MapEventsHandler onMapClick={onMapClick} />
      </MapContainer>
    </div>
  );
}
