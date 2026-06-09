'use client';

import dynamic from 'next/dynamic';

const DynamicEmergencyMap = dynamic(() => import('./EmergencyMap'), {
  ssr: false,
  loading: () => (
    <div className="h-full w-full bg-gray-100 flex items-center justify-center">
      Cargando mapa...
    </div>
  ),
});

export default DynamicEmergencyMap;
