'use client';

import { MapContainer, TileLayer, Marker, Popup, Circle as LeafletCircle, useMap, useMapEvents, Polyline } from 'react-leaflet';
import L from 'leaflet';
import { useEffect, useState } from 'react';

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
    is_offline?: boolean;
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

interface MapProps {
  members: Member[];
  geofences: Geofence[];
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

export default function CircleMap({ 
  members, 
  geofences, 
  center, 
  zoom = 13, 
  onMapClick,
  clickedCoords,
  historyRoute,
  playbackIndex
}: MapProps) {
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsMounted(true);
    }, 0);
    return () => clearTimeout(timer);
  }, []);

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

          return (
            <Marker 
              key={member.id} 
              position={[loc.latitude, loc.longitude]}
              icon={MemberIcon}
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
            </Marker>
          );
        })}

        {/* Geofences circles */}
        {geofences.map(geofence => (
          <LeafletCircle
            key={geofence.id}
            center={[geofence.latitude, geofence.longitude]}
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
              positions={historyRoute.map(pt => [pt.latitude, pt.longitude])}
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
                center={[pt.latitude, pt.longitude]}
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
              const dist = haversineDistance(prev.latitude, prev.longitude, activePt.latitude, activePt.longitude);
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
                position={[activePt.latitude, activePt.longitude]}
                icon={PlaybackIcon}
              >
                <Popup>
                  <div className="text-xs font-sans p-1">
                    <p className="font-bold text-indigo-700">Reproducción de Trayecto</p>
                    <p className="text-gray-900 mt-1 font-semibold">🕒 {new Date(activePt.recorded_at).toLocaleTimeString()}</p>
                    <p className="text-gray-600">📅 {new Date(activePt.recorded_at).toLocaleDateString()}</p>
                    <p className="text-gray-800 font-bold mt-1">⚡ Velocidad: {speedText}</p>
                    {activePt.accuracy && (
                      <p className="text-gray-400 text-[10px]">Precisión: {activePt.accuracy.toFixed(1)}m</p>
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
