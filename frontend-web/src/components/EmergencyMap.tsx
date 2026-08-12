'use client';

import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import L from 'leaflet';
import { useEffect, useState, useMemo } from 'react';

interface MapProps {
  center: [number, number];
  zoom?: number;
  isCrash?: boolean;
  gForce?: number | null;
}

function RecenterMap({ center }: { center: [number, number] }) {
  const map = useMap();
  useEffect(() => {
    if (!isNaN(center[0]) && !isNaN(center[1])) {
      map.setView(center);
    }
  }, [center, map]);
  return null;
}

function AnimatedMarker({ position, children, ...props }: any) {
  const [currentPos, setCurrentPos] = useState<[number, number]>(() => [
    Number(position[0]) || 0,
    Number(position[1]) || 0,
  ]);

  useEffect(() => {
    const lat1 = Number(currentPos[0]);
    const lng1 = Number(currentPos[1]);
    const lat2 = Number(position[0]);
    const lng2 = Number(position[1]);

    if (isNaN(lat2) || isNaN(lng2)) return;
    if (lat1 === lat2 && lng1 === lng2) return;

    const duration = 1500; // 1.5s
    const startTime = performance.now();
    let frameId: number;

    const animate = (time: number) => {
      const elapsed = time - startTime;
      const t = Math.min(elapsed / duration, 1);
      const lat = lat1 + (lat2 - lat1) * t;
      const lng = lng1 + (lng2 - lng1) * t;

      if (!isNaN(lat) && !isNaN(lng)) {
        setCurrentPos([lat, lng]);
      }

      if (t < 1) {
        frameId = requestAnimationFrame(animate);
      }
    };

    frameId = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(frameId);
  }, [position]);

  const validPos: [number, number] = [
    Number(currentPos[0]) || 0,
    Number(currentPos[1]) || 0,
  ];

  return (
    <Marker position={validPos} {...props}>
      {children}
    </Marker>
  );
}

export default function EmergencyMap({ center, zoom = 15, isCrash = false, gForce = null }: MapProps) {
  const [isMounted, setIsMounted] = useState(false);

  const safeCenter: [number, number] = useMemo(() => {
    const lat = Number(center[0]);
    const lng = Number(center[1]);
    return [isNaN(lat) ? 0 : lat, isNaN(lng) ? 0 : lng];
  }, [center]);

  const defaultIcon = useMemo(() => {
    if (typeof window === 'undefined') return undefined;
    return L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
    });
  }, []);

  const crashIcon = useMemo(() => {
    if (typeof window === 'undefined') return undefined;
    return L.divIcon({
      html: `<div class="relative flex items-center justify-center">
        <div class="absolute w-12 h-12 bg-red-500 rounded-full opacity-60 animate-ping"></div>
        <div class="relative bg-red-600 text-white p-2.5 rounded-full border-2 border-white shadow-lg text-lg flex items-center justify-center">
          🚗
        </div>
      </div>`,
      className: 'custom-crash-icon',
      iconSize: [48, 48],
      iconAnchor: [24, 24],
    });
  }, []);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  if (!isMounted || safeCenter[0] === 0 && safeCenter[1] === 0) {
    return (
      <div className="h-full w-full bg-gray-100 flex items-center justify-center text-gray-500 font-semibold text-sm">
        Cargando ubicación en el mapa...
      </div>
    );
  }

  return (
    <div className="h-full w-full rounded-xl overflow-hidden shadow-inner border border-red-100 relative">
      <link
        rel="stylesheet"
        href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
        integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
        crossOrigin=""
      />
      <MapContainer 
        center={safeCenter} 
        zoom={zoom} 
        scrollWheelZoom={true}
        style={{ height: '100%', width: '100%' }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <AnimatedMarker 
          position={safeCenter} 
          icon={isCrash && crashIcon ? crashIcon : defaultIcon}
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
        <RecenterMap center={safeCenter} />
      </MapContainer>
    </div>
  );
}
