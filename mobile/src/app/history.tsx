import React, { useState, useEffect, useRef } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ActivityIndicator, Alert, Dimensions, Platform, Linking } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import HistoryMap from '@/components/HistoryMap';
import Slider from '@react-native-community/slider';
import api from '@/services/api';
import { useAuth } from '@/context/AuthContext';
import { Compass, Play, Pause, ChevronLeft, ChevronRight, ArrowLeft, Star, Map } from 'lucide-react-native';

interface HistoryPoint {
  id: number;
  latitude: number;
  longitude: number;
  accuracy: number;
  recorded_at: string;
}

export default function HistoryScreen() {
  const { memberId, circleId, mode } = useLocalSearchParams();
  const { user } = useAuth();
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const mapRef = useRef<any>(null);

  const openInExternalMaps = (lat: number, lng: number) => {
    const url = `https://www.google.com/maps/search/?api=1&query=${lat},${lng}`;
    Linking.openURL(url).catch(() => {
      Alert.alert('Error', 'No se pudo abrir el mapa.');
    });
  };

  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const [points, setPoints] = useState<HistoryPoint[]>([]);
  const [loading, setLoading] = useState(false);
  const [playbackIndex, setPlaybackIndex] = useState(0);
  const [isPlaying, setIsPlaying] = useState(false);
  const [playbackSpeed, setPlaybackSpeed] = useState(1000); // 1000ms, 500ms, 200ms
  const [memberInfo, setMemberInfo] = useState<any>(null);
  const [mapReady, setMapReady] = useState(false);

  const isPremium = user?.is_premium ?? false;

  // Format date to YYYY-MM-DD in local timezone
  const getFormattedDate = (date: Date) => {
    const offset = date.getTimezoneOffset();
    const local = new Date(date.getTime() - offset * 60 * 1000);
    return local.toISOString().split('T')[0];
  };

  const getHumanReadableDate = (date: Date) => {
    return date.toLocaleDateString('es-ES', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
    });
  };

  // Fetch Member Info (from circle list to get name)
  const fetchMemberInfo = async () => {
    try {
      const response = await api.get(`/circles`);
      const circle = response.data.find((c: any) => String(c.id) === String(circleId));
      if (circle) {
        const member = circle.users.find((u: any) => String(u.id) === String(memberId));
        if (member) {
          setMemberInfo(member);
        }
      }
    } catch (e) {
      console.error('Error fetching member info', e);
    }
  };

  // Fetch history coordinates
  const fetchHistory = async () => {
    if (!memberId || !circleId) return;

    setLoading(true);
    setPoints([]);
    setPlaybackIndex(0);
    setIsPlaying(false);

    const dateStr = getFormattedDate(currentDate);

    try {
      const response = await api.get(`/circles/${circleId}/members/${memberId}/history`, {
        params: { date: dateStr }
      });
      
      const formattedPoints = response.data.map((p: any) => ({
        ...p,
        latitude: Number(p.latitude),
        longitude: Number(p.longitude),
        accuracy: p.accuracy !== null && p.accuracy !== undefined ? Number(p.accuracy) : null,
      }));
      setPoints(formattedPoints);
      
      // Auto center map on the last (most recent) point if available
      if (formattedPoints.length > 0) {
        const lastIndex = formattedPoints.length - 1;
        setPlaybackIndex(lastIndex);
      }
    } catch (e: any) {
      const msg = e.response?.data?.message || 'No se pudo obtener el historial.';
      Alert.alert('Error', msg);
    } finally {
      setLoading(false);
    }
  };

  // Center map on the last point when coordinates are loaded, or on current location as fallback, when map is ready
  useEffect(() => {
    if (!mapReady || !mapRef.current) return;

    if (points.length > 0) {
      const lastPoint = points[points.length - 1];
      mapRef.current.animateToRegion({
        latitude: lastPoint.latitude,
        longitude: lastPoint.longitude,
        latitudeDelta: 0.015,
        longitudeDelta: 0.015,
      }, 1000);
    } else if (memberInfo?.current_location) {
      mapRef.current.animateToRegion({
        latitude: Number(memberInfo.current_location.latitude),
        longitude: Number(memberInfo.current_location.longitude),
        latitudeDelta: 0.015,
        longitudeDelta: 0.015,
      }, 1000);
    }
  }, [points, mapReady, memberInfo]);

  useEffect(() => {
    fetchMemberInfo();
  }, [memberId, circleId]);

  useEffect(() => {
    fetchHistory();
  }, [currentDate, memberId, circleId]);

  // Playback timer effect
  useEffect(() => {
    let intervalId: any = null;
    if (isPlaying && points.length > 0) {
      intervalId = setInterval(() => {
        setPlaybackIndex((prev) => {
          if (prev >= points.length - 1) {
            setIsPlaying(false);
            return prev;
          }
          const next = prev + 1;
          
          // Smoothly animate map to follow the current playing point
          if (mapRef.current && points[next]) {
            mapRef.current.animateCamera({
              center: {
                latitude: points[next].latitude,
                longitude: points[next].longitude,
              }
            }, { duration: 300 });
          }
          
          return next;
        });
      }, playbackSpeed);
    }
    return () => {
      if (intervalId) clearInterval(intervalId);
    };
  }, [isPlaying, points, playbackSpeed]);

  const handlePrevDay = () => {
    const prev = new Date(currentDate);
    prev.setDate(prev.getDate() - 1);

    const now = new Date();
    // Calculate difference in days (ignoring hours)
    const diffTime = now.getTime() - prev.getTime();
    const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

    if (!isPremium && diffDays >= 1) {
      Alert.alert(
        'Acceso Limitado',
        'El historial de más de 24 horas requiere una suscripción Premium ⭐. Por favor, mejore su cuenta en la aplicación web.'
      );
      return;
    }

    if (diffDays > 30) {
      Alert.alert('Límite excedido', 'El historial solo está disponible para los últimos 30 días.');
      return;
    }

    setCurrentDate(prev);
  };

  const handleNextDay = () => {
    const next = new Date(currentDate);
    next.setDate(next.getDate() + 1);

    const now = new Date();
    now.setHours(23, 59, 59, 999);
    if (next > now) return; // Cannot query future dates

    setCurrentDate(next);
  };

  const activePoint = points[playbackIndex];

  // Helper to calculate speed between points
  const getSpeedText = () => {
    if (playbackIndex === 0) return 'Inicio';
    const prev = points[playbackIndex - 1];
    const curr = points[playbackIndex];
    if (!prev || !curr) return '0.0 km/h';

    const dist = haversineDistance(prev.latitude, prev.longitude, curr.latitude, curr.longitude); // in meters
    const timeDiff = (new Date(curr.recorded_at).getTime() - new Date(prev.recorded_at).getTime()) / 1000; // in seconds

    if (timeDiff > 0) {
      const speedKmh = (dist / timeDiff) * 3.6;
      return `${speedKmh.toFixed(1)} km/h`;
    }
    return '0.0 km/h';
  };

  // Helper for haversine distance
  const haversineDistance = (lat1: number, lon1: number, lat2: number, lon2: number): number => {
    const R = 6371e3; // Earth radius in meters
    const phi1 = (lat1 * Math.PI) / 180;
    const phi2 = (lat2 * Math.PI) / 180;
    const deltaPhi = ((lat2 - lat1) * Math.PI) / 180;
    const deltaLambda = ((lon2 - lon1) * Math.PI) / 180;

    const a =
      Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
      Math.cos(phi1) * Math.cos(phi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
  };

  return (
    <View style={styles.container}>
      {/* Map View */}
      <HistoryMap
        mapRef={mapRef}
        points={points}
        onMapReady={() => setMapReady(true)}
        activePoint={activePoint}
        playbackIndex={playbackIndex}
        getSpeedText={getSpeedText}
        memberInfo={memberInfo}
        style={styles.map}
      />

      {/* Floating Top Header Bar */}
      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        <TouchableOpacity style={styles.backButton} onPress={() => router.back()}>
          <ArrowLeft size={20} color="#1f2937" />
        </TouchableOpacity>
        <View style={styles.headerTitleContainer}>
          <Text style={styles.headerTitle}>
            {mode === 'live'
              ? `Ubicación de ${memberInfo?.name || 'Miembro'}`
              : `Ruta de ${memberInfo?.name || 'Miembro'}`}
          </Text>
          <Text style={styles.headerSubtitle}>
            {points.length} puntos {loading ? '(Cargando...)' : ''}
          </Text>
        </View>
        {(activePoint || memberInfo?.current_location) && (
          <TouchableOpacity
            style={styles.externalMapButton}
            onPress={() => {
              const lat = activePoint?.latitude ?? memberInfo.current_location.latitude;
              const lng = activePoint?.longitude ?? memberInfo.current_location.longitude;
              openInExternalMaps(Number(lat), Number(lng));
            }}
          >
            <Map size={16} color="#065f46" />
            <Text style={styles.externalMapButtonText}>Cómo llegar</Text>
          </TouchableOpacity>
        )}
      </View>

      {/* Playback & Control panel */}
      <View style={[styles.controlsCard, { paddingBottom: insets.bottom + 16 }]}>
        {/* Date Selector Slider widget */}
        <View style={styles.dateSelectorRow}>
          <TouchableOpacity style={styles.dateNavButton} onPress={handlePrevDay}>
            <ChevronLeft size={20} color="#3730a3" />
          </TouchableOpacity>
          <View style={styles.dateDisplayContainer}>
            <Text style={styles.dateText}>{getHumanReadableDate(currentDate)}</Text>
            {!isPremium && (
              <View style={styles.standardBadge}>
                <Star size={8} color="#b45309" fill="#b45309" />
                <Text style={styles.standardBadgeText}>Estándar</Text>
              </View>
            )}
          </View>
          <TouchableOpacity style={styles.dateNavButton} onPress={handleNextDay}>
            <ChevronRight size={20} color="#3730a3" />
          </TouchableOpacity>
        </View>

        {loading ? (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="small" color="#4f46e5" />
            <Text style={styles.loadingText}>Buscando coordenadas...</Text>
          </View>
        ) : points.length === 0 ? (
          <View style={styles.emptyContainer}>
            <Compass size={32} color="#9ca3af" />
            <Text style={styles.emptyText}>No se registraron ubicaciones para esta fecha.</Text>
          </View>
        ) : (
          <View style={styles.playerContainer}>
            {/* Speed & Metadata info */}
            {activePoint && (
              <View style={styles.infoRow}>
                <View style={styles.infoBox}>
                  <Text style={styles.infoLabel}>Hora</Text>
                  <Text style={styles.infoValue}>
                    {new Date(activePoint.recorded_at).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </Text>
                </View>
                <View style={styles.infoBox}>
                  <Text style={styles.infoLabel}>Velocidad Estimada</Text>
                  <Text style={styles.infoValue}>{getSpeedText()}</Text>
                </View>
                <View style={styles.infoBox}>
                  <Text style={styles.infoLabel}>Precisión</Text>
                  <Text style={styles.infoValue}>{activePoint.accuracy ? `${Number(activePoint.accuracy).toFixed(0)}m` : 'N/D'}</Text>
                </View>
              </View>
            )}

            {/* Slider track bar */}
            <View style={styles.sliderContainer}>
              <Slider
                style={styles.slider}
                minimumValue={0}
                maximumValue={points.length - 1}
                step={1}
                value={playbackIndex}
                onValueChange={(val) => setPlaybackIndex(val)}
                minimumTrackTintColor="#4f46e5"
                maximumTrackTintColor="#e0e7ff"
                thumbTintColor="#4f46e5"
              />
            </View>

            {/* Play/Pause & Speed Buttons */}
            <View style={styles.actionRow}>
              <TouchableOpacity style={styles.playButton} onPress={() => setIsPlaying(!isPlaying)}>
                {isPlaying ? (
                  <Pause size={18} color="#fff" fill="#fff" />
                ) : (
                  <Play size={18} color="#fff" fill="#fff" />
                )}
                <Text style={styles.playButtonText}>{isPlaying ? 'Pausar' : 'Reproducir'}</Text>
              </TouchableOpacity>

              <View style={styles.speedRow}>
                {[
                  { label: '1x', val: 1000 },
                  { label: '2x', val: 500 },
                  { label: '5x', val: 200 }
                ].map((s) => (
                  <TouchableOpacity
                    key={s.label}
                    style={[styles.speedButton, playbackSpeed === s.val && styles.speedButtonActive]}
                    onPress={() => setPlaybackSpeed(s.val)}
                  >
                    <Text style={[styles.speedTextLabel, playbackSpeed === s.val && styles.speedTextLabelActive]}>
                      {s.label}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            </View>
          </View>
        )}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f3f4f6',
  },
  map: {
    flex: 1,
    width: Dimensions.get('window').width,
    height: Dimensions.get('window').height,
  },
  header: {
    position: 'absolute',
    top: 0,
    left: 16,
    right: 16,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderRadius: 20,
    padding: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.1,
    shadowRadius: 10,
    elevation: 5,
  },
  backButton: {
    padding: 8,
    backgroundColor: '#f3f4f6',
    borderRadius: 12,
    marginRight: 12,
  },
  externalMapButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: '#ecfdf5',
    paddingVertical: 6,
    paddingHorizontal: 10,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: '#a7f3d0',
  },
  externalMapButtonText: {
    fontSize: 11,
    fontWeight: '700',
    color: '#065f46',
  },
  headerTitleContainer: {
    flex: 1,
  },
  headerTitle: {
    fontSize: 14,
    fontWeight: '800',
    color: '#1f2937',
  },
  headerSubtitle: {
    fontSize: 11,
    fontWeight: '600',
    color: '#6b7280',
    marginTop: 1,
  },
  controlsCard: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: '#fff',
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
    paddingTop: 20,
    paddingHorizontal: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.1,
    shadowRadius: 16,
    elevation: 8,
  },
  dateSelectorRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: '#f5f3ff',
    borderRadius: 16,
    paddingVertical: 10,
    paddingHorizontal: 12,
    borderWidth: 1,
    borderColor: '#ddd6fe',
    marginBottom: 16,
  },
  dateNavButton: {
    padding: 6,
    backgroundColor: '#fff',
    borderRadius: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  dateDisplayContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  dateText: {
    fontSize: 13,
    fontWeight: '800',
    color: '#312e81',
  },
  standardBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 3,
    backgroundColor: '#fef3c7',
    paddingVertical: 2.5,
    paddingHorizontal: 6,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: '#fde68a',
  },
  standardBadgeText: {
    fontSize: 8,
    fontWeight: '800',
    color: '#92400e',
    textTransform: 'uppercase',
  },
  loadingContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 40,
    gap: 8,
  },
  loadingText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#6b7280',
  },
  emptyContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 32,
    gap: 8,
  },
  emptyText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#9ca3af',
    textAlign: 'center',
  },
  playerContainer: {
    width: '100%',
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 8,
    marginBottom: 16,
  },
  infoBox: {
    flex: 1,
    backgroundColor: '#f9fafb',
    borderRadius: 14,
    padding: 8,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#f3f4f6',
  },
  infoLabel: {
    fontSize: 9,
    fontWeight: '700',
    color: '#9ca3af',
    textTransform: 'uppercase',
  },
  infoValue: {
    fontSize: 12,
    fontWeight: '800',
    color: '#1f2937',
    marginTop: 2,
  },
  sliderContainer: {
    width: '100%',
    height: 40,
    justifyContent: 'center',
    marginBottom: 12,
  },
  slider: {
    width: '100%',
    height: 40,
  },
  actionRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
  },
  playButton: {
    flex: 1.2,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    backgroundColor: '#4f46e5',
    paddingVertical: 12,
    borderRadius: 16,
    shadowColor: '#4f46e5',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.2,
    shadowRadius: 8,
    elevation: 3,
  },
  playButtonText: {
    color: '#fff',
    fontSize: 13,
    fontWeight: '800',
  },
  speedRow: {
    flex: 1,
    flexDirection: 'row',
    backgroundColor: '#f3f4f6',
    borderRadius: 16,
    padding: 4,
    justifyContent: 'space-between',
  },
  speedButton: {
    flex: 1,
    paddingVertical: 8,
    borderRadius: 12,
    alignItems: 'center',
  },
  speedButtonActive: {
    backgroundColor: '#fff',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 3,
    elevation: 1,
  },
  speedTextLabel: {
    fontSize: 11,
    fontWeight: '700',
    color: '#6b7280',
  },
  speedTextLabelActive: {
    color: '#4f46e5',
    fontWeight: '900',
  },
});
