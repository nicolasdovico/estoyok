import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, Alert, ActivityIndicator, ScrollView, RefreshControl, TextInput, Clipboard, Linking } from 'react-native';
import { useAuth } from '@/context/AuthContext';
import api from '@/services/api';
import * as Location from 'expo-location';
import { LOCATION_TASK_NAME } from '@/services/locationTask';
import { MapPin, CheckCircle, Power, User as UserIcon, Shield, Settings, Users, Copy, Plus, Trash2, Compass, Map, Battery, BatteryMedium, BatteryLow } from 'lucide-react-native';
import { useRouter } from 'expo-router';

export default function HomeScreen() {
  const { user, logout } = useAuth();
  const router = useRouter();
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

  const onRefresh = async () => {
    setRefreshing(true);
    await Promise.all([
      checkTrackingStatus(),
      fetchUserData(),
      fetchCheckIns(),
      fetchCircles()
    ]);
    setRefreshing(false);
  };

  useEffect(() => {
    if (user) {
      checkTrackingStatus();
      fetchUserData();
      fetchCheckIns();
      fetchCircles();
    }
  }, [user]);

  const fetchCircles = async () => {
    if (!user) return;
    setLoadingCircles(true);
    try {
      const response = await api.get('/circles');
      setCircles(response.data);
      if (response.data.length > 0 && !selectedCircleId) {
        setSelectedCircleId(response.data[0].id);
      }
    } catch (e) {
      console.error('Error fetching circles', e);
    } finally {
      setLoadingCircles(false);
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
      setFreshUser(response.data);
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

                      return (
                        <View key={member.id} style={styles.memberItem}>
                          <View style={styles.memberAvatar}>
                            <Text style={styles.memberAvatarText}>{member.name.charAt(0).toUpperCase()}</Text>
                          </View>
                          
                          <View style={styles.memberInfo}>
                            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6, flexWrap: 'wrap' }}>
                              <Text style={styles.memberName}>{member.name} {isSelf && '(Tú)'}</Text>
                              {member.is_premium && <Text style={{ fontSize: 10 }}>⭐</Text>}
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
                          </View>

                          <View style={styles.memberActions}>
                            {member.current_location && (
                              <TouchableOpacity
                                style={styles.mapIconButton}
                                onPress={() => openInMaps(
                                  member.current_location.latitude,
                                  member.current_location.longitude
                                )}
                              >
                                <Map size={14} color="#065f46" />
                                <Text style={styles.mapActionText}>Ver</Text>
                              </TouchableOpacity>
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
  inviteCodeCard: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', backgroundColor: '#f3f4f6', borderRadius: 16, padding: 14, marginBottom: 16 },
  inviteCodeLabel: { fontSize: 10, fontWeight: '800', color: '#6b7280', textTransform: 'uppercase', letterSpacing: 0.5 },
  inviteCodeText: { fontSize: 20, fontWeight: '900', color: '#1f2937', fontFamily: 'monospace', marginTop: 2, letterSpacing: 1 },
  copyButton: { flexDirection: 'row', alignItems: 'center', gap: 6, backgroundColor: '#fff', paddingVertical: 8, paddingHorizontal: 12, borderRadius: 10, borderWidth: 1, borderColor: '#e5e7eb' },
  copyButtonText: { fontSize: 11, fontWeight: '800', color: '#4b5563' },
  membersSectionTitle: { fontSize: 11, fontWeight: '800', color: '#9ca3af', textTransform: 'uppercase', letterSpacing: 0.5, marginBottom: 10 },
  membersList: { gap: 8 },
  memberItem: { flexDirection: 'row', alignItems: 'center', gap: 12, backgroundColor: '#f9fafb', borderRadius: 16, padding: 12, borderWidth: 1, borderColor: '#f3f4f6' },
  memberAvatar: { width: 36, height: 36, borderRadius: 18, backgroundColor: '#fee2e2', justifyContent: 'center', alignItems: 'center', borderWidth: 1, borderColor: '#fecaca' },
  memberAvatarText: { fontSize: 14, fontWeight: '800', color: '#dc2626' },
  memberInfo: { flex: 1 },
  memberName: { fontSize: 13, fontWeight: '700', color: '#1f2937' },
  memberRole: { fontSize: 10, color: '#9ca3af', fontWeight: '700', marginTop: 1 },
  memberActions: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  mapIconButton: { flexDirection: 'row', alignItems: 'center', gap: 4, backgroundColor: '#d1fae5', paddingVertical: 6, paddingHorizontal: 10, borderRadius: 10 },
  mapActionText: { fontSize: 11, fontWeight: '800', color: '#065f46' },
  removeMemberButton: { padding: 8, backgroundColor: '#fff', borderRadius: 10, borderWidth: 1, borderColor: '#fee2e2' },
  leaveMemberButton: { paddingVertical: 6, paddingHorizontal: 10, backgroundColor: '#f3f4f6', borderRadius: 10 },
  leaveMemberText: { fontSize: 11, fontWeight: '800', color: '#ef4444' }
});
