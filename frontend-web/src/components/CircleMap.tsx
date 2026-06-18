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
          return (
            <Marker 
              key={member.id} 
              position={[member.current_location.latitude, member.current_location.longitude]}
              icon={MemberIcon}
            >
              <Popup>
                <div className="text-xs">
                  <p className="font-bold text-gray-900">{member.name}</p>
                  <p className="text-gray-500 text-[10px]">{member.email}</p>
                  <p className="text-[10px] text-gray-400 mt-1">
                    Última ubicación: {new Date(member.current_location.updated_at).toLocaleString()}
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
