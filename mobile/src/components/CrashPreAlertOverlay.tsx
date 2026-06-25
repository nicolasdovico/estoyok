import React, { useState, useEffect, useRef } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Platform, DeviceEventEmitter, AppState, Linking, Alert } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { Audio } from 'expo-av';
import * as Location from 'expo-location';
import api from '@/services/api';
import { LOCATION_TASK_NAME } from '@/services/locationTask';

export default function CrashPreAlertOverlay() {
  const [alertData, setAlertData] = useState<any>(null);
  const [countdown, setCountdown] = useState<number>(15);
  const soundRef = useRef<Audio.Sound | null>(null);
  const countdownIntervalRef = useRef<any>(null);

  const playSiren = async () => {
    if (Platform.OS === 'web') return;
    try {
      await stopSiren();
      const { sound } = await Audio.Sound.createAsync(
        { uri: 'https://www.soundjay.com/misc/sounds/siren-1.mp3' },
        { shouldPlay: true, isLooping: true, volume: 1.0 }
      );
      soundRef.current = sound;
    } catch (e) {
      console.error('Failed to play siren sound', e);
    }
  };

  const stopSiren = async () => {
    if (Platform.OS === 'web') return;
    try {
      if (soundRef.current) {
        await soundRef.current.stopAsync();
        await soundRef.current.unloadAsync();
        soundRef.current = null;
      }
    } catch (e) {
      console.error('Failed to stop siren sound', e);
    }
  };

  const startCountdownTimer = (initialValue: number) => {
    if (countdownIntervalRef.current) {
      clearInterval(countdownIntervalRef.current);
    }
    setCountdown(initialValue);
    countdownIntervalRef.current = setInterval(() => {
      setCountdown((prev) => {
        if (prev <= 1) {
          clearInterval(countdownIntervalRef.current);
          handleTimeout();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const stopCountdownTimer = () => {
    if (countdownIntervalRef.current) {
      clearInterval(countdownIntervalRef.current);
      countdownIntervalRef.current = null;
    }
  };

  const handleTimeout = async () => {
    if (alertData) {
      const gForce = alertData.gForce;
      await triggerEmergency(gForce);
    }
  };

  const handleIcodeOk = async () => {
    await stopSiren();
    stopCountdownTimer();
    setAlertData(null);
    await AsyncStorage.removeItem('active_crash_pre_alert');
    Alert.alert('Cancelado', 'Has indicado que estás bien. Falsa alarma cancelada.');
  };

  const handleHelpNow = async () => {
    if (alertData) {
      stopCountdownTimer();
      await triggerEmergency(alertData.gForce);
    }
  };

  const triggerEmergency = async (gForce: number) => {
    try {
      await stopSiren();
      setAlertData(null);
      await AsyncStorage.removeItem('active_crash_pre_alert');

      let lat = -34.6037;
      let lon = -58.3816;
      try {
        const lastLoc = await Location.getLastKnownPositionAsync();
        if (lastLoc) {
          lat = lastLoc.coords.latitude;
          lon = lastLoc.coords.longitude;
        }
        const loc = await Location.getCurrentPositionAsync({ accuracy: Location.Accuracy.Balanced });
        if (loc) {
          lat = loc.coords.latitude;
          lon = loc.coords.longitude;
        }
      } catch (err) {
        console.error('Failed to get location for crash report', err);
      }

      await Location.startLocationUpdatesAsync(LOCATION_TASK_NAME, {
        accuracy: Location.Accuracy.BestForNavigation,
        timeInterval: 5000,
        distanceInterval: 1,
        foregroundService: {
          notificationTitle: '🚨 RASTREO CRÍTICO DE IMPACTO 🚨',
          notificationBody: 'Transmitiendo ubicación en tiempo real',
          notificationColor: '#dc2626',
        },
      });

      const res = await api.post('/alerts/crash', {
        latitude: lat,
        longitude: lon,
        speed: 0,
        g_force: gForce,
      });

      const { crash_event, emergency_alert } = res.data;

      if (crash_event?.id) {
        await AsyncStorage.setItem('active_crash_event_id', crash_event.id.toString());
      }

      // Audio recording (15s post-impact)
      let recordingInstance: any = null;
      try {
        const { status } = await Audio.requestPermissionsAsync();
        if (status === 'granted') {
          await Audio.setAudioModeAsync({
            allowsRecordingIOS: true,
            playsInSilentModeIOS: true,
          });
          const { recording } = await Audio.Recording.createAsync(
            Audio.RecordingOptionsPresets.HIGH_QUALITY
          );
          recordingInstance = recording;
          console.log('Crash silent recording started');

          setTimeout(async () => {
            if (recordingInstance) {
              try {
                console.log('Stopping crash recording...');
                await recordingInstance.stopAndUnloadAsync();
                const uri = recordingInstance.getURI();
                if (uri && emergency_alert?.id) {
                  const formData = new FormData();
                  const filename = uri.split('/').pop() || 'recording.m4a';
                  const match = /\.(\w+)$/.exec(filename);
                  const type = match ? `audio/${match[1]}` : `audio/m4a`;
                  formData.append('audio', {
                    uri,
                    name: filename,
                    type,
                  } as any);

                  await api.post(`/emergency-alerts/${emergency_alert.id}/audio`, formData, {
                    headers: { 'Content-Type': 'multipart/form-data' },
                  });
                  console.log('Crash audio uploaded successfully');
                }
              } catch (recErr) {
                console.error('Error stopping/uploading crash audio', recErr);
              }
            }
          }, 15000);
        }
      } catch (err) {
        console.error('Failed to record crash audio', err);
      }

      // Native fallback SMS
      try {
        const userRes = await api.get('/user');
        const activeContacts = userRes.data?.emergency_contacts?.filter((c: any) => c.is_active) || [];
        const phones = activeContacts.map((c: any) => c.phone).filter(Boolean);
        if (phones.length > 0) {
          const emergencyUrl = `http://localhost:3000/emergencia/${emergency_alert.id}`;
          const message = `🚨 ¡ALERTA DE ACCIDENTE! He sufrido un accidente de coche. Mi ubicación en vivo: ${emergencyUrl}`;
          const separator = Platform.OS === 'ios' ? '&' : '?';
          const url = `sms:${phones.join(',')}${separator}body=${encodeURIComponent(message)}`;
          await Linking.openURL(url);
        }
      } catch (err) {
        console.error('Failed to send fallback SMS', err);
      }

      Alert.alert('Protocolo de Emergencia Activado', 'Tus contactos de emergencia han sido alertados.');
      DeviceEventEmitter.emit('crash_emergency_active', { active: true });
    } catch (err) {
      console.error('Failed to trigger crash emergency:', err);
    }
  };

  const checkActiveAlert = async () => {
    try {
      const val = await AsyncStorage.getItem('active_crash_pre_alert');
      if (val) {
        const parsed = JSON.parse(val);
        const age = Date.now() - parsed.timestamp;
        if (age < 60000) {
          const remaining = Math.max(0, 15 - Math.floor(age / 1000));
          if (remaining > 0) {
            setAlertData(parsed);
            startCountdownTimer(remaining);
            playSiren();
          } else {
            await triggerEmergency(parsed.gForce);
          }
        } else {
          await AsyncStorage.removeItem('active_crash_pre_alert');
        }
      }
    } catch (e) {
      console.error('Error checking active pre-alert', e);
    }
  };

  useEffect(() => {
    const sub = DeviceEventEmitter.addListener('crash_pre_alert', (data) => {
      setAlertData(data);
      startCountdownTimer(15);
      playSiren();
    });

    const appStateSub = AppState.addEventListener('change', (nextAppState) => {
      if (nextAppState === 'active') {
        checkActiveAlert();
      }
    });

    checkActiveAlert();

    return () => {
      sub.remove();
      appStateSub.remove();
      stopSiren();
      stopCountdownTimer();
    };
  }, []);

  if (!alertData) return null;

  return (
    <View style={styles.overlay}>
      <View style={styles.content}>
        <Text style={styles.alertIcon}>⚠️</Text>
        <Text style={styles.title}>¡IMPACTO DETECTADO!</Text>
        <Text style={styles.subtitle}>
          Se detectó una fuerza de impacto de <Text style={styles.boldText}>{alertData.gForce?.toFixed(1)} G</Text>.
        </Text>
        <Text style={styles.description}>
          Si no respondes en los siguientes segundos, enviaremos una alerta de emergencia a tu familia con tu ubicación en vivo.
        </Text>

        <View style={styles.countdownContainer}>
          <Text style={styles.countdownNumber}>{countdown}</Text>
          <Text style={styles.countdownLabel}>segundos</Text>
        </View>

        <TouchableOpacity style={styles.okButton} onPress={handleIcodeOk}>
          <Text style={styles.okButtonText}>ESTOY BIEN, CANCELAR</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.helpButton} onPress={handleHelpNow}>
          <Text style={styles.helpButtonText}>PEDIR AYUDA AHORA</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  overlay: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: '#7f1d1d', // Dark red
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 99999,
  },
  content: {
    padding: 30,
    width: '90%',
    maxWidth: 450,
    borderRadius: 20,
    backgroundColor: '#991b1b', // Medium red
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.5,
    shadowRadius: 15,
    elevation: 10,
  },
  alertIcon: {
    fontSize: 72,
    marginBottom: 10,
  },
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#ffffff',
    textAlign: 'center',
    marginBottom: 15,
  },
  subtitle: {
    fontSize: 18,
    color: '#fee2e2',
    textAlign: 'center',
    marginBottom: 10,
  },
  boldText: {
    fontWeight: 'bold',
    color: '#ffffff',
  },
  description: {
    fontSize: 14,
    color: '#fca5a5',
    textAlign: 'center',
    lineHeight: 20,
    marginBottom: 30,
  },
  countdownContainer: {
    width: 120,
    height: 120,
    borderRadius: 60,
    borderWidth: 6,
    borderColor: '#ffffff',
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 40,
  },
  countdownNumber: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  countdownLabel: {
    fontSize: 12,
    color: '#fca5a5',
    marginTop: -5,
  },
  okButton: {
    width: '100%',
    paddingVertical: 18,
    borderRadius: 12,
    backgroundColor: '#ffffff',
    alignItems: 'center',
    marginBottom: 15,
  },
  okButtonText: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#991b1b',
  },
  helpButton: {
    width: '100%',
    paddingVertical: 14,
    borderRadius: 12,
    backgroundColor: 'transparent',
    borderWidth: 2,
    borderColor: '#fca5a5',
    alignItems: 'center',
  },
  helpButtonText: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#fca5a5',
  },
});
