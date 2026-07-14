'use client';

import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
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

interface MapProps {
  center: [number, number];
  zoom?: number;
  isCrash?: boolean;
  gForce?: number | null;
}

const crashIcon = typeof window !== 'undefined' ? L.divIcon({
  html: `<div class="relative flex items-center justify-center">
    <div class="absolute w-12 h-12 bg-red-500 rounded-full opacity-60 animate-ping"></div>
    <div class="relative bg-red-600 text-white p-2.5 rounded-full border-2 border-white shadow-lg text-lg flex items-center justify-center">
      🚗
    </div>
  </div>`,
  className: 'custom-crash-icon',
  iconSize: [48, 48],
  iconAnchor: [24, 24],
}) : undefined;

function RecenterMap({ center }: { center: [number, number] }) {
  const map = useMap();
  useEffect(() => {
    map.setView(center);
  }, [center, map]);
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

export default function EmergencyMap({ center, zoom = 15, isCrash = false, gForce = null }: MapProps) {
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsMounted(true);
    }, 0);
    return () => clearTimeout(timer);
  }, []);

  if (!isMounted) return null;

  return (
    <div className="h-full w-full rounded-xl overflow-hidden shadow-inner border border-red-100">
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
        <AnimatedMarker 
          position={center} 
          icon={isCrash && crashIcon ? crashIcon : DefaultIcon}
        >
          <Popup>
            {isCrash ? (
              <div className="text-center p-1 font-sans">
                <p className="font-extrabold text-red-600 m-0">🚨 IMPACTO DETECTADO</p>
                {gForce !== null && <p className="text-xs font-bold text-gray-700 mt-1 m-0">Fuerza: {gForce.toFixed(1)} G</p>}
                <p className="text-xs text-gray-500 mt-1 m-0">Velocidad: 0 km/h (Inmóvil)</p>
              </div>
            ) : (
              'Última ubicación conocida'
            )}
          </Popup>
        </AnimatedMarker>
        <RecenterMap center={center} />
      </MapContainer>
    </div>
  );
}
