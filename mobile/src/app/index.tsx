import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert, ActivityIndicator } from 'react-native';
import { useAuth } from '@/context/AuthContext';
import api from '@/services/api';
import * as Location from 'expo-location';
import { LOCATION_TASK_NAME } from '@/services/locationTask';
import { MapPin, CheckCircle, Power, User as UserIcon } from 'lucide-react-native';

export default function HomeScreen() {
  const { user, logout } = useAuth();
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
      Alert.alert('¡Excelente!', 'Tu estado ha sido actualizado. Tu familia sabe que estás bien.');
    } catch (error) {
      Alert.alert('Error', 'No pudimos registrar tu check-in. Reintenta en unos momentos.');
    } finally {
      setCheckingIn(false);
    }
  };

  const toggleTracking = async () => {
    if (isTracking) {
      try {
        await Location.stopLocationUpdatesAsync(LOCATION_TASK_NAME);
        setIsTracking(false);
        Alert.alert('Seguimiento Desactivado', 'Ya no estás compartiendo tu ubicación en tiempo real.');
      } catch (e) {
        console.error('Error stopping tracking', e);
      }
    } else {
      const { status: foregroundStatus } = await Location.requestForegroundPermissionsAsync();
      if (foregroundStatus !== 'granted') {
        Alert.alert('Permiso Denegado', 'Necesitamos acceso a tu ubicación para el rastreo activo.');
        return;
      }

      const { status: backgroundStatus } = await Location.requestBackgroundPermissionsAsync();
      if (backgroundStatus !== 'granted') {
        Alert.alert('Permiso de Fondo Denegado', 'Para que el rastreo funcione con la app cerrada, activa "Permitir siempre" en los ajustes de ubicación.');
        return;
      }

      try {
        await Location.startLocationUpdatesAsync(LOCATION_TASK_NAME, {
          accuracy: Location.Accuracy.Balanced,
          timeInterval: 60000, // 1 minute
          distanceInterval: 100, // 100 meters
          foregroundService: {
            notificationTitle: 'Estoy Ok está activo',
            notificationBody: 'Protegiendo tu ubicación en segundo plano',
            notificationColor: '#dc2626',
          },
        });
        setIsTracking(true);
        Alert.alert('Seguimiento Activado', 'Tu ubicación se actualizará automáticamente para tu círculo de seguridad.');
      } catch (e) {
        console.error('Error starting tracking', e);
        Alert.alert('Error', 'No pudimos iniciar el seguimiento. Verifica los permisos.');
      }
    }
  };

  return (
    <View style={styles.container}>
      {/* Profile Header */}
      <View style={styles.profileCard}>
        <View style={styles.avatar}>
          <UserIcon size={24} color="#fff" />
        </View>
        <View style={styles.profileInfo}>
          <Text style={styles.userName}>{user?.name}</Text>
          <Text style={styles.userPlan}>{user?.is_premium ? 'Plan Premium' : 'Plan Básico'}</Text>
        </View>
        <TouchableOpacity onPress={logout} style={styles.logoutButton}>
          <Power size={20} color="#6b7280" />
        </TouchableOpacity>
      </View>

      {/* Main Action Area */}
      <View style={styles.mainAction}>
        <Text style={styles.statusTitle}>¿Cómo estás hoy?</Text>
        <TouchableOpacity 
          style={[styles.checkInButton, checkingIn && styles.buttonDisabled]} 
          onPress={handleCheckIn}
          disabled={checkingIn}
        >
          {checkingIn ? (
            <ActivityIndicator size="large" color="#fff" />
          ) : (
            <>
              <CheckCircle size={64} color="#fff" strokeWidth={1.5} />
              <Text style={styles.checkInText}>ESTOY OK</Text>
            </>
          )}
        </TouchableOpacity>
        {lastCheckIn && (
          <Text style={styles.lastCheckIn}>Último check-in: {lastCheckIn}</Text>
        )}
      </View>

      {/* Tracking Toggle */}
      <View style={styles.trackingCard}>
        <View style={styles.trackingInfo}>
          <View style={[styles.statusDot, { backgroundColor: isTracking ? '#22c55e' : '#9ca3af' }]} />
          <View>
            <Text style={styles.trackingTitle}>Rastreo en Tiempo Real</Text>
            <Text style={styles.trackingSubtitle}>
              {isTracking ? 'Activo y compartiendo' : 'Desactivado'}
            </Text>
          </View>
        </View>
        <TouchableOpacity 
          style={[styles.toggleButton, { backgroundColor: isTracking ? '#fee2e2' : '#f3f4f6' }]} 
          onPress={toggleTracking}
        >
          <MapPin size={20} color={isTracking ? '#dc2626' : '#6b7280'} />
          <Text style={[styles.toggleText, { color: isTracking ? '#dc2626' : '#6b7280' }]}>
            {isTracking ? 'Detener' : 'Activar'}
          </Text>
        </TouchableOpacity>
      </View>

      <View style={styles.infoCard}>
        <Text style={styles.infoText}>
          Si no realizas tu check-in diario, notificaremos automáticamente a tus contactos de emergencia.
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f9fafb',
    padding: 20,
  },
  profileCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    padding: 15,
    borderRadius: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 10,
    elevation: 2,
    marginBottom: 30,
  },
  avatar: {
    width: 45,
    height: 45,
    borderRadius: 22.5,
    backgroundColor: '#dc2626',
    justifyContent: 'center',
    alignItems: 'center',
  },
  profileInfo: {
    flex: 1,
    marginLeft: 12,
  },
  userName: {
    fontSize: 16,
    fontWeight: '700',
    color: '#111827',
  },
  userPlan: {
    fontSize: 12,
    color: '#6b7280',
  },
  logoutButton: {
    padding: 8,
  },
  mainAction: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  statusTitle: {
    fontSize: 24,
    fontWeight: '800',
    color: '#111827',
    marginBottom: 30,
  },
  checkInButton: {
    width: 220,
    height: 220,
    borderRadius: 110,
    backgroundColor: '#dc2626',
    justifyContent: 'center',
    alignItems: 'center',
    elevation: 8,
    shadowColor: '#dc2626',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.3,
    shadowRadius: 20,
  },
  checkInText: {
    color: '#fff',
    fontSize: 28,
    fontWeight: '900',
    marginTop: 10,
    letterSpacing: -1,
  },
  buttonDisabled: {
    opacity: 0.8,
  },
  lastCheckIn: {
    marginTop: 20,
    fontSize: 14,
    color: '#6b7280',
    fontStyle: 'italic',
  },
  trackingCard: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#fff',
    padding: 20,
    borderRadius: 20,
    marginBottom: 15,
  },
  trackingInfo: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  statusDot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    marginRight: 12,
  },
  trackingTitle: {
    fontSize: 15,
    fontWeight: '700',
    color: '#111827',
  },
  trackingSubtitle: {
    fontSize: 12,
    color: '#6b7280',
  },
  toggleButton: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 8,
    paddingHorizontal: 15,
    borderRadius: 12,
    gap: 6,
  },
  toggleText: {
    fontSize: 13,
    fontWeight: '700',
  },
  infoCard: {
    backgroundColor: '#eff6ff',
    padding: 15,
    borderRadius: 15,
    borderWidth: 1,
    borderColor: '#dbeafe',
  },
  infoText: {
    fontSize: 12,
    color: '#1e40af',
    textAlign: 'center',
    lineHeight: 18,
  },
});
