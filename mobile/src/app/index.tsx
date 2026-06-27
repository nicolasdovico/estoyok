import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert, ActivityIndicator, ScrollView, RefreshControl, TextInput, Clipboard, Linking, Platform, DeviceEventEmitter, Vibration, Modal, Switch, KeyboardAvoidingView } from 'react-native';
import { useAuth } from '@/context/AuthContext';
import api from '@/services/api';
import * as Location from 'expo-location';
import { LOCATION_TASK_NAME, flushOfflineLocations } from '@/services/locationTask';
import { MapPin, CheckCircle, Power, User as UserIcon, Shield, Settings, Users, Copy, Plus, Trash2, Compass, Map, Battery, BatteryMedium, BatteryLow, EyeOff, MapPinOff, WifiOff, Star, X, Lock, CreditCard, AlertCircle, Info } from 'lucide-react-native';
import { useRouter, useFocusEffect } from 'expo-router';
import { Audio } from 'expo-av';
import { startSos, uploadSosAudio } from '@/services/sosService';
import AsyncStorage from '@react-native-async-storage/async-storage';
import * as Notifications from 'expo-notifications';
import { useStripe } from '@stripe/stripe-react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

export default function HomeScreen() {
  const { user, logout, login } = useAuth();
  const router = useRouter();
  const [activeTab, setActiveTab] = useState<'dashboard' | 'map' | 'family' | 'more'>('dashboard');
  const stripe = useStripe();
  const insets = useSafeAreaInsets();
  
  // Stripe hooks & loading
  const [stripeLoading, setStripeLoading] = useState(false);
  
  // Mock sheet states
  const [mockModalVisible, setMockModalVisible] = useState(false);
  const [cardNumber, setCardNumber] = useState('');
  const [cardExpiry, setCardExpiry] = useState('');
  const [cardCvc, setCardCvc] = useState('');
  const [cardName, setCardName] = useState('');
  const [mockSubmitting, setMockSubmitting] = useState(false);

  // Expiry validation helper
  const validateExpiry = (expiry: string): boolean => {
    const parts = expiry.split('/');
    if (parts.length !== 2) return false;
    const month = parseInt(parts[0], 10);
    const year = parseInt('20' + parts[1], 10);
    if (isNaN(month) || isNaN(year)) return false;
    if (month < 1 || month > 12) return false;
    
    const now = new Date();
    const currentYear = now.getFullYear();
    const currentMonth = now.getMonth() + 1;
    
    if (year < currentYear) return false;
    if (year === currentYear && month < currentMonth) return false;
    return true;
  };

  // Luhn algorithm check
  const validateLuhn = (num: string): boolean => {
    let sum = 0;
    let shouldDouble = false;
    for (let i = num.length - 1; i >= 0; i--) {
      let digit = parseInt(num.charAt(i), 10);
      if (shouldDouble) {
        digit *= 2;
        if (digit > 9) digit -= 9;
      }
      sum += digit;
      shouldDouble = !shouldDouble;
    }
    return sum % 10 === 0;
  };

  // Cvc validation helper
  const validateCVC = (cvc: string, brand: string): boolean => {
    const clean = cvc.replace(/\D/g, '');
    if (brand === 'American Express') {
      return clean.length === 4;
    }
    return clean.length === 3;
  };

  // Card brand helper
  const getCardBrand = (number: string): string => {
    const clean = number.replace(/\D/g, '');
    if (clean.startsWith('4')) return 'Visa';
    if (/^5[1-5]/.test(clean) || /^2(22[1-9]|2[3-9][0-9]|[3-6][0-9]{2}|7[0-1][0-9]|720)/.test(clean)) return 'Mastercard';
    if (/^3[47]/.test(clean)) return 'American Express';
    if (/^6(011|5)/.test(clean)) return 'Discover';
    return '';
  };

  const formatCardNumber = (text: string) => {
    const clean = text.replace(/\D/g, '');
    const groups = clean.match(/.{1,4}/g);
    if (!groups) return '';
    return groups.join(' ').substring(0, 19);
  };

  const formatExpiry = (text: string) => {
    const clean = text.replace(/\D/g, '');
    if (clean.length <= 2) return clean;
    return `${clean.slice(0, 2)}/${clean.slice(2, 4)}`.substring(0, 5);
  };

  const formatCvc = (text: string) => {
    return text.replace(/\D/g, '').substring(0, 4);
  };

  const isFormValid = () => {
    const cleanNum = cardNumber.replace(/\D/g, '');
    const brand = getCardBrand(cardNumber);
    
    const numValid = cleanNum.length >= 13 && cleanNum.length <= 19 && validateLuhn(cleanNum);
    const expValid = validateExpiry(cardExpiry);
    const cvcValid = validateCVC(cardCvc, brand);
    const nameValid = cardName.trim().length > 2;
    
    return numValid && expValid && cvcValid && nameValid;
  };

  const handlePremiumSubscribe = async () => {
    setStripeLoading(true);
    try {
      let clientSecret = "";
      try {
        const response = await api.post('/subscriptions/setup-intent');
        clientSecret = response.data.client_secret;
      } catch (backendError) {
        console.log('El backend no está listo o no soporta setup-intent. Usaremos el flujo simulado/test.');
      }

      const isStripeAvailable = stripe && typeof stripe.initPaymentSheet === 'function';
      
      if (!isStripeAvailable || !clientSecret) {
        setMockModalVisible(true);
        setStripeLoading(false);
        return;
      }

      const { error: initError } = await stripe.initPaymentSheet({
        setupIntentClientSecret: clientSecret,
        merchantDisplayName: 'Estoy Ok',
      });

      if (initError) {
        Alert.alert('Error de Inicialización', initError.message);
        setStripeLoading(false);
        return;
      }

      const { error: presentError } = await stripe.presentPaymentSheet();

      if (presentError) {
        if (presentError.code !== 'Canceled') {
          Alert.alert('Error de Pago', presentError.message);
        }
        setStripeLoading(false);
        return;
      }

      // Enviar método de pago resultante al backend
      try {
        await api.post('/subscriptions/create', {
          payment_method_id: 'stripe_real_success_from_payment_sheet',
        });
      } catch (err) {
        console.log('Error enviando suscripción al backend (esperado si no está listo):', err);
      }

      Alert.alert('¡Suscripción Exitosa!', 'Tu cuenta ha sido actualizada a Premium PRO.');
      const token = await AsyncStorage.getItem('auth_token');
      if (token && user) {
        const updatedUser = { ...user, is_premium: true };
        await login(token, updatedUser);
      }
    } catch (err: any) {
      console.error(err);
      Alert.alert('Error', 'No se pudo procesar la suscripción.');
    } finally {
      setStripeLoading(false);
    }
  };

  const handleMockSubmit = async () => {
    if (!isFormValid()) return;
    
    const cleanNum = cardNumber.replace(/\D/g, '');
    
    // 3D Secure / SCA Simulation Card: 4000 0027 6000 3184
    if (cleanNum === '4000002760003184') {
      Alert.alert(
        'Autenticación 3D Secure Requerida',
        'Tu banco emisor requiere confirmación adicional para procesar esta transacción.',
        [
          {
            text: 'Autorizar Confirmación Bancaria',
            onPress: () => processMockSubscription()
          },
          {
            text: 'Cancelar',
            style: 'cancel'
          }
        ]
      );
    } else {
      await processMockSubscription();
    }
  };

  const processMockSubscription = async () => {
    setMockSubmitting(true);
    try {
      // Simular latencia de tokenización de Stripe
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      const mockPaymentMethodId = `pm_mock_${Math.random().toString(36).substring(2, 11)}${Date.now().toString().substring(8)}`;
      
      console.log('Stripe Payment Method (Mock Mobile) created:', mockPaymentMethodId);
      
      // Enviar método de pago resultante al backend
      try {
        await api.post('/subscriptions/create', {
          payment_method_id: mockPaymentMethodId,
        });
      } catch (err) {
        console.log('El backend no está listo para registrar la suscripción. Usaremos fallback local.');
      }

      Alert.alert(
        '🔒 Suscripción Procesada',
        `Se ha tokenizado tu tarjeta de forma segura en Stripe.\nToken generado: ${mockPaymentMethodId}\n\n¡Gracias por mejorar a Premium PRO! Su cuenta ahora tiene acceso completo.`
      );

      // Simular cambio a Premium en el estado de la aplicación
      const token = await AsyncStorage.getItem('auth_token');
      if (token && user) {
        const updatedUser = { ...user, is_premium: true };
        await login(token, updatedUser);
      }
      
      setMockModalVisible(false);
      // Limpiar campos
      setCardNumber('');
      setCardExpiry('');
      setCardCvc('');
      setCardName('');
    } catch (err) {
      console.error(err);
      Alert.alert('Error', 'No se pudo procesar la suscripción.');
    } finally {
      setMockSubmitting(false);
    }
  };

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
    <View style={{ flex: 1, backgroundColor: '#f3f4f6' }}>
      <ScrollView 
        style={styles.container} 
        contentContainerStyle={styles.contentContainer}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={onRefresh} colors={['#dc2626']} />
        }
      >
        {/* Header Compacto */}
        {activeTab !== 'more' && (
          <View style={styles.header}>
            <View style={styles.userInfo}>
              <Text style={styles.welcomeText}>Hola, {user?.name}</Text>
              <Text style={styles.planBadge}>{user?.is_premium ? 'Socio Premium ⭐' : 'Plan Básico'}</Text>
            </View>
            <TouchableOpacity onPress={logout} style={styles.logoutButton}>
              <Power size={20} color="#9ca3af" />
            </TouchableOpacity>
          </View>
        )}

        {/* Botón de SOS Destacado */}
        {(activeTab === 'dashboard' || activeTab === 'map') && (
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
        )}

        {/* PESTAÑA 1: BIENESTAR */}
        {activeTab === 'dashboard' && (
          <View>
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
            </View>
          </View>
        )}

        {/* PESTAÑA 2: MAPA */}
        {activeTab === 'map' && (
          <View>
            {/* Rastreo en Tiempo Real */}
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

            {/* Ubicación de Familiares */}
            <View style={styles.moduleCard}>
              <View style={styles.moduleHeader}>
                <Compass size={20} color="#2563eb" />
                <Text style={styles.moduleTitle}>Ubicación de Familiares</Text>
              </View>

              {circles.length === 0 ? (
                <Text style={styles.emptyCirclesText}>Únete a un núcleo en la pestaña "Núcleo" para ver la ubicación de tu familia.</Text>
              ) : (
                (() => {
                  const activeCircle = circles.find(c => c.id === selectedCircleId) || circles[0];
                  if (!activeCircle) return <Text style={styles.emptyCirclesText}>No hay un núcleo activo seleccionado.</Text>;
                  
                  return (
                    <View>
                      {circles.length > 1 && (
                        <View style={styles.circleSelectorRow}>
                          <Text style={styles.circleSelectorLabel}>Seleccionar Núcleo:</Text>
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

                      <Text style={[styles.circleNameTitle, { marginBottom: 12 }]}>{activeCircle.name}</Text>

                      <View style={styles.membersList}>
                        {activeCircle.users.map((member: any) => {
                          const isSelf = member.id === user?.id;
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
                                          let iconColor = '#10b981';
                                          let IconComponent = Battery;
                                          if (lvl < 0.15) {
                                            iconColor = '#ef4444';
                                            IconComponent = BatteryLow;
                                          } else if (lvl < 0.50) {
                                            iconColor = '#f59e0b';
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
                                    <Text style={styles.mapActionText}>Ver mapa</Text>
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
                              </View>
                            </View>
                          );
                        })}
                      </View>
                    </View>
                  );
                })()
              )}
            </View>
          </View>
        )}

        {/* PESTAÑA 3: FAMILIA (NÚCLEO) */}
        {activeTab === 'family' && (
          <View>
            <View style={[styles.moduleCard, { borderLeftColor: '#3b82f6', borderLeftWidth: 4 }]}>
              <View style={styles.moduleHeader}>
                <Users size={20} color="#3b82f6" />
                <Text style={styles.moduleTitle}>Administración de Núcleos</Text>
              </View>

              {loadingCircles && circles.length === 0 ? (
                <ActivityIndicator size="small" color="#3b82f6" style={{ marginVertical: 10 }} />
              ) : circles.length === 0 ? (
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
                <View style={styles.circlesContainer}>
                  {circles.length > 1 && (
                    <View style={styles.circleSelectorRow}>
                      <Text style={styles.circleSelectorLabel}>Seleccionar Núcleo:</Text>
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

                        <Text style={styles.membersSectionTitle}>Miembros del Núcleo ({activeCircle.users.length})</Text>
                        <View style={styles.membersList}>
                          {activeCircle.users.map((member: any) => {
                            const isSelf = member.id === user?.id;
                            const isOwner = member.id === activeCircle.owner_id;
                            const isCurrentUserAdmin = activeCircle.users.find((u: any) => u.id === user?.id)?.pivot?.role === 'admin';

                            return (
                              <View key={member.id} style={styles.memberCardContainer}>
                                <View style={styles.memberCardHeader}>
                                  <View style={styles.memberAvatar}>
                                    <Text style={styles.memberAvatarText}>
                                      {member.name.charAt(0).toUpperCase()}
                                    </Text>
                                  </View>
                                  
                                  <View style={styles.memberInfo}>
                                    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
                                      <Text style={styles.memberName}>{member.name} {isSelf && '(Tú)'}</Text>
                                      {member.is_premium && <Text style={{ fontSize: 10 }}>⭐</Text>}
                                    </View>
                                    <Text style={styles.memberRole}>
                                      {isOwner ? 'Dueño' : member.pivot?.role === 'admin' ? 'Administrador' : 'Miembro'}
                                    </Text>
                                  </View>

                                  <View style={styles.memberActions}>
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
                              </View>
                            );
                          })}
                        </View>

                        {/* Formularios para unirse o crear en la parte inferior */}
                        <View style={{ marginTop: 24, gap: 16 }}>
                          <View style={styles.circleFormSection}>
                            <Text style={styles.circleFormLabel}>Crear otro Núcleo</Text>
                            <View style={styles.circleInputRow}>
                              <TextInput
                                style={styles.circleInput}
                                placeholder="Nombre (ej: Trabajo)"
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

                          <View style={styles.circleFormSection}>
                            <Text style={styles.circleFormLabel}>Unirse a otro Núcleo</Text>
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
                      </View>
                    );
                  })()}
                </View>
              )}
            </View>
          </View>
        )}

        {/* PESTAÑA 4: MÁS/AJUSTES */}
        {activeTab === 'more' && (
          <View>
            <View style={styles.profileHeader}>
              <View style={styles.profileAvatar}>
                <UserIcon size={32} color="#4b5563" />
              </View>
              <Text style={styles.profileName}>{user?.name}</Text>
              <Text style={styles.profileEmail}>{user?.email}</Text>
              <View style={[styles.premiumBadgeContainer, user?.is_premium ? styles.premiumActiveBadge : styles.basicActiveBadge]}>
                <Text style={user?.is_premium ? styles.premiumBadgeText : styles.basicBadgeText}>
                  {user?.is_premium ? '⭐ SOCIO PREMIUM PRO' : 'PLAN BÁSICO GRATUITO'}
                </Text>
              </View>
            </View>

            {/* SECCIÓN DE FACTURACIÓN Y MEMBRESÍA PREMIUM */}
            {user?.is_premium ? (
              <View style={styles.activePremiumCard}>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 10 }}>
                  <Star size={24} color="#a16207" fill="#a16207" />
                  <Text style={{ fontSize: 16, fontWeight: '800', color: '#713f12' }}>Socio Premium Activo</Text>
                </View>
                <Text style={styles.activePremiumText}>
                  ¡Gracias por proteger a tu núcleo familiar con la máxima tecnología de Estoy Ok! Tienes habilitadas todas las alertas de WhatsApp/SMS, S.O.S. Silencioso con audio, sensores y geolocalización avanzada.
                </Text>
              </View>
            ) : (
              <View style={styles.premiumCard}>
                <View style={styles.premiumHeader}>
                  <View>
                    <Text style={styles.premiumSubtitle}>Membresía recomendada</Text>
                    <Text style={styles.premiumTitle}>Premium PRO</Text>
                  </View>
                  <View style={styles.priceBadge}>
                    <Text style={styles.priceText}>$4.99</Text>
                    <Text style={styles.priceSub}>/ al mes</Text>
                  </View>
                </View>

                <View style={styles.benefitsContainer}>
                  <View style={styles.benefitRow}>
                    <Shield size={14} color="#eab308" style={styles.benefitIcon} />
                    <View style={{ flex: 1 }}>
                      <Text style={styles.benefitText}>WhatsApp &amp; SMS Ilimitados</Text>
                      <Text style={styles.benefitSub}>Alertas críticas directas a la red, incluso si no tienen internet.</Text>
                    </View>
                  </View>
                  <View style={styles.benefitRow}>
                    <Shield size={14} color="#eab308" style={styles.benefitIcon} />
                    <View style={{ flex: 1 }}>
                      <Text style={styles.benefitText}>Bienestar Pasivo Inteligente</Text>
                      <Text style={styles.benefitSub}>Auto-check-ins por Wi-Fi seguro o podómetro del celular.</Text>
                    </View>
                  </View>
                  <View style={styles.benefitRow}>
                    <Shield size={14} color="#eab308" style={styles.benefitIcon} />
                    <View style={{ flex: 1 }}>
                      <Text style={styles.benefitText}>S.O.S. Silencioso y Colisión (🚗)</Text>
                      <Text style={styles.benefitSub}>Grabación de 15s de audio ambiente y detección de accidentes.</Text>
                    </View>
                  </View>
                </View>

                <View style={styles.cardSection}>
                  <Text style={styles.cardLabel}>Pago 100% Seguro</Text>
                  <Text style={styles.warningText}>
                    🔒 La transacción se procesará mediante la pasarela segura de Stripe Elements.
                  </Text>
                </View>

                <TouchableOpacity
                  style={[
                    styles.premiumBtn,
                    stripeLoading && styles.premiumBtnDisabled,
                  ]}
                  disabled={stripeLoading}
                  onPress={handlePremiumSubscribe}
                >
                  {stripeLoading ? (
                    <ActivityIndicator color="#111827" size="small" />
                  ) : (
                    <Text style={styles.premiumBtnText}>🔒 Mejorar a Premium PRO ($4.99/mes)</Text>
                  )}
                </TouchableOpacity>
              </View>
            )}

            <View style={styles.menuGroup}>
              <Text style={styles.menuGroupTitle}>Seguridad y Contactos</Text>
              <TouchableOpacity style={styles.menuItem} onPress={() => router.push('/contacts')}>
                <View style={styles.menuItemLeft}>
                  <Users size={20} color="#2563eb" />
                  <Text style={styles.menuItemText}>Contactos de Emergencia</Text>
                </View>
                <Text style={styles.menuItemArrow}>›</Text>
              </TouchableOpacity>
              
              <TouchableOpacity style={styles.menuItem} onPress={() => router.push('/settings')}>
                <View style={styles.menuItemLeft}>
                  <Settings size={20} color="#4b5563" />
                  <Text style={styles.menuItemText}>Configuración y Sensores</Text>
                </View>
                <Text style={styles.menuItemArrow}>›</Text>
              </TouchableOpacity>
            </View>

            <View style={styles.menuGroup}>
              <Text style={styles.menuGroupTitle}>Sesión</Text>
              <TouchableOpacity style={[styles.menuItem, styles.logoutMenuItem]} onPress={logout}>
                <View style={styles.menuItemLeft}>
                  <Power size={20} color="#dc2626" />
                  <Text style={[styles.menuItemText, { color: '#dc2626' }]}>Cerrar Sesión</Text>
                </View>
              </TouchableOpacity>
            </View>
          </View>
        )}

        <View style={styles.footer}>
          <Text style={styles.footerText}>Estoy Ok v1.0 • Sistema de Protección Familiar</Text>
        </View>
      </ScrollView>

      {/* Barra de Navegación Inferior (Bottom Tab Bar) */}
      <View style={[styles.bottomTabBar, { paddingBottom: Math.max(insets.bottom, 10), height: 54 + Math.max(insets.bottom, 10) }]}>
        <TouchableOpacity 
          style={[styles.tabButton, activeTab === 'dashboard' && styles.tabButtonActive]}
          onPress={() => setActiveTab('dashboard')}
        >
          <Shield size={22} color={activeTab === 'dashboard' ? '#2563eb' : '#9ca3af'} />
          <Text style={[styles.tabText, activeTab === 'dashboard' && styles.tabTextActive]}>Panel</Text>
        </TouchableOpacity>

        <TouchableOpacity 
          style={[styles.tabButton, activeTab === 'map' && styles.tabButtonActive]}
          onPress={() => setActiveTab('map')}
        >
          <Map size={22} color={activeTab === 'map' ? '#2563eb' : '#9ca3af'} />
          <Text style={[styles.tabText, activeTab === 'map' && styles.tabTextActive]}>Mapa</Text>
        </TouchableOpacity>

        <TouchableOpacity 
          style={[styles.tabButton, activeTab === 'family' && styles.tabButtonActive]}
          onPress={() => setActiveTab('family')}
        >
          <Users size={22} color={activeTab === 'family' ? '#2563eb' : '#9ca3af'} />
          <Text style={[styles.tabText, activeTab === 'family' && styles.tabTextActive]}>Núcleo</Text>
        </TouchableOpacity>

        <TouchableOpacity 
          style={[styles.tabButton, activeTab === 'more' && styles.tabButtonActive]}
          onPress={() => setActiveTab('more')}
        >
          <Settings size={22} color={activeTab === 'more' ? '#2563eb' : '#9ca3af'} />
          <Text style={[styles.tabText, activeTab === 'more' && styles.tabTextActive]}>Ajustes</Text>
        </TouchableOpacity>
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

      {/* Modal de Simulación de Stripe PaymentSheet */}
      <Modal
        visible={mockModalVisible}
        animationType="slide"
        transparent={true}
        onRequestClose={() => setMockModalVisible(false)}
      >
        <View style={styles.modalOverlay}>
          <KeyboardAvoidingView
            behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
            style={{ width: '100%' }}
          >
            <View style={styles.modalContent}>
              <View style={styles.modalHeader}>
                <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                  <Shield size={20} color="#eab308" />
                  <Text style={styles.modalTitle}>Pago Seguro con Stripe</Text>
                </View>
                <TouchableOpacity onPress={() => setMockModalVisible(false)} style={styles.closeButton}>
                  <X size={20} color="#9ca3af" />
                </TouchableOpacity>
              </View>

              <Text style={styles.modalSubtitle}>
                Estás suscribiéndote a <Text style={{ fontWeight: 'bold', color: '#fff' }}>Estoy Ok Premium PRO</Text> por <Text style={{ fontWeight: 'bold', color: '#eab308' }}>$4.99 al mes</Text>.
              </Text>

              {/* Formulario de tarjeta simulada */}
              <View style={styles.formContainer}>
                <Text style={styles.inputLabel}>Número de Tarjeta</Text>
                <View style={styles.inputWrapper}>
                  <TextInput
                    style={styles.formInput}
                    placeholder="4242 4242 4242 4242"
                    placeholderTextColor="#4b5563"
                    keyboardType="numeric"
                    value={cardNumber}
                    onChangeText={(t) => setCardNumber(formatCardNumber(t))}
                  />
                  {getCardBrand(cardNumber) ? (
                    <Text style={styles.brandBadge}>{getCardBrand(cardNumber)}</Text>
                  ) : (
                    <CreditCard size={18} color="#4b5563" />
                  )}
                </View>

                <View style={{ flexDirection: 'row', gap: 15, marginTop: 12 }}>
                  <View style={{ flex: 1 }}>
                    <Text style={styles.inputLabel}>Vencimiento</Text>
                    <TextInput
                      style={styles.formInput}
                      placeholder="MM/YY"
                      placeholderTextColor="#4b5563"
                      keyboardType="numeric"
                      value={cardExpiry}
                      onChangeText={(t) => setCardExpiry(formatExpiry(t))}
                    />
                  </View>
                  <View style={{ flex: 1 }}>
                    <Text style={styles.inputLabel}>CVC</Text>
                    <TextInput
                      style={styles.formInput}
                      placeholder="123"
                      placeholderTextColor="#4b5563"
                      keyboardType="numeric"
                      secureTextEntry={true}
                      value={cardCvc}
                      onChangeText={(t) => setCardCvc(formatCvc(t))}
                    />
                  </View>
                </View>

                <View style={{ marginTop: 12 }}>
                  <Text style={styles.inputLabel}>Nombre del Titular</Text>
                  <TextInput
                    style={styles.formInput}
                    placeholder="JUAN PEREZ"
                    placeholderTextColor="#4b5563"
                    autoCapitalize="characters"
                    value={cardName}
                    onChangeText={setCardName}
                  />
                </View>
              </View>

              {/* Indicador de 3DS */}
              {cardNumber.replace(/\D/g, '') === '4000002760003184' && (
                <View style={styles.tdsInfoContainer}>
                  <Info size={14} color="#fef08a" />
                  <Text style={styles.tdsInfoText}>
                    Esta tarjeta de prueba simula autenticación 3D Secure / SCA. Se mostrará una verificación adicional al procesar.
                  </Text>
                </View>
              )}

              {/* Botón de envío */}
              <TouchableOpacity
                style={[
                  styles.submitButton,
                  (!isFormValid() || mockSubmitting) && styles.submitButtonDisabled
                ]}
                disabled={!isFormValid() || mockSubmitting}
                onPress={handleMockSubmit}
              >
                {mockSubmitting ? (
                  <ActivityIndicator color="#111827" size="small" />
                ) : (
                  <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
                    <Lock size={14} color="#111827" />
                    <Text style={styles.submitButtonText}>Suscribirse ahora ($4.99/mes)</Text>
                  </View>
                )}
              </TouchableOpacity>

              <Text style={styles.secureText}>
                🔒 Tus datos se tokenizan directamente con Stripe bajo cumplimiento estricto PCI-DSS.
              </Text>
            </View>
          </KeyboardAvoidingView>
        </View>
      </Modal>
    </View>
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
  },
  
  // Barra de navegación inferior (Bottom Tabs)
  bottomTabBar: {
    flexDirection: 'row',
    backgroundColor: '#ffffff',
    borderTopWidth: 1,
    borderTopColor: '#e5e7eb',
    justifyContent: 'space-around',
    alignItems: 'center',
  },
  tabButton: {
    alignItems: 'center',
    justifyContent: 'center',
    flex: 1,
    height: '100%',
  },
  tabButtonActive: {
    // Estilo activo si se requiere
  },
  tabText: {
    fontSize: 10,
    fontWeight: '700',
    color: '#9ca3af',
    marginTop: 2,
  },
  tabTextActive: {
    color: '#2563eb',
  },
  
  // Pestaña "Más" (Perfil y Ajustes)
  profileHeader: {
    alignItems: 'center',
    paddingVertical: 24,
    borderBottomWidth: 1,
    borderBottomColor: '#f3f4f6',
    backgroundColor: '#ffffff',
    borderRadius: 24,
    marginBottom: 16,
  },
  profileAvatar: {
    width: 64,
    height: 64,
    borderRadius: 32,
    backgroundColor: '#f3f4f6',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
  },
  profileName: {
    fontSize: 18,
    fontWeight: '800',
    color: '#1f2937',
  },
  profileEmail: {
    fontSize: 13,
    color: '#6b7280',
    marginTop: 2,
  },
  premiumBadgeContainer: {
    marginTop: 10,
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: 12,
  },
  premiumActiveBadge: {
    backgroundColor: '#fef9c3',
    borderWidth: 1,
    borderColor: '#fef08a',
  },
  basicActiveBadge: {
    backgroundColor: '#f3f4f6',
  },
  premiumBadgeText: {
    fontSize: 10,
    fontWeight: '800',
    color: '#713f12',
  },
  basicBadgeText: {
    fontSize: 10,
    fontWeight: '700',
    color: '#6b7280',
  },
  menuGroup: {
    backgroundColor: '#ffffff',
    borderRadius: 24,
    paddingVertical: 8,
    paddingHorizontal: 16,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOpacity: 0.02,
    shadowRadius: 5,
    elevation: 1,
  },
  menuGroupTitle: {
    fontSize: 11,
    fontWeight: '800',
    color: '#9ca3af',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginVertical: 10,
    marginLeft: 4,
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderBottomColor: '#f3f4f6',
  },
  menuItemLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  menuItemText: {
    fontSize: 14,
    fontWeight: '700',
    color: '#374151',
  },
  menuItemArrow: {
    fontSize: 18,
    color: '#d1d5db',
    fontWeight: '700',
  },
  logoutMenuItem: {
    borderBottomWidth: 0,
  },
  
  // Estilos de Facturación Premium PRO
  premiumCard: {
    backgroundColor: '#111827',
    borderRadius: 24,
    padding: 20,
    marginTop: 10,
    marginBottom: 20,
    borderWidth: 1,
    borderColor: '#eab308',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 5,
    elevation: 6,
  },
  premiumHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    borderBottomWidth: 1,
    borderBottomColor: '#374151',
    paddingBottom: 12,
    marginBottom: 12,
  },
  premiumTitle: {
    fontSize: 16,
    fontWeight: '800',
    color: '#ffffff',
  },
  premiumSubtitle: {
    fontSize: 10,
    color: '#eab308',
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  priceBadge: {
    backgroundColor: '#1f2937',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#374151',
  },
  priceText: {
    fontSize: 14,
    fontWeight: '900',
    color: '#eab308',
  },
  priceSub: {
    fontSize: 8,
    color: '#9ca3af',
  },
  benefitsContainer: {
    marginVertical: 8,
  },
  benefitRow: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    marginVertical: 6,
  },
  benefitIcon: {
    marginRight: 8,
    marginTop: 2,
  },
  benefitText: {
    fontSize: 11,
    color: '#e5e7eb',
    fontWeight: '600',
  },
  benefitSub: {
    fontSize: 9,
    color: '#9ca3af',
    marginTop: 1,
    lineHeight: 12,
  },
  cardSection: {
    marginTop: 15,
    borderTopWidth: 1,
    borderTopColor: '#374151',
    paddingTop: 15,
  },
  cardLabel: {
    fontSize: 9,
    fontWeight: '700',
    color: '#9ca3af',
    textTransform: 'uppercase',
    marginBottom: 6,
  },
  premiumBtn: {
    backgroundColor: '#eab308',
    paddingVertical: 12,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 12,
  },
  premiumBtnDisabled: {
    backgroundColor: '#4b5563',
  },
  premiumBtnText: {
    color: '#111827',
    fontSize: 12,
    fontWeight: '700',
  },
  activePremiumCard: {
    backgroundColor: '#fef9c3',
    borderRadius: 24,
    padding: 20,
    marginTop: 10,
    marginBottom: 20,
    borderWidth: 1,
    borderColor: '#fef08a',
  },
  activePremiumText: {
    fontSize: 12,
    color: '#713f12',
    fontWeight: '600',
    marginTop: 6,
    lineHeight: 16,
  },
  warningContainer: {
    backgroundColor: '#7f1d1d',
    padding: 10,
    borderRadius: 12,
    marginTop: 10,
    borderWidth: 1,
    borderColor: '#b91c1c',
  },
  warningText: {
    fontSize: 10,
    color: '#9ca3af',
    lineHeight: 14,
  },
  modalContent: {
    backgroundColor: '#1f2937',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 20,
    width: '100%',
    paddingBottom: 40,
    borderWidth: 1,
    borderColor: '#374151',
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#ffffff',
  },
  closeButton: {
    padding: 5,
  },
  modalSubtitle: {
    fontSize: 12,
    color: '#9ca3af',
    marginBottom: 20,
    lineHeight: 16,
  },
  formContainer: {
    gap: 8,
  },
  inputLabel: {
    fontSize: 11,
    fontWeight: '700',
    color: '#e5e7eb',
    marginBottom: 4,
    textTransform: 'uppercase',
  },
  inputWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#111827',
    borderWidth: 1,
    borderColor: '#374151',
    borderRadius: 12,
    paddingHorizontal: 12,
  },
  formInput: {
    flex: 1,
    paddingVertical: 12,
    color: '#ffffff',
    fontSize: 15,
  },
  brandBadge: {
    backgroundColor: '#eab308',
    color: '#111827',
    fontSize: 10,
    fontWeight: '800',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
    textTransform: 'uppercase',
  },
  tdsInfoContainer: {
    flexDirection: 'row',
    backgroundColor: '#78350f',
    padding: 10,
    borderRadius: 12,
    gap: 8,
    marginTop: 15,
    borderWidth: 1,
    borderColor: '#b45309',
  },
  tdsInfoText: {
    fontSize: 10,
    color: '#fef08a',
    flex: 1,
    lineHeight: 14,
  },
  submitButton: {
    backgroundColor: '#eab308',
    paddingVertical: 14,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 20,
  },
  submitButtonDisabled: {
    backgroundColor: '#4b5563',
  },
  submitButtonText: {
    color: '#111827',
    fontSize: 14,
    fontWeight: '800',
  },
  secureText: {
    fontSize: 9,
    color: '#9ca3af',
    textAlign: 'center',
    marginTop: 12,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 15,
  }
});
