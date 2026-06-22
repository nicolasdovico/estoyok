import React from 'react';
import MapView, { PROVIDER_GOOGLE, Marker, Polyline } from 'react-native-maps';
import { HistoryMapProps } from './HistoryMapProps';

export default function HistoryMap({
  mapRef,
  points,
  onMapReady,
  activePoint,
  playbackIndex,
  getSpeedText,
  memberInfo,
  style
}: HistoryMapProps) {
  return (
    <MapView
      ref={mapRef}
      provider={PROVIDER_GOOGLE}
      style={style}
      onMapReady={onMapReady}
      initialRegion={{
        latitude: -34.6037,
        longitude: -58.3816,
        latitudeDelta: 0.05,
        longitudeDelta: 0.05,
      }}
    >
      {points.length > 0 && (
        <Polyline
          coordinates={points.map((p) => ({ latitude: p.latitude, longitude: p.longitude }))}
          strokeColor="#4f46e5"
          strokeWidth={4}
          lineDashPattern={[5, 5]}
        />
      )}

      {/* Start Point Marker */}
      {points.length > 0 && (
        <Marker
          key={`start-${points[0].id || 0}`}
          coordinate={{ latitude: points[0].latitude, longitude: points[0].longitude }}
          title="Inicio del recorrido"
          pinColor="green"
        />
      )}

      {/* End Point Marker */}
      {points.length > 1 && (
        <Marker
          key={`end-${points[points.length - 1].id || 'end'}`}
          coordinate={{ latitude: points[points.length - 1].latitude, longitude: points[points.length - 1].longitude }}
          title="Fin del recorrido"
          pinColor="red"
        />
      )}

      {/* Playback Marker */}
      {activePoint && (
        <Marker
          key={`playback-${activePoint.id || 'playback'}-${playbackIndex}`}
          coordinate={{ latitude: activePoint.latitude, longitude: activePoint.longitude }}
          title="Posición en reproducción"
          description={`Velocidad: ${getSpeedText()}`}
          pinColor="gold"
        />
      )}

      {/* Fallback Current Location Marker when no history is loaded */}
      {points.length === 0 && memberInfo?.current_location && (
        <Marker
          key={`fallback-${memberInfo.id}`}
          coordinate={{
            latitude: Number(memberInfo.current_location.latitude),
            longitude: Number(memberInfo.current_location.longitude)
          }}
          title={`Última ubicación de ${memberInfo.name}`}
          description={memberInfo.current_location.last_seen_at ? `Visto por última vez: ${new Date(memberInfo.current_location.last_seen_at).toLocaleTimeString()}` : 'Última ubicación registrada'}
          pinColor="red"
        />
      )}
    </MapView>
  );
}
