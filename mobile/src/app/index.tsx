import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert, ActivityIndicator, ScrollView } from 'react-native';
import { useAuth } from '@/context/AuthContext';
import api from '@/services/api';
import * as Location from 'expo-location';
import { LOCATION_TASK_NAME } from '@/services/locationTask';
import { MapPin, CheckCircle, Power, User as UserIcon, Shield, Settings, Users } from 'lucide-react-native';
import { useRouter } from 'expo-router';

export default function HomeScreen() {
  const { user, logout } = useAuth();
  const router = useRouter();
  const [checkingIn, setCheckingIn] = useState(false);
  const [isTracking, setIsTracking] = useState(false);
  const [lastCheckIn, setLastCheckIn] = useState<string | null>(null);

  useEffect(() => {
    checkTrackingStatus();
  }, []);

  const checkTrackingStatus = async () => {
    try {
      const hasStarted = await Location.hasStartedLocationUpdatesAsync(LOCATION_TASK_NAME);
      setIsTracking(hasStarted);
    } catch (e) {
      console.error('Error checking tracking status', e);
    }
  };

  const handleCheckIn = async () => {
    setCheckingIn(true);
    try {
      await api.post('/check-in');
      setLastCheckIn(new Date().toLocaleTimeString());
      Alert.alert('¡Excelente!', 'Tu estado ha sido actualizado.');
    } catch (error) {
      Alert.alert('Error', 'No pudimos registrar tu check-in.');
    } finally {
      setCheckingIn(false);
    }
  };

  const toggleTracking = async () => {
    if (isTracking) {
      try {
        await Location.stopLocationUpdatesAsync(LOCATION_TASK_NAME);
        setIsTracking(false);
        Alert.alert('Rastreo Desactivado', 'Ya no compartes tu ubicación.');
      } catch (e) {
        console.error('Error stopping tracking', e);
      }
    } else {
      const { status: foregroundStatus } = await Location.requestForegroundPermissionsAsync();
      if (foregroundStatus !== 'granted') {
        Alert.alert('Permiso Denegado', 'Necesitamos acceso a la ubicación.');
        return;
      }
      const { status: backgroundStatus } = await Location.requestBackgroundPermissionsAsync();
      if (backgroundStatus !== 'granted') {
        Alert.alert('Permiso de Fondo Denegado', 'Activa "Permitir siempre" en ajustes.');
        return;
      }

      try {
        await Location.startLocationUpdatesAsync(LOCATION_TASK_NAME, {
          accuracy: Location.Accuracy.Balanced,
          timeInterval: 60000,
          distanceInterval: 100,
          foregroundService: {
            notificationTitle: 'Estoy Ok está activo',
            notificationBody: 'Protegiendo tu ubicación en segundo plano',
            notificationColor: '#dc2626',
          },
        });
        setIsTracking(true);
        Alert.alert('Rastreo Activado', 'Tu ubicación se actualiza para tu círculo.');
      } catch (e) {
        Alert.alert('Error', 'No se pudo iniciar el rastreo.');
      }
    }
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.contentContainer}>
      {/* Header Compacto */}
      <View style={styles.header}>
        <View style={styles.userInfo}>
          <Text style={styles.welcomeText}>Hola, {user?.name}</Text>
          <Text style={styles.planBadge}>{user?.is_premium ? 'Socio Premium ⭐' : 'Plan Básico'}</Text>
        </View>
        <TouchableOpacity onPress={logout} style={styles.logoutButton}>
          <Power size={20} color="#9ca3af" />
        </TouchableOpacity>
      </View>

      {/* MODULO 1: BIENESTAR */}
      <View style={styles.moduleCard}>
        <View style={styles.moduleHeader}>
          <Shield size={20} color="#2563eb" />
          <Text style={styles.moduleTitle}>Mi Bienestar Diario</Text>
        </View>
        
        <View style={styles.wellbeingContent}>
          <TouchableOpacity 
            style={[styles.checkInButton, checkingIn && styles.buttonDisabled]} 
            onPress={handleCheckIn}
            disabled={checkingIn}
          >
            {checkingIn ? (
              <ActivityIndicator size="large" color="#fff" />
            ) : (
              <>
                <CheckCircle size={48} color="#fff" strokeWidth={2} />
                <Text style={styles.checkInText}>ESTOY OK</Text>
              </>
            )}
          </TouchableOpacity>
          
          <View style={styles.wellbeingInfo}>
            <Text style={styles.wellbeingStatus}>
              {lastCheckIn ? `Último reporte: ${lastCheckIn}` : 'No te has reportado hoy'}
            </Text>
            <Text style={styles.wellbeingDesc}>
              Avisaremos a tus contactos si pasan {user?.checkin_interval_hours} {__DEV__ ? 'minutos' : 'horas'} sin noticias.
            </Text>
          </View>
        </View>

        <View style={styles.moduleActions}>
          <TouchableOpacity style={styles.subActionButton} onPress={() => router.push('/contacts')}>
            <Users size={18} color="#4b5563" />
            <Text style={styles.subActionText}>Contactos</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.subActionButton} onPress={() => router.push('/settings')}>
            <Settings size={18} color="#4b5563" />
            <Text style={styles.subActionText}>Configurar</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* MODULO 2: RASTREO */}
      <View style={[styles.moduleCard, { borderLeftColor: '#dc2626', borderLeftWidth: 4 }]}>
        <View style={styles.moduleHeader}>
          <MapPin size={20} color="#dc2626" />
          <Text style={styles.moduleTitle}>Rastreo en Tiempo Real</Text>
        </View>

        <View style={styles.trackingContent}>
          <View style={styles.trackingInfo}>
            <Text style={styles.trackingStatus}>
              {isTracking ? '✓ Compartiendo ubicación' : '✕ Rastreo desactivado'}
            </Text>
            <Text style={styles.trackingDesc}>
              Tus círculos de confianza podrán ver tu posición en el mapa.
            </Text>
          </View>
          
          <TouchableOpacity 
            style={[styles.toggleButton, { backgroundColor: isTracking ? '#fee2e2' : '#f3f4f6' }]} 
            onPress={toggleTracking}
          >
            <Text style={[styles.toggleText, { color: isTracking ? '#dc2626' : '#4b5563' }]}>
              {isTracking ? 'Detener' : 'Activar'}
            </Text>
          </TouchableOpacity>
        </View>
      </View>

      <View style={styles.footer}>
        <Text style={styles.footerText}>Estoy Ok v1.0 • Sistema de Protección Familiar</Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f3f4f6' },
  contentContainer: { padding: 16, paddingBottom: 40 },
  header: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24, marginTop: 10 },
  userInfo: { flex: 1 },
  welcomeText: { fontSize: 22, fontWeight: '900', color: '#111827' },
  planBadge: { fontSize: 12, fontWeight: '700', color: '#6b7280', marginTop: 2 },
  logoutButton: { padding: 8, backgroundColor: '#fff', borderRadius: 12, shadowColor: '#000', shadowOpacity: 0.05, elevation: 1 },
  
  moduleCard: { backgroundColor: '#fff', borderRadius: 24, padding: 20, marginBottom: 16, shadowColor: '#000', shadowOpacity: 0.05, shadowRadius: 10, elevation: 2 },
  moduleHeader: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 20 },
  moduleTitle: { fontSize: 15, fontWeight: '800', color: '#374151', textTransform: 'uppercase', letterSpacing: 0.5 },
  
  wellbeingContent: { alignItems: 'center', gap: 20, marginBottom: 20 },
  checkInButton: { width: 160, height: 160, borderRadius: 80, backgroundColor: '#2563eb', justifyContent: 'center', alignItems: 'center', elevation: 8, shadowColor: '#2563eb', shadowOffset: { width: 0, height: 8 }, shadowOpacity: 0.3, shadowRadius: 15 },
  checkInText: { color: '#fff', fontSize: 20, fontWeight: '900', marginTop: 8 },
  wellbeingInfo: { alignItems: 'center' },
  wellbeingStatus: { fontSize: 16, fontWeight: '700', color: '#111827' },
  wellbeingDesc: { fontSize: 12, color: '#6b7280', textAlign: 'center', marginTop: 4, paddingHorizontal: 20 },
  
  moduleActions: { flexDirection: 'row', gap: 12, borderTopWidth: 1, borderTopColor: '#f3f4f6', paddingTop: 16 },
  subActionButton: { flex: 1, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 8, backgroundColor: '#f9fafb', paddingVertical: 12, borderRadius: 15, borderWidth: 1, borderColor: '#f3f4f6' },
  subActionText: { fontSize: 13, fontWeight: '700', color: '#4b5563' },
  
  trackingContent: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  trackingInfo: { flex: 1, marginRight: 16 },
  trackingStatus: { fontSize: 15, fontWeight: '700', color: '#111827' },
  trackingDesc: { fontSize: 12, color: '#6b7280', marginTop: 4 },
  toggleButton: { paddingVertical: 10, paddingHorizontal: 20, borderRadius: 12 },
  toggleText: { fontSize: 14, fontWeight: '800' },
  
  footer: { marginTop: 20, alignItems: 'center' },
  footerText: { fontSize: 11, color: '#9ca3af', fontWeight: '600' },
  buttonDisabled: { opacity: 0.7, backgroundColor: '#9ca3af' }
});
