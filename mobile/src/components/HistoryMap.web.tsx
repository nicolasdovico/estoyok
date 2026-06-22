import React from 'react';
import { View, Text } from 'react-native';
import { HistoryMapProps } from './HistoryMapProps';

export default function HistoryMap({
  style
}: HistoryMapProps) {
  return (
    <View style={[style, { justifyContent: 'center', alignItems: 'center', backgroundColor: '#e5e7eb', padding: 20 }]}>
      <Text style={{ fontWeight: 'bold', color: '#4b5563', textAlign: 'center', fontSize: 16 }}>
        Mapa no soportado en desarrollo Web.
      </Text>
      <Text style={{ fontSize: 13, color: '#6b7280', marginTop: 8, textAlign: 'center' }}>
        Por favor abre Expo Go en tu teléfono iOS o Android real para ver el mapa de rutas e historial interactivo.
      </Text>
    </View>
  );
}
