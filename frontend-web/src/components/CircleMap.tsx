'use client';

import { MapContainer, TileLayer, Marker, Popup, Circle as LeafletCircle, useMap, useMapEvents } from 'react-leaflet';
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
  clickedCoords
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
          const isTrackingActive = loc.is_tracking_active !== false;
          const isGpsEnabled = loc.gps_enabled !== false;
          const isOffline = !!loc.is_offline;
          const hasIssue = !isTrackingActive || !isGpsEnabled || isOffline;

          return (
            <Marker 
              key={member.id} 
              position={[loc.latitude, loc.longitude]}
              icon={MemberIcon}
              opacity={hasIssue ? 0.5 : 1.0}
            >
              <Popup>
                <div className="text-xs min-w-[180px] font-sans">
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

        <RecenterMap center={center} />
        <MapEventsHandler onMapClick={onMapClick} />
      </MapContainer>
    </div>
  );
}
