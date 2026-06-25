import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert, ActivityIndicator, ScrollView, RefreshControl, TextInput, Clipboard, Linking, Platform, DeviceEventEmitter, Vibration, Modal, Switch } from 'react-native';
import { useAuth } from '@/context/AuthContext';
import api from '@/services/api';
import * as Location from 'expo-location';
import { LOCATION_TASK_NAME, flushOfflineLocations } from '@/services/locationTask';
import { MapPin, CheckCircle, Power, User as UserIcon, Shield, Settings, Users, Copy, Plus, Trash2, Compass, Map, Battery, BatteryMedium, BatteryLow, EyeOff, MapPinOff, WifiOff } from 'lucide-react-native';
import { useRouter, useFocusEffect } from 'expo-router';
import { Audio } from 'expo-av';
import { startSos, uploadSosAudio } from '@/services/sosService';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as Notifications from 'expo-notifications';

export default function HomeScreen() {
  const { user, logout } = useAuth();
  const router = useRouter();

  useFocusEffect(
    useCallback(() => {
      if (user) {
        checkTrackingStatus();
        fetchUserData();
        fetchCheckIns();
        fetchCircles();
        fetchActiveGeofences();
      }
    }, [user])
  );

  const [checkingIn, setCheckingIn] = useState(false);
  const [isTracking, setIsTracking] = useState(false);
  const [freshUser, setFreshUser] = useState<any>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [checkIns, setCheckIns] = useState<any[]>([]);
  const [loadingCheckIns, setLoadingCheckIns] = useState(false);

  // Estados para Núcleos
  const [circles, setCircles] = useState<any[]>([]);
  const [loadingCircles, setLoadingCircles] = useState(false);
  const [selectedCircleId, setSelectedCircleId] = useState<number | null>(null);
  const [newCircleName, setNewCircleName] = useState('');
  const [inviteCodeInput, setInviteCodeInput] = useState('');
  const [submittingCircle, setSubmittingCircle] = useState(false);

  // Estados para Radar de Proximidad Dinámico
  const [activeGeofences, setActiveGeofences] = useState<any[]>([]);
  const [radarModalVisible, setRadarModalVisible] = useState(false);
  const [radarTargetMember, setRadarTargetMember] = useState<any>(null);

  const fetchActiveGeofences = async () => {
    try {
      const response = await api.get('/dynamic-geofences/active');
      setActiveGeofences(response.data);
    } catch (e) {
      console.error('Error fetching active dynamic geofences:', e);
    }
  };

  const handleOpenRadarModal = (member: any) => {
    setRadarTargetMember(member);
    setRadarModalVisible(true);
  };

  const handleStartRadar = async (radius: number) => {
    if (!radarTargetMember) return;
    try {
      await api.post('/dynamic-geofences', {
        target_id: radarTargetMember.id,
        safe_radius_meters: radius,
      });
      Alert.alert('Éxito', `Radar de proximidad iniciado (${radius}m).`);
      setRadarModalVisible(false);
      setRadarTargetMember(null);
      fetchActiveGeofences();
    } catch (err: any) {
      const message = err.response?.data?.message || 'No se pudo iniciar el radar.';
      Alert.alert('Error', message);
    }
  };

  const handleDeactivateRadar = async (id: number) => {
    try {
      await api.post(`/dynamic-geofences/${id}/deactivate`);
      Alert.alert('Éxito', 'Radar de proximidad desactivado.');
      fetchActiveGeofences();
    } catch (err) {
      Alert.alert('Error', 'No se pudo desactivar el radar.');
    }
  };

  const toggleProximityAlerts = async () => {
    const newValue = !(freshUser?.proximity_alerts_enabled !== false);
    try {
      await api.put('/settings/proximity-alerts', {
        proximity_alerts_enabled: newValue
      });
      setFreshUser((prev: any) => prev ? { ...prev, proximity_alerts_enabled: newValue } : null);
      Alert.alert('Éxito', newValue ? 'Radar de proximidad habilitado.' : 'Radar de proximidad deshabilitado.');
    } catch (e) {
      Alert.alert('Error', 'No se pudo actualizar la configuración de privacidad del radar.');
    }
  };

  // Estados para SOS
  const [recording, setRecording] = useState<any>(null);
  const [isSosActive, setIsSosActive] = useState(false);
  const [isRecordingAudio, setIsRecordingAudio] = useState(false);

  const onRefresh = async () => {
    setRefreshing(true);
    await Promise.all([
      checkTrackingStatus(),
      fetchUserData(),
      fetchCheckIns(),
      fetchCircles(),
      fetchActiveGeofences(),
      flushOfflineLocations().catch(e => console.error(e))
    ]);
    setRefreshing(false);
  };

  const startHighFrequencyTracking = async () => {
    try {
      await Location.startLocationUpdatesAsync(LOCATION_TASK_NAME, {
        accuracy: Location.Accuracy.BestForNavigation,
        timeInterval: 5000,
        distanceInterval: 1,
        foregroundService: {
          notificationTitle: '🚨 S.O.S. SILENCIOSO ACTIVO 🚨',
          notificationBody: 'Transmitiendo ubicación en tiempo real crítico',
          notificationColor: '#dc2626',
        },
      });
      setIsTracking(true);
    } catch (e) {
      console.error('Failed to start high frequency tracking', e);
    }
  };

  const restoreNormalTracking = async () => {
    try {
      const hasStarted = await Location.hasStartedLocationUpdatesAsync(LOCATION_TASK_NAME);
      if (hasStarted) {
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
      }
    } catch (e) {
      console.error('Failed to restore normal tracking', e);
    }
  };

  const sendFallbackSms = async () => {
    try {
      const activeContacts = freshUser?.emergency_contacts?.filter((c: any) => c.is_active) || [];
      const phones = activeContacts.map((c: any) => c.phone).filter(Boolean);
      
      if (phones.length > 0) {
        const message = `🚨 ¡SOS CRÍTICO! Estoy en peligro. Por favor, ayuda.`;
        const separator = Platform.OS === 'ios' ? '&' : '?';
        const url = `sms:${phones.join(',')}${separator}body=${encodeURIComponent(message)}`;
        await Linking.openURL(url);
      } else {
        Alert.alert('Sin Contactos', 'No tienes contactos de emergencia activos para enviar el SMS.');
      }
    } catch (err) {
      console.error('Failed to send fallback SMS', err);
    }
  };

  const handleSilentSos = async () => {
    if (isSosActive) {
      setCheckingIn(true);
      try {
        const crashEventId = await AsyncStorage.getItem('active_crash_event_id');
        if (crashEventId) {
          await api.post(`/alerts/crash/${crashEventId}/false-alarm`);
          await AsyncStorage.removeItem('active_crash_event_id');
          Alert.alert('Alerta Cancelada', 'La alerta de accidente ha sido cancelada como falsa alarma y el rastreo volvió a la normalidad.');
        } else {
          await api.post('/check-in'); // Checkin resuelve alertas activas en backend
          Alert.alert('SOS Desactivado', 'El SOS ha sido cancelado y el rastreo volvió a la normalidad.');
        }
        await restoreNormalTracking();
        setIsSosActive(false);
        await onRefresh();
      } catch (err) {
        console.error('Failed to resolve SOS/Crash Alert', err);
        Alert.alert('Error', 'No se pudo desactivar el estado de emergencia. Intenta de nuevo.');
      } finally {
        setCheckingIn(false);
      }
      return;
    }

    setIsSosActive(true);
    let alertId: string | null = null;

    try {
      await startHighFrequencyTracking();
      const alertData = await startSos();
      alertId = alertData.id;
      // Actualizar núcleos inmediatamente para que se refleje el S.O.S. en la lista
      await fetchCircles(true);
    } catch (err: any) {
      console.error('SOS Activation error, attempting fallback SMS', err);
      Alert.alert(
        'Error de Conectividad',
        'No se pudo conectar con el servidor. ¿Deseas enviar un SMS de emergencia a tus contactos?',
        [
          { text: 'Cancelar', style: 'cancel', onPress: () => setIsSosActive(false) },
          {
            text: 'Enviar SMS',
            onPress: async () => {
              await sendFallbackSms();
              setIsSosActive(false);
            },
          },
        ]
      );
      return;
    }

    try {
      setIsRecordingAudio(true);
      const { status } = await Audio.requestPermissionsAsync();
      if (status !== 'granted') {
        console.warn('Microphone permission not granted for SOS recording');
        setIsRecordingAudio(false);
        return;
      }

      await Audio.setAudioModeAsync({
        allowsRecordingIOS: true,
        playsInSilentModeIOS: true,
      });

      const { recording: newRecording } = await Audio.Recording.createAsync(
        Audio.RecordingOptionsPresets.HIGH_QUALITY
      );
      
      setRecording(newRecording);
      console.log('SOS silent recording started');

      setTimeout(async () => {
        try {
          console.log('Stopping SOS recording...');
          await newRecording.stopAndUnloadAsync();
          const uri = newRecording.getURI();
          setIsRecordingAudio(false);
          setRecording(null);

          if (uri && alertId) {
            console.log('Uploading SOS audio:', uri);
            await uploadSosAudio(alertId, uri);
            console.log('SOS audio uploaded successfully');
          }
        } catch (recErr) {
          console.error('Error stopping or uploading SOS recording', recErr);
          setIsRecordingAudio(false);
          setRecording(null);
        }
      }, 15000);

    } catch (err) {
      console.error('Failed to record SOS audio', err);
      setIsRecordingAudio(false);
    }
  };

  useEffect(() => {
    if (user) {
      checkTrackingStatus();
      fetchUserData();
      fetchCheckIns();
      fetchCircles();
      fetchActiveGeofences();
      flushOfflineLocations().catch(e => console.error(e));

      // Polling interval to refresh tracking status, circle members locations and sensor states
      const interval = setInterval(() => {
        fetchCircles(true);
        fetchUserData();
        fetchActiveGeofences();
      }, 10000); // 10 seconds

      return () => clearInterval(interval);
    }
  }, [user]);

  // Listener para notificaciones push de geocercas dinámicas (breach y restore)
  useEffect(() => {
    if (Platform.OS === 'web') return;

    const subscription = Notifications.addNotificationReceivedListener(notification => {
      const data = notification.request.content.data;
      if (data && data.type === 'dynamic_geofence_breached') {
        // Vibrate persistently
        Vibration.vibrate([500, 500, 500], true);
        
        Alert.alert(
          '🚨 ALERTA DE DISTANCIA',
          notification.request.content.body || '¡Atención! Un miembro se ha alejado demasiado de tu ubicación.',
          [
            {
              text: 'Detener Alerta',
              onPress: () => {
                Vibration.cancel();
              },
              style: 'destructive',
            }
          ],
          { cancelable: false }
        );
      } else if (data && data.type === 'dynamic_geofence_restored') {
        Vibration.cancel();
      }
    });

    return () => {
      subscription.remove();
      Vibration.cancel();
    };
  }, []);

  // Sincronizar isSosActive con el estado del servidor (a través de la lista de círculos)
  useEffect(() => {
    if (user && circles.length > 0) {
      const selfMember = circles
        .flatMap(c => c.users || [])
        .find((u: any) => u.id === user.id);
      
      const hasActiveSos = selfMember?.active_emergency_alerts?.some(
        (alert: any) => (alert.type === 'silent_sos' || alert.type === 'crash') && alert.status === 'active'
      );
      
      if (hasActiveSos !== undefined) {
        setIsSosActive(!!hasActiveSos);
      }
    }
  }, [circles, user]);

  // Listener para eventos de activación de emergencia por choque (desde overlay)
  useEffect(() => {
    const sub = DeviceEventEmitter.addListener('crash_emergency_active', (data) => {
      if (data.active) {
        setIsSosActive(true);
        onRefresh();
      }
    });
    return () => {
      sub.remove();
    };
  }, []);

  const fetchCircles = async (silent = false) => {
    if (!user) return;
    if (!silent) setLoadingCircles(true);
    try {
      const response = await api.get('/circles');
      setCircles(response.data);
      if (response.data.length > 0 && !selectedCircleId) {
        setSelectedCircleId(response.data[0].id);
      }
    } catch (e) {
      console.error('Error fetching circles', e);
    } finally {
      if (!silent) setLoadingCircles(false);
    }
  };

  const handleCreateCircle = async () => {
    if (!newCircleName.trim()) {
      Alert.alert('Error', 'Ingresa un nombre para el núcleo.');
      return;
    }
    setSubmittingCircle(true);
    try {
      const response = await api.post('/circles', { name: newCircleName });
      Alert.alert('Éxito', 'Núcleo creado exitosamente.');
      setNewCircleName('');
      await fetchCircles();
      setSelectedCircleId(response.data.id);
    } catch (e: any) {
      const errMsg = e.response?.data?.message || 'No se pudo crear el núcleo.';
      Alert.alert('Error', errMsg);
    } finally {
      setSubmittingCircle(false);
    }
  };

  const handleJoinCircle = async () => {
    if (!inviteCodeInput.trim() || inviteCodeInput.length !== 10) {
      Alert.alert('Error', 'Ingresa un código válido de 10 caracteres.');
      return;
    }
    setSubmittingCircle(true);
    try {
      const response = await api.post('/circles/join', { invite_code: inviteCodeInput });
      Alert.alert('Éxito', 'Te has unido al núcleo.');
      setInviteCodeInput('');
      await fetchCircles();
      setSelectedCircleId(response.data.id);
    } catch (e: any) {
      const errMsg = e.response?.data?.message || 'Código inválido o ya perteneces al núcleo.';
      Alert.alert('Error', errMsg);
    } finally {
      setSubmittingCircle(false);
    }
  };

  const handleRemoveMember = async (circleId: number, memberId: number) => {
    const isSelf = memberId === user?.id;
    Alert.alert(
      isSelf ? 'Salir del Núcleo' : 'Expulsar Miembro',
      isSelf ? '¿Estás seguro de que deseas abandonar este núcleo?' : '¿Estás seguro de que deseas expulsar a este miembro?',
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: isSelf ? 'Salir' : 'Expulsar',
          style: 'destructive',
          onPress: async () => {
            try {
              const response = await api.delete(`/circles/${circleId}/members/${memberId}`);
              Alert.alert('Éxito', response.data.message || 'Acción completada.');
              if (isSelf || circles.find(c => c.id === circleId)?.owner_id === memberId) {
                setSelectedCircleId(null);
              }
              await fetchCircles();
            } catch (e: any) {
              Alert.alert('Error', e.response?.data?.message || 'No se pudo realizar la acción.');
            }
          }
        }
      ]
    );
  };

  const openInMaps = (latitude: number, longitude: number) => {
    const url = `https://www.google.com/maps/search/?api=1&query=${latitude},${longitude}`;
    Linking.openURL(url).catch(() => {
      Alert.alert('Error', 'No se pudo abrir el mapa.');
    });
  };

  const copyToClipboard = (code: string) => {
    Clipboard.setString(code);
    Alert.alert('Copiado', 'El código de invitación se ha copiado al portapapeles.');
  };

  const checkTrackingStatus = async () => {
    try {
      const hasStarted = await Location.hasStartedLocationUpdatesAsync(LOCATION_TASK_NAME);
      setIsTracking(hasStarted);
    } catch (e) {
      console.error('Error checking tracking status', e);
    }
  };

  const fetchUserData = async () => {
    if (!user) return;
    try {
      const response = await api.get('/user');
      const data = response.data;
      setFreshUser(data);

      // Sincronizar el estado del rastreo local con el backend
      const hasStarted = await Location.hasStartedLocationUpdatesAsync(LOCATION_TASK_NAME);
      const isTrackingActiveOnBackend = data.current_location ? data.current_location.is_tracking_active : true;

      if (isTrackingActiveOnBackend && !hasStarted) {
        const { status: foregroundStatus } = await Location.getForegroundPermissionsAsync();
        const { status: backgroundStatus } = await Location.getBackgroundPermissionsAsync();
        if (foregroundStatus === 'granted' && backgroundStatus === 'granted') {
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
          } catch (e) {
            console.error('Auto start tracking failed, syncing inactive status to backend', e);
            try {
              await api.put('/locations/sensor-status', {
                is_tracking_active: false,
              });
            } catch (err) {
              console.error('Failed to update sensor status to inactive after auto-start failure', err);
            }
          }
        } else {
          // Si no hay permisos pero el backend cree que está activo, informamos que no
          try {
            await api.put('/locations/sensor-status', {
              is_tracking_active: false,
            });
          } catch (err) {
            console.error('Failed to notify inactive tracking status', err);
          }
        }
      } else if (!isTrackingActiveOnBackend && hasStarted) {
        try {
          await Location.stopLocationUpdatesAsync(LOCATION_TASK_NAME);
          setIsTracking(false);
        } catch (e) {
          console.error('Auto stop tracking failed', e);
        }
      }
    } catch (e) {
      console.error('Error fetching user data', e);
    }
  };

  const fetchCheckIns = async () => {
    if (!user) return;
    setLoadingCheckIns(true);
    try {
      const response = await api.get('/check-ins');
      setCheckIns(response.data);
    } catch (e) {
      console.error('Error fetching check-ins', e);
    } finally {
      setLoadingCheckIns(false);
    }
  };

  const handleCheckIn = async () => {
    setCheckingIn(true);
    try {
      await api.post('/check-in');
      await Promise.all([
        fetchUserData(),
        fetchCheckIns()
      ]);
      Alert.alert('¡Excelente!', 'Tu estado ha sido actualizado.');
    } catch (error) {
      Alert.alert('Error', 'No pudimos registrar tu check-in.');
    } finally {
      setCheckingIn(false);
    }
  };

  const getStatus = () => {
    const targetUser = freshUser || user;
    if (!targetUser || !targetUser.last_check_in_at) {
      return { safe: false, lastCheckInTime: null, nextCheckInTime: null };
    }

    const lastCheckInTime = new Date(targetUser.last_check_in_at).getTime();
    const intervalMs = targetUser.checkin_interval_hours * (__DEV__ ? 60 * 1000 : 60 * 60 * 1000);
    const nextCheckInTime = lastCheckInTime + intervalMs;
    const now = new Date().getTime();

    return {
      safe: nextCheckInTime > now,
      lastCheckInTime,
      nextCheckInTime
    };
  };

  const renderStatusBanner = () => {
    const status = getStatus();
    
    if (!status.lastCheckInTime) {
      return (
        <View style={[styles.statusBanner, styles.statusInfo]}>
          <Text style={styles.statusEmoji}>ℹ️</Text>
          <View style={styles.statusTextContainer}>
            <Text style={styles.statusTitleInfo}>Sin Reportes</Text>
            <Text style={styles.statusDescInfo}>
              Aún no has enviado tu primer reporte. Presiona el botón "Estoy OK" para iniciar la protección.
            </Text>
          </View>
        </View>
      );
    }

    if (status.safe) {
      return (
        <View style={[styles.statusBanner, styles.statusSuccess]}>
          <Text style={styles.statusEmoji}>🛡️</Text>
          <View style={styles.statusTextContainer}>
            <Text style={styles.statusTitleSuccess}>Protegido y a Salvo</Text>
            <Text style={styles.statusDescSuccess}>
              Tu temporizador está activo. Próximo reporte antes de:{'\n'}
              <Text style={{ fontWeight: '800' }}>
                {new Date(status.nextCheckInTime).toLocaleString()}
              </Text>
            </Text>
          </View>
        </View>
      );
    }

    return (
      <View style={[styles.statusBanner, styles.statusWarning]}>
        <Text style={styles.statusEmoji}>⚠️</Text>
        <View style={styles.statusTextContainer}>
          <Text style={styles.statusTitleWarning}>Reporte Vencido</Text>
          <Text style={styles.statusDescWarning}>
            El tiempo límite expiró. Presiona el botón "Estoy OK" de inmediato para evitar falsas alertas.
          </Text>
        </View>
      </View>
    );
  };

  const toggleTracking = async () => {
    if (isTracking) {
      try {
        try {
          await Location.stopLocationUpdatesAsync(LOCATION_TASK_NAME);
        } catch (stopErr) {
          console.log('Location tracking was not running or failed to stop:', stopErr);
        }
        setIsTracking(false);
        
        let gpsEnabled = true;
        try {
          gpsEnabled = await Location.hasServicesEnabledAsync();
        } catch (gpsErr) {
          console.log('Failed to check GPS services status:', gpsErr);
        }

        try {
          await api.put('/locations/sensor-status', {
            is_tracking_active: false,
            gps_enabled: gpsEnabled,
          });
        } catch (err) {
          console.error('Failed to update sensor status to inactive', err);
        }
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
        
        let gpsEnabled = true;
        try {
          gpsEnabled = await Location.hasServicesEnabledAsync();
        } catch (gpsErr) {
          console.log('Failed to check GPS services status:', gpsErr);
        }

        try {
          await api.put('/locations/sensor-status', {
            is_tracking_active: true,
            gps_enabled: gpsEnabled,
          });
        } catch (err) {
          console.error('Failed to update sensor status to active', err);
        }
        Alert.alert('Rastreo Activado', 'Tu ubicación se actualiza para tu núcleo.');
      } catch (e) {
        Alert.alert('Error', 'No se pudo iniciar el rastreo.');
      }
    }
  };

  return (
    <ScrollView 
      style={styles.container} 
      contentContainerStyle={styles.contentContainer}
      refreshControl={
        <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={['#dc2626']} />
      }
    >
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

      {/* Botón de SOS Destacado */}
      <TouchableOpacity 
        style={[
          styles.sosButton, 
          isSosActive ? styles.sosButtonActive : null
        ]}
        onPress={handleSilentSos}
        disabled={checkingIn}
      >
        {isSosActive ? (
          <View style={styles.sosButtonContent}>
            <ActivityIndicator size="small" color="#fff" style={{ marginRight: 8 }} />
            <Text style={styles.sosButtonTextActive}>🚨 S.O.S. ACTIVO (PULSA PARA CANCELAR)</Text>
          </View>
        ) : (
          <View style={styles.sosButtonContent}>
            <Text style={styles.sosButtonText}>🚨 S.O.S. SILENCIOSO DE EMERGENCIA</Text>
          </View>
        )}
      </TouchableOpacity>

      {/* MODULO 1: BIENESTAR */}
      <View style={styles.moduleCard}>
        <View style={styles.moduleHeader}>
          <Shield size={20} color="#2563eb" />
          <Text style={styles.moduleTitle}>Mi Bienestar Diario</Text>
        </View>

        {/* Status Banner */}
        {renderStatusBanner()}
        
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
            <Text style={styles.wellbeingDesc}>
              Avisaremos a tus contactos si pasan {freshUser?.checkin_interval_hours || user?.checkin_interval_hours} {__DEV__ ? 'minutos' : 'horas'} sin noticias.
            </Text>
          </View>
        </View>

        {/* Historial Reciente */}
        <View style={styles.historySection}>
          <Text style={styles.historyTitle}>Historial Reciente</Text>
          {loadingCheckIns && checkIns.length === 0 ? (
            <ActivityIndicator size="small" color="#2563eb" style={{ marginVertical: 10 }} />
          ) : checkIns.length === 0 ? (
            <Text style={styles.emptyHistoryText}>No hay reportes registrados aún.</Text>
          ) : (
            <View style={styles.historyList}>
              {checkIns.slice(0, 5).map((checkIn) => (
                <View key={checkIn.id} style={styles.historyItem}>
                  <View style={styles.historyItemLeft}>
                    <View style={styles.historyCheckIcon}>
                      <Text style={{ fontSize: 10, color: '#059669', fontWeight: 'bold' }}>✓</Text>
                    </View>
                    <Text style={styles.historyItemText}>Reporte verificado</Text>
                  </View>
                  <Text style={styles.historyItemDate}>
                    {new Date(checkIn.created_at).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' })}
                  </Text>
                </View>
              ))}
            </View>
          )}
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
              Tus núcleos de confianza podrán ver tu posición en el mapa.
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

        {/* Toggle Alertas de Proximidad Relativas (Radar) */}
        <View style={[styles.trackingContent, { marginTop: 12, paddingTop: 12, borderTopWidth: 1, borderTopColor: '#f3f4f6' }]}>
          <View style={styles.trackingInfo}>
            <Text style={styles.trackingStatus}>
              {(freshUser?.proximity_alerts_enabled !== false) ? '✓ Radar de Proximidad activo' : '✕ Radar de Proximidad inactivo'}
            </Text>
            <Text style={styles.trackingDesc}>
              Permite que otros familiares de tu círculo inicien radares contigo.
            </Text>
          </View>
          <Switch
            value={freshUser?.proximity_alerts_enabled !== false}
            onValueChange={toggleProximityAlerts}
            trackColor={{ false: '#e5e7eb', true: '#fca5a5' }}
            thumbColor={freshUser?.proximity_alerts_enabled !== false ? '#dc2626' : '#f4f3f4'}
          />
        </View>
      </View>

      {/* MODULO 3: MIS CÍRCULOS */}
      <View style={[styles.moduleCard, { borderLeftColor: '#3b82f6', borderLeftWidth: 4 }]}>
        <View style={styles.moduleHeader}>
          <Users size={20} color="#3b82f6" />
          <Text style={styles.moduleTitle}>Mis Núcleos Familiares</Text>
        </View>

        {loadingCircles && circles.length === 0 ? (
          <ActivityIndicator size="small" color="#3b82f6" style={{ marginVertical: 10 }} />
        ) : circles.length === 0 ? (
          // Estado sin núcleos
          <View style={styles.emptyCirclesContainer}>
            <Text style={styles.emptyCirclesText}>No participas en ningún núcleo de seguridad aún.</Text>
            
            <View style={styles.circleFormSection}>
              <Text style={styles.circleFormLabel}>Crear un Nuevo Núcleo</Text>
              <View style={styles.circleInputRow}>
                <TextInput
                  style={styles.circleInput}
                  placeholder="Nombre del núcleo (ej: Familia)"
                  placeholderTextColor="#9ca3af"
                  value={newCircleName}
                  onChangeText={setNewCircleName}
                />
                <TouchableOpacity 
                  style={styles.circleAddButton} 
                  onPress={handleCreateCircle}
                  disabled={submittingCircle}
                >
                  <Plus size={20} color="#fff" />
                </TouchableOpacity>
              </View>
            </View>

            <View style={[styles.circleFormSection, { marginTop: 16 }]}>
              <Text style={styles.circleFormLabel}>Unirse a un Núcleo Existente</Text>
              <View style={styles.circleInputRow}>
                <TextInput
                  style={[styles.circleInput, { fontFamily: 'monospace' }]}
                  placeholder="Código de 10 caracteres"
                  placeholderTextColor="#9ca3af"
                  maxLength={10}
                  value={inviteCodeInput}
                  onChangeText={setInviteCodeInput}
                  autoCapitalize="characters"
                />
                <TouchableOpacity 
                  style={[styles.circleAddButton, { backgroundColor: '#10b981' }]} 
                  onPress={handleJoinCircle}
                  disabled={submittingCircle}
                >
                  <Compass size={20} color="#fff" />
                </TouchableOpacity>
              </View>
            </View>
          </View>
        ) : (
          // Listado y detalle del núcleo seleccionado
          <View style={styles.circlesContainer}>
            {circles.length > 1 && (
              <View style={styles.circleSelectorRow}>
                <Text style={styles.circleSelectorLabel}>Núcleo Activo:</Text>
                <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.circlesTabList}>
                  {circles.map((c) => (
                    <TouchableOpacity
                      key={c.id}
                      style={[styles.circleTab, selectedCircleId === c.id && styles.circleTabActive]}
                      onPress={() => setSelectedCircleId(c.id)}
                    >
                      <Text style={[styles.circleTabText, selectedCircleId === c.id && styles.circleTabTextActive]}>
                        {c.name}
                      </Text>
                    </TouchableOpacity>
                  ))}
                </ScrollView>
              </View>
            )}

            {(() => {
              const activeCircle = circles.find(c => c.id === selectedCircleId);
              if (!activeCircle) return null;

              return (
                <View style={styles.circleDetail}>
                  <Text style={styles.circleNameTitle}>{activeCircle.name}</Text>
                  
                  {/* Tarjeta de Código de Invitación */}
                  <View style={styles.inviteCodeCard}>
                    <View style={{ flex: 1 }}>
                      <Text style={styles.inviteCodeLabel}>Código para invitar familiares:</Text>
                      <Text style={styles.inviteCodeText}>{activeCircle.invite_code}</Text>
                    </View>
                    <TouchableOpacity 
                      style={styles.copyButton}
                      onPress={() => copyToClipboard(activeCircle.invite_code)}
                    >
                      <Copy size={16} color="#4b5563" />
                      <Text style={styles.copyButtonText}>Copiar</Text>
                    </TouchableOpacity>
                  </View>

                  {/* Lista de Miembros */}
                  <Text style={styles.membersSectionTitle}>Miembros del Núcleo ({activeCircle.users.length})</Text>
                  <View style={styles.membersList}>
                    {activeCircle.users.map((member: any) => {
                      const isSelf = member.id === user?.id;
                      const isOwner = member.id === activeCircle.owner_id;
                      const isCurrentUserAdmin = activeCircle.users.find((u: any) => u.id === user?.id)?.pivot?.role === 'admin';
                      const activeSos = member.active_emergency_alerts?.find((alert: any) => (alert.type === 'silent_sos' || alert.type === 'crash') && alert.status === 'active');
                      const activeRadar = activeGeofences.find((g: any) => g.is_active && g.initiator_id === user?.id && g.target_id === member.id);
                      const isBeingTrackedByRadar = activeGeofences.find((g: any) => g.is_active && g.target_id === user?.id && g.initiator_id === member.id);

                      return (
                        <View key={member.id} style={[styles.memberCardContainer, activeSos ? styles.memberItemSosActive : null]}>
                          <View style={styles.memberCardHeader}>
                            <View style={[styles.memberAvatar, activeSos ? styles.memberAvatarSosActive : null]}>
                              <Text style={[styles.memberAvatarText, activeSos ? styles.memberAvatarTextSosActive : null]}>
                                {member.name.charAt(0).toUpperCase()}
                              </Text>
                            </View>
                            
                            <View style={styles.memberInfo}>
                              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6, flexWrap: 'wrap' }}>
                                <Text style={styles.memberName}>{member.name} {isSelf && '(Tú)'}</Text>
                                {member.is_premium && <Text style={{ fontSize: 10 }}>⭐</Text>}
                                {activeSos && (
                                  <Text style={styles.sosTextInline}>
                                    {activeSos.type === 'crash' ? '🚨 IMPACTO DETECTADO' : '🚨 S.O.S. ACTIVO'}
                                  </Text>
                                )}
                                {activeRadar && (
                                  <Text style={[styles.radarBadgeInline, { backgroundColor: '#fee2e2', color: '#dc2626' }]}>
                                    📡 Radar: {activeRadar.safe_radius_meters}m
                                  </Text>
                                )}
                                {isBeingTrackedByRadar && (
                                  <Text style={[styles.radarBadgeInline, { backgroundColor: '#dbeafe', color: '#2563eb' }]}>
                                    📡 Radar Activo
                                  </Text>
                                )}
                                {member.current_location && member.current_location.is_driving && (
                                  <Text style={styles.drivingTextInline}>🚗 {Math.round(member.current_location.speed ?? 0)} km/h</Text>
                                )}
                                {member.current_location && member.current_location.battery_level !== undefined && member.current_location.battery_level !== null && (
                                  <View style={{ flexDirection: 'row', alignItems: 'center', gap: 2, marginLeft: 2 }}>
                                    {(() => {
                                      const lvl = member.current_location.battery_level;
                                      const pct = Math.round(lvl * 100);
                                      let iconColor = '#10b981'; // Green
                                      let IconComponent = Battery;
                                      if (lvl < 0.15) {
                                        iconColor = '#ef4444'; // Red
                                        IconComponent = BatteryLow;
                                      } else if (lvl < 0.50) {
                                        iconColor = '#f59e0b'; // Yellow
                                        IconComponent = BatteryMedium;
                                      }
                                      return (
                                        <>
                                          <IconComponent size={14} color={iconColor} />
                                          <Text style={{ fontSize: 11, color: iconColor, fontWeight: '600' }}>{pct}%</Text>
                                        </>
                                      );
                                    })()}
                                  </View>
                                )}
                              </View>
                              <Text style={styles.memberRole}>
                                {isOwner ? 'Dueño' : member.pivot?.role === 'admin' ? 'Administrador' : 'Miembro'}
                              </Text>

                              {/* Detalle del estado de sensores */}
                              {member.current_location && (
                                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 4, flexWrap: 'wrap' }}>
                                  {!member.current_location.is_tracking_active && (
                                    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 2 }}>
                                      <EyeOff size={12} color="#9ca3af" />
                                      <Text style={{ fontSize: 11, color: '#9ca3af' }}>Rastreo apagado</Text>
                                    </View>
                                  )}

                                  {member.current_location.is_tracking_active && !member.current_location.gps_enabled && (
                                    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 2 }}>
                                      <MapPinOff size={12} color="#f59e0b" />
                                      <Text style={{ fontSize: 11, color: '#f59e0b', fontWeight: '500' }}>GPS desactivado</Text>
                                    </View>
                                  )}

                                  {member.current_location.is_tracking_active && member.current_location.is_offline && (
                                    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 2 }}>
                                      <WifiOff size={12} color="#ef4444" />
                                      <Text style={{ fontSize: 11, color: '#ef4444' }}>
                                        Sin señal ({(() => {
                                          const lastSeen = member.current_location.last_seen_at ? new Date(member.current_location.last_seen_at).getTime() : 0;
                                          const mins = lastSeen ? Math.round((Date.now() - lastSeen) / 60000) : 0;
                                          return mins > 0 ? `hace ${mins} min` : 'recientemente';
                                        })()})
                                      </Text>
                                    </View>
                                  )}
                                </View>
                              )}
                            </View>
                          </View>

                          <View style={styles.memberCardActions}>
                             {activeSos && !isSelf && (
                               <TouchableOpacity
                                 style={styles.sosAlertButton}
                                 onPress={() => {
                                   const frontendUrl = 'http://localhost:3000';
                                   const url = `${frontendUrl}/emergencia/${activeSos.id}`;
                                   Linking.openURL(url);
                                 }}
                               >
                                 <Text style={styles.sosAlertActionText}>Alerta</Text>
                               </TouchableOpacity>
                             )}

                            {member.current_location && member.current_location.is_tracking_active && !isSelf && (
                              <TouchableOpacity
                                style={styles.mapIconButton}
                                onPress={() => router.push({
                                  pathname: '/history',
                                  params: { memberId: String(member.id), circleId: String(activeCircle.id), mode: 'live' }
                                })}
                              >
                                <Map size={14} color="#065f46" />
                                <Text style={styles.mapActionText}>Ver</Text>
                              </TouchableOpacity>
                            )}

                            {!isSelf && (
                               <TouchableOpacity
                                 style={styles.historyIconButton}
                                 onPress={() => router.push({
                                   pathname: '/history',
                                   params: { memberId: String(member.id), circleId: String(activeCircle.id), mode: 'history' }
                                 })}
                               >
                                 <Compass size={14} color="#3730a3" />
                                 <Text style={styles.historyActionText}>Ruta</Text>
                               </TouchableOpacity>
                             )}

                             {!isSelf && (
                               activeRadar ? (
                                 <TouchableOpacity
                                   style={[styles.radarIconButton, styles.radarActiveButton]}
                                   onPress={() => handleDeactivateRadar(activeRadar.id)}
                                 >
                                   <Shield size={14} color="#dc2626" />
                                   <Text style={styles.radarActiveActionText}>Parar</Text>
                                 </TouchableOpacity>
                               ) : (
                                 <TouchableOpacity
                                   style={styles.radarIconButton}
                                   onPress={() => handleOpenRadarModal(member)}
                                 >
                                   <Shield size={14} color="#2563eb" />
                                   <Text style={styles.radarActionText}>Radar</Text>
                                 </TouchableOpacity>
                               )
                             )}

                            {isSelf ? (
                              activeCircle.owner_id !== user?.id && (
                                <TouchableOpacity
                                  style={styles.leaveMemberButton}
                                  onPress={() => handleRemoveMember(activeCircle.id, member.id)}
                                >
                                  <Text style={styles.leaveMemberText}>Salir</Text>
                                </TouchableOpacity>
                              )
                            ) : (
                              (activeCircle.owner_id === user?.id || (isCurrentUserAdmin && !isOwner)) && (
                                <TouchableOpacity
                                  style={styles.removeMemberButton}
                                  onPress={() => handleRemoveMember(activeCircle.id, member.id)}
                                >
                                  <Trash2 size={14} color="#dc2626" />
                                </TouchableOpacity>
                              )
                            )}
                          </View>
                        </View>
                      );
                    })}
                  </View>
                </View>
              );
            })()}
          </View>
        )}
      </View>

      <View style={styles.footer}>
        <Text style={styles.footerText}>Estoy Ok v1.0 • Sistema de Protección Familiar</Text>
      </View>

      {/* Modal para configurar Radar de Proximidad */}
      <Modal
        visible={radarModalVisible}
        transparent={true}
        animationType="slide"
        onRequestClose={() => {
          setRadarModalVisible(false);
          setRadarTargetMember(null);
        }}
      >
        <View style={styles.modalOverlay}>
          <View style={styles.radarModalContent}>
            <Text style={styles.radarModalTitle}>📡 Radar de Proximidad</Text>
            <Text style={styles.radarModalDesc}>
              Selecciona el radio de seguridad para recibir una alerta instantánea si {radarTargetMember?.name || 'este miembro'} se aleja más de esa distancia de ti.
            </Text>

            <View style={styles.radarOptionsContainer}>
              <TouchableOpacity
                style={styles.radarOptionButton}
                onPress={() => handleStartRadar(30)}
              >
                <Text style={styles.radarOptionText}>30 metros</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={styles.radarOptionButton}
                onPress={() => handleStartRadar(50)}
              >
                <Text style={styles.radarOptionText}>50 metros</Text>
              </TouchableOpacity>

              <TouchableOpacity
                style={styles.radarOptionButton}
                onPress={() => handleStartRadar(100)}
              >
                <Text style={styles.radarOptionText}>100 metros</Text>
              </TouchableOpacity>
            </View>

            <TouchableOpacity
              style={styles.radarCancelButton}
              onPress={() => {
                setRadarModalVisible(false);
                setRadarTargetMember(null);
              }}
            >
              <Text style={styles.radarCancelButtonText}>Cancelar</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
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
  wellbeingDesc: { fontSize: 12, color: '#6b7280', textAlign: 'center', marginTop: 4, paddingHorizontal: 20 },
  statusBanner: {
    flexDirection: 'row',
    padding: 16,
    borderRadius: 16,
    marginBottom: 20,
    alignItems: 'flex-start',
    gap: 12,
  },
  statusSuccess: {
    backgroundColor: '#ecfdf5',
    borderColor: '#a7f3d0',
    borderWidth: 1,
  },
  statusWarning: {
    backgroundColor: '#fff5f5',
    borderColor: '#feb2b2',
    borderWidth: 1,
  },
  statusInfo: {
    backgroundColor: '#eff6ff',
    borderColor: '#bfdbfe',
    borderWidth: 1,
  },
  statusEmoji: {
    fontSize: 20,
  },
  statusTextContainer: {
    flex: 1,
  },
  statusTitleSuccess: {
    fontSize: 14,
    fontWeight: '800',
    color: '#064e3b',
  },
  statusDescSuccess: {
    fontSize: 12,
    color: '#047857',
    marginTop: 2,
    lineHeight: 16,
  },
  statusTitleWarning: {
    fontSize: 14,
    fontWeight: '800',
    color: '#7f1d1d',
  },
  statusDescWarning: {
    fontSize: 12,
    color: '#b91c1c',
    marginTop: 2,
    lineHeight: 16,
  },
  statusTitleInfo: {
    fontSize: 14,
    fontWeight: '800',
    color: '#1e3a8a',
  },
  statusDescInfo: {
    fontSize: 12,
    color: '#1d4ed8',
    marginTop: 2,
    lineHeight: 16,
  },
  
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
  buttonDisabled: { opacity: 0.7, backgroundColor: '#9ca3af' },
  historySection: {
    marginTop: 10,
    marginBottom: 20,
    borderTopWidth: 1,
    borderTopColor: '#f3f4f6',
    paddingTop: 16,
  },
  historyTitle: {
    fontSize: 13,
    fontWeight: '800',
    color: '#374151',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: 10,
  },
  emptyHistoryText: {
    fontSize: 12,
    color: '#9ca3af',
    fontStyle: 'italic',
    textAlign: 'center',
    marginVertical: 10,
  },
  historyList: {
    gap: 8,
  },
  historyItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
    paddingHorizontal: 12,
    backgroundColor: '#f9fafb',
    borderRadius: 12,
  },
  historyItemLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  historyCheckIcon: {
    width: 16,
    height: 16,
    borderRadius: 8,
    backgroundColor: '#d1fae5',
    justifyContent: 'center',
    alignItems: 'center',
  },
  historyItemText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#4b5563',
  },
  historyItemDate: {
    fontSize: 11,
    fontWeight: '700',
    color: '#9ca3af',
  },
  emptyCirclesContainer: { padding: 4 },
  emptyCirclesText: { fontSize: 12, color: '#6b7280', textAlign: 'center', marginBottom: 16, fontStyle: 'italic', fontWeight: '500' },
  circleFormSection: { backgroundColor: '#f9fafb', padding: 14, borderRadius: 16, borderWidth: 1, borderColor: '#f3f4f6' },
  circleFormLabel: { fontSize: 11, fontWeight: '800', color: '#4b5563', marginBottom: 8, textTransform: 'uppercase', letterSpacing: 0.5 },
  circleInputRow: { flexDirection: 'row', gap: 10 },
  circleInput: { flex: 1, height: 44, backgroundColor: '#fff', borderWidth: 1, borderColor: '#e5e7eb', borderRadius: 12, paddingHorizontal: 12, fontSize: 13, color: '#1f2937', fontWeight: '600' },
  circleAddButton: { width: 44, height: 44, borderRadius: 12, backgroundColor: '#3b82f6', justifyContent: 'center', alignItems: 'center' },
  circlesContainer: { padding: 4 },
  circleSelectorRow: { marginBottom: 14 },
  circleSelectorLabel: { fontSize: 11, fontWeight: '800', color: '#9ca3af', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 6 },
  circlesTabList: { flexDirection: 'row' },
  circleTab: { paddingVertical: 8, paddingHorizontal: 16, backgroundColor: '#f3f4f6', borderRadius: 20, marginRight: 8, borderWidth: 1, borderColor: 'transparent' },
  circleTabActive: { backgroundColor: '#eff6ff', borderColor: '#3b82f6' },
  circleTabText: { fontSize: 12, fontWeight: '700', color: '#6b7280' },
  circleTabTextActive: { color: '#3b82f6' },
  circleDetail: { marginTop: 4 },
  circleNameTitle: { fontSize: 16, fontWeight: '900', color: '#111827', marginBottom: 12 },
  membersSectionTitle: { fontSize: 11, fontWeight: '800', color: '#9ca3af', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 8, marginTop: 8 },
  inviteCodeCard: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', backgroundColor: '#f3f4f6', borderRadius: 16, padding: 14, marginBottom: 16 },
  inviteCodeLabel: { fontSize: 10, fontWeight: '800', color: '#6b7280', textTransform: 'uppercase', letterSpacing: 0.5 },
  inviteCodeText: { fontSize: 20, fontWeight: '900', color: '#1f2937', fontFamily: 'monospace', marginTop: 2, letterSpacing: 1 },
  copyButton: { flexDirection: 'row', alignItems: 'center', gap: 6, backgroundColor: '#fff', paddingVertical: 8, paddingHorizontal: 12, borderRadius: 10, borderWidth: 1, borderColor: '#e5e7eb' },
  copyButtonText: { fontSize: 11, fontWeight: '800', color: '#4b5563' },
  membersList: { gap: 8 },
  memberItem: { flexDirection: 'row', alignItems: 'center', gap: 12, backgroundColor: '#f9fafb', borderRadius: 16, padding: 12, borderWidth: 1.5, borderColor: '#f3f4f6' },
  memberCardContainer: {
    backgroundColor: '#f9fafb',
    borderRadius: 16,
    padding: 12,
    borderWidth: 1.5,
    borderColor: '#f3f4f6',
    flexDirection: 'column',
    gap: 8,
  },
  memberCardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  memberCardActions: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginTop: 4,
    width: '100%',
    justifyContent: 'flex-start',
    flexWrap: 'wrap',
  },
  memberAvatar: { width: 36, height: 36, borderRadius: 18, backgroundColor: '#fee2e2', justifyContent: 'center', alignItems: 'center', borderWidth: 1, borderColor: '#fecaca' },
  memberAvatarText: { fontSize: 14, fontWeight: '800', color: '#dc2626' },
  memberInfo: { flex: 1 },
  memberName: { fontSize: 13, fontWeight: '700', color: '#1f2937' },
  memberRole: { fontSize: 10, color: '#9ca3af', fontWeight: '700', marginTop: 1 },
  memberActions: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  mapIconButton: { flexDirection: 'row', alignItems: 'center', gap: 4, backgroundColor: '#d1fae5', paddingVertical: 6, paddingHorizontal: 10, borderRadius: 10 },
  mapActionText: { fontSize: 11, fontWeight: '800', color: '#065f46' },
  historyIconButton: { flexDirection: 'row', alignItems: 'center', gap: 4, backgroundColor: '#e0e7ff', paddingVertical: 6, paddingHorizontal: 10, borderRadius: 10 },
  historyActionText: { fontSize: 11, fontWeight: '800', color: '#3730a3' },
  removeMemberButton: { padding: 8, backgroundColor: '#fff', borderRadius: 10, borderWidth: 1, borderColor: '#fee2e2' },
  leaveMemberButton: { paddingVertical: 6, paddingHorizontal: 10, backgroundColor: '#f3f4f6', borderRadius: 10 },
  leaveMemberText: { fontSize: 11, fontWeight: '800', color: '#ef4444' },
  sosButton: {
    backgroundColor: '#dc2626',
    marginHorizontal: 16,
    marginTop: 8,
    marginBottom: 16,
    paddingVertical: 14,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#dc2626',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 6,
    elevation: 5,
  },
  sosButtonActive: {
    backgroundColor: '#b91c1c',
    borderWidth: 2,
    borderColor: '#fca5a5',
  },
  sosButtonContent: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  sosButtonText: {
    color: '#ffffff',
    fontSize: 14,
    fontWeight: '900',
    letterSpacing: 0.5,
  },
  sosButtonTextActive: {
    color: '#ffffff',
    fontSize: 12,
    fontWeight: '900',
    letterSpacing: 0.5,
  },
  memberItemSosActive: {
    backgroundColor: '#fef2f2',
    borderColor: '#fca5a5',
    borderWidth: 1.5,
  },
  memberAvatarSosActive: {
    backgroundColor: '#dc2626',
    borderColor: '#b91c1c',
  },
  memberAvatarTextSosActive: {
    color: '#ffffff',
  },
  sosTextInline: {
    color: '#ffffff',
    fontSize: 9,
    fontWeight: '900',
    backgroundColor: '#dc2626',
    paddingVertical: 2,
    paddingHorizontal: 6,
    borderRadius: 4,
    overflow: 'hidden',
    marginLeft: 6,
  },
  drivingTextInline: {
    color: '#ffffff',
    fontSize: 9,
    fontWeight: '900',
    backgroundColor: '#3b82f6',
    paddingVertical: 2,
    paddingHorizontal: 6,
    borderRadius: 4,
    overflow: 'hidden',
    marginLeft: 6,
  },
  sosAlertButton: {
    backgroundColor: '#dc2626',
    paddingVertical: 6,
    paddingHorizontal: 10,
    borderRadius: 10,
  },
  sosAlertActionText: {
    fontSize: 11,
    fontWeight: '800',
    color: '#ffffff',
  },
  radarBadgeInline: {
    color: '#ffffff',
    fontSize: 9,
    fontWeight: '900',
    paddingVertical: 2,
    paddingHorizontal: 6,
    borderRadius: 4,
    overflow: 'hidden',
    marginLeft: 6,
  },
  radarIconButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: '#eff6ff',
    paddingVertical: 6,
    paddingHorizontal: 10,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#bfdbfe',
  },
  radarActiveButton: {
    backgroundColor: '#fee2e2',
    borderColor: '#fca5a5',
  },
  radarActionText: {
    fontSize: 11,
    fontWeight: '800',
    color: '#2563eb',
  },
  radarActiveActionText: {
    fontSize: 11,
    fontWeight: '800',
    color: '#dc2626',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  radarModalContent: {
    backgroundColor: '#ffffff',
    borderRadius: 24,
    padding: 24,
    width: '100%',
    maxWidth: 320,
    alignItems: 'center',
    elevation: 5,
    shadowColor: '#000',
    shadowOpacity: 0.1,
    shadowRadius: 10,
  },
  radarModalTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#1f2937',
    marginBottom: 10,
  },
  radarModalDesc: {
    fontSize: 13,
    color: '#6b7280',
    textAlign: 'center',
    marginBottom: 20,
    lineHeight: 18,
  },
  radarOptionsContainer: {
    width: '100%',
    gap: 10,
    marginBottom: 20,
  },
  radarOptionButton: {
    backgroundColor: '#f3f4f6',
    paddingVertical: 12,
    borderRadius: 12,
    alignItems: 'center',
    width: '100%',
  },
  radarOptionText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#374151',
  },
  radarCancelButton: {
    paddingVertical: 10,
    width: '100%',
    alignItems: 'center',
  },
  radarCancelButtonText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#9ca3af',
  }
});
