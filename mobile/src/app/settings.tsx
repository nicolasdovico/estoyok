import React, { useState } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Alert, ActivityIndicator } from 'react-native';
import { useAuth } from '@/context/AuthContext';
import { settingsService } from '@/services/settings';
import { Clock, Check } from 'lucide-react-native';

const INTERVAL_OPTIONS = [
  { label: '6 Horas', value: 6 },
  { label: '12 Horas', value: 12 },
  { label: '24 Horas', value: 24 },
  { label: '48 Horas', value: 48 },
  { label: '1 Semana', value: 168 },
];

const DEV_OPTIONS = [
  { label: '2 Minutos (Test)', value: 2 },
  { label: '3 Minutos (Test)', value: 3 },
  { label: '5 Minutos (Test)', value: 5 },
];

export default function SettingsScreen() {
  const { user } = useAuth();
  const [currentInterval, setCurrentInterval] = useState(user?.checkin_interval_hours || 24);
  const [isUpdating, setIsUpdating] = useState(false);

  const handleUpdateInterval = async (hours: number) => {
    setIsUpdating(true);
    try {
      await settingsService.updateCheckinInterval(hours);
      setCurrentInterval(hours);
      Alert.alert('Éxito', 'Intervalo de check-in actualizado.');
    } catch (error) {
      Alert.alert('Error', 'No se pudo actualizar el intervalo.');
    } finally {
      setIsUpdating(false);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Clock size={32} color="#dc2626" />
        <Text style={styles.title}>Configuración de Seguridad</Text>
        <Text style={styles.subtitle}>Define cada cuánto tiempo debes reportarte.</Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Intervalo de "Estoy Ok"</Text>
        <Text style={styles.description}>
          Si no presionas el botón "Estoy Ok" en este tiempo, enviaremos alertas a tus contactos de emergencia.
        </Text>

        <View style={styles.optionsContainer}>
          {INTERVAL_OPTIONS.map((option) => (
            <TouchableOpacity
              key={option.value}
              style={[
                styles.option,
                currentInterval === option.value && styles.optionSelected
              ]}
              onPress={() => handleUpdateInterval(option.value)}
              disabled={isUpdating}
            >
              <Text style={[
                styles.optionLabel,
                currentInterval === option.value && styles.optionLabelSelected
              ]}>
                {option.label}
              </Text>
              {currentInterval === option.value && (
                <Check size={18} color="#fff" />
              )}
            </TouchableOpacity>
          ))}
        </View>

        {__DEV__ && (
          <>
            <Text style={[styles.sectionTitle, { marginTop: 30, color: '#dc2626' }]}>
              Modo de Prueba (Minutos)
            </Text>
            <Text style={styles.description}>
              Solo visible en desarrollo. Los valores se tratarán como minutos.
            </Text>
            <View style={styles.optionsContainer}>
              {DEV_OPTIONS.map((option) => (
                <TouchableOpacity
                  key={option.value}
                  style={[
                    styles.option,
                    currentInterval === option.value && styles.optionSelected
                  ]}
                  onPress={() => handleUpdateInterval(option.value)}
                  disabled={isUpdating}
                >
                  <Text style={[
                    styles.optionLabel,
                    currentInterval === option.value && styles.optionLabelSelected
                  ]}>
                    {option.label}
                  </Text>
                  {currentInterval === option.value && (
                    <Check size={18} color="#fff" />
                  )}
                </TouchableOpacity>
              ))}
            </View>
          </>
        )}

        {isUpdating && <ActivityIndicator size="small" color="#dc2626" style={{ marginTop: 20 }} />}
      </View>

      <View style={styles.infoCard}>
        <Text style={styles.infoText}>
          Nota: Un intervalo más corto ofrece mayor seguridad pero requiere reportes más frecuentes.
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f9fafb' },
  header: { padding: 30, alignItems: 'center', backgroundColor: '#fff', borderBottomWidth: 1, borderBottomColor: '#f3f4f6' },
  title: { fontSize: 20, fontWeight: '800', color: '#111827', marginTop: 10 },
  subtitle: { fontSize: 14, color: '#6b7280', marginTop: 4, textAlign: 'center' },
  section: { padding: 20 },
  sectionTitle: { fontSize: 16, fontWeight: '700', color: '#374151', marginBottom: 10 },
  description: { fontSize: 14, color: '#6b7280', marginBottom: 20, lineHeight: 20 },
  optionsContainer: { gap: 10 },
  option: { 
    flexDirection: 'row', 
    justifyContent: 'space-between', 
    alignItems: 'center', 
    backgroundColor: '#fff', 
    padding: 18, 
    borderRadius: 15,
    borderWidth: 1,
    borderColor: '#e5e7eb'
  },
  optionSelected: { backgroundColor: '#dc2626', borderColor: '#dc2626' },
  optionLabel: { fontSize: 16, color: '#4b5563', fontWeight: '600' },
  optionLabelSelected: { color: '#fff' },
  infoCard: { margin: 20, padding: 15, backgroundColor: '#eff6ff', borderRadius: 12, borderWidth: 1, borderColor: '#dbeafe' },
  infoText: { fontSize: 12, color: '#1e40af', textAlign: 'center' },
});
