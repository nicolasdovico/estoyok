import React, { useState, useEffect, useRef } from 'react';
import { View, Text, StyleSheet, TouchableOpacity, Alert, ActivityIndicator, ScrollView, Switch, TextInput, KeyboardAvoidingView, Platform } from 'react-native';
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
  const scrollViewRef = useRef<ScrollView>(null);
  const [currentInterval, setCurrentInterval] = useState(user?.checkin_interval_hours || 24);
  const [quietHoursEnabled, setQuietHoursEnabled] = useState(false);
  const [startHours, setStartHours] = useState('23');
  const [startMinutes, setStartMinutes] = useState('00');
  const [endHours, setEndHours] = useState('07');
  const [endMinutes, setEndMinutes] = useState('00');
  const [allowSmsWhatsappCheckin, setAllowSmsWhatsappCheckin] = useState(false);
  const [escalationEnabled, setEscalationEnabled] = useState(false);
  const [escalationIntervalMinutes, setEscalationIntervalMinutes] = useState('15');
  const [shareContactResponses, setShareContactResponses] = useState(true);
  const [isLoadingData, setIsLoadingData] = useState(true);
  const [isUpdating, setIsUpdating] = useState(false);

  useEffect(() => {
    fetchCurrentSettings();
  }, []);

  const fetchCurrentSettings = async () => {
    setIsLoadingData(true);
    try {
      const data = await settingsService.fetchUserSettings();
      setCurrentInterval(data.checkin_interval_hours || 24);
      setQuietHoursEnabled(data.quiet_hours_enabled || false);
      if (data.quiet_hours_start) {
        const parts = data.quiet_hours_start.split(':');
        setStartHours(parts[0] || '23');
        setStartMinutes(parts[1] || '00');
      }
      if (data.quiet_hours_end) {
        const parts = data.quiet_hours_end.split(':');
        setEndHours(parts[0] || '07');
        setEndMinutes(parts[1] || '00');
      }
      setAllowSmsWhatsappCheckin(data.allow_sms_whatsapp_checkin || false);
      setEscalationEnabled(data.escalation_enabled || false);
      setEscalationIntervalMinutes((data.escalation_interval_minutes || 15).toString());
      setShareContactResponses(data.share_contact_responses !== false);
    } catch (e) {
      console.error('Error fetching settings', e);
    } finally {
      setIsLoadingData(false);
    }
  };

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

  const handleToggleQuietHours = async (enabled: boolean) => {
    setQuietHoursEnabled(enabled);
    setIsUpdating(true);
    try {
      const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
      const start = `${startHours.padStart(2, '0')}:${startMinutes.padStart(2, '0')}`;
      const end = `${endHours.padStart(2, '0')}:${endMinutes.padStart(2, '0')}`;
      await settingsService.updateQuietHours(
        enabled,
        start,
        end,
        timezone
      );
      Alert.alert('Éxito', `Modo Sueño ${enabled ? 'activado' : 'desactivado'}.`);
    } catch (error) {
      setQuietHoursEnabled(!enabled); // revert
      Alert.alert('Error', 'No se pudo actualizar el Modo Sueño.');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleToggleSmsWhatsappCheckin = async (enabled: boolean) => {
    setAllowSmsWhatsappCheckin(enabled);
    setIsUpdating(true);
    try {
      await settingsService.updateSmsWhatsappCheckin(enabled);
      Alert.alert('Éxito', `Check-in por SMS/WhatsApp ${enabled ? 'activado' : 'desactivado'}.`);
    } catch (error) {
      setAllowSmsWhatsappCheckin(!enabled); // revert
      Alert.alert('Error', 'No se pudo actualizar la configuración.');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleToggleEscalation = async (enabled: boolean) => {
    setEscalationEnabled(enabled);
    setIsUpdating(true);
    try {
      const minutes = parseInt(escalationIntervalMinutes, 10) || 15;
      await settingsService.updateEscalation(enabled, minutes);
      Alert.alert('Éxito', `Notificación Escalonada ${enabled ? 'activada' : 'desactivada'}.`);
    } catch (error) {
      setEscalationEnabled(!enabled); // revert
      Alert.alert('Error', 'No se pudo actualizar la configuración.');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleTogglePrivacy = async (enabled: boolean) => {
    setShareContactResponses(enabled);
    setIsUpdating(true);
    try {
      await settingsService.updatePrivacy(enabled);
      Alert.alert('Éxito', `Respuestas de contactos ${enabled ? 'compartidas' : 'ocultas'}.`);
    } catch (error) {
      setShareContactResponses(!enabled); // revert
      Alert.alert('Error', 'No se pudo actualizar la privacidad.');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleSaveEscalationInterval = async () => {
    const minutes = parseInt(escalationIntervalMinutes, 10);
    if (isNaN(minutes) || minutes < 1 || minutes > 1440) {
      Alert.alert('Error', 'Por favor ingresa un intervalo válido (entre 1 y 1440 minutos).');
      return;
    }

    setIsUpdating(true);
    try {
      await settingsService.updateEscalation(escalationEnabled, minutes);
      Alert.alert('Éxito', 'Intervalo de escalado guardado.');
    } catch (error) {
      Alert.alert('Error', 'No se pudo guardar la configuración.');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleSaveQuietHours = async () => {
    const hStart = parseInt(startHours, 10);
    const mStart = parseInt(startMinutes, 10);
    const hEnd = parseInt(endHours, 10);
    const mEnd = parseInt(endMinutes, 10);

    if (isNaN(hStart) || hStart < 0 || hStart > 23 ||
        isNaN(mStart) || mStart < 0 || mStart > 59 ||
        isNaN(hEnd) || hEnd < 0 || hEnd > 23 ||
        isNaN(mEnd) || mEnd < 0 || mEnd > 59) {
      Alert.alert('Error', 'Por favor ingresa un horario válido (Horas: 00-23, Minutos: 00-59).');
      return;
    }

    const start = `${startHours.padStart(2, '0')}:${startMinutes.padStart(2, '0')}`;
    const end = `${endHours.padStart(2, '0')}:${endMinutes.padStart(2, '0')}`;

    setIsUpdating(true);
    try {
      const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
      await settingsService.updateQuietHours(
        quietHoursEnabled,
        start,
        end,
        timezone
      );
      Alert.alert('Éxito', 'Horario de descanso guardado.');
    } catch (error) {
      Alert.alert('Error', 'No se pudo guardar la configuración.');
    } finally {
      setIsUpdating(false);
    }
  };

  if (isLoadingData) {
    return (
      <View style={[styles.container, { justifyContent: 'center', alignItems: 'center' }]}>
        <ActivityIndicator size="large" color="#dc2626" />
      </View>
    );
  }

  return (
    <KeyboardAvoidingView
      style={{ flex: 1 }}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      keyboardVerticalOffset={Platform.OS === 'ios' ? 100 : 0}
    >
      <ScrollView ref={scrollViewRef} style={styles.container} contentContainerStyle={styles.contentContainer}>
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
        </View>

        {/* SECCION MODO SUEÑO */}
        <View style={[styles.section, { borderTopWidth: 1, borderTopColor: '#e5e7eb', marginTop: 10, paddingTop: 20 }]}>
          <View style={styles.row}>
            <View style={{ flex: 1, marginRight: 10 }}>
              <Text style={styles.sectionTitle}>Modo Sueño</Text>
              <Text style={styles.description}>Pausa las alertas mientras duermes.</Text>
            </View>
            <Switch
              value={quietHoursEnabled}
              onValueChange={handleToggleQuietHours}
              trackColor={{ false: '#e5e7eb', true: '#fca5a5' }}
              thumbColor={quietHoursEnabled ? '#dc2626' : '#f4f3f4'}
            />
          </View>

          {quietHoursEnabled && (
            <View style={styles.timeInputsContainer}>
              {/* Hora Inicio */}
              <View style={styles.timeInputWrapper}>
                <Text style={styles.timeInputLabel}>Hora Inicio (ej: 23:00)</Text>
                <View style={styles.hourMinuteContainer}>
                  <TextInput
                    style={styles.hourMinuteInput}
                    value={startHours}
                    onChangeText={(text) => {
                      const cleaned = text.replace(/\D/g, '');
                      setStartHours(cleaned);
                    }}
                    onFocus={() => {
                      setTimeout(() => {
                        scrollViewRef.current?.scrollToEnd({ animated: true });
                      }, 350);
                    }}
                    placeholder="23"
                    maxLength={2}
                    keyboardType="number-pad"
                  />
                  <Text style={styles.timeSeparator}>:</Text>
                  <TextInput
                    style={styles.hourMinuteInput}
                    value={startMinutes}
                    onChangeText={(text) => {
                      const cleaned = text.replace(/\D/g, '');
                      setStartMinutes(cleaned);
                    }}
                    onFocus={() => {
                      setTimeout(() => {
                        scrollViewRef.current?.scrollToEnd({ animated: true });
                      }, 350);
                    }}
                    placeholder="00"
                    maxLength={2}
                    keyboardType="number-pad"
                  />
                </View>
              </View>

              {/* Hora Fin */}
              <View style={styles.timeInputWrapper}>
                <Text style={styles.timeInputLabel}>Hora Fin (ej: 07:00)</Text>
                <View style={styles.hourMinuteContainer}>
                  <TextInput
                    style={styles.hourMinuteInput}
                    value={endHours}
                    onChangeText={(text) => {
                      const cleaned = text.replace(/\D/g, '');
                      setEndHours(cleaned);
                    }}
                    onFocus={() => {
                      setTimeout(() => {
                        scrollViewRef.current?.scrollToEnd({ animated: true });
                      }, 350);
                    }}
                    placeholder="07"
                    maxLength={2}
                    keyboardType="number-pad"
                  />
                  <Text style={styles.timeSeparator}>:</Text>
                  <TextInput
                    style={styles.hourMinuteInput}
                    value={endMinutes}
                    onChangeText={(text) => {
                      const cleaned = text.replace(/\D/g, '');
                      setEndMinutes(cleaned);
                    }}
                    onFocus={() => {
                      setTimeout(() => {
                        scrollViewRef.current?.scrollToEnd({ animated: true });
                      }, 350);
                    }}
                    placeholder="00"
                    maxLength={2}
                    keyboardType="number-pad"
                  />
                </View>
              </View>
            </View>
          )}

          {quietHoursEnabled && (
            <TouchableOpacity 
              style={styles.saveButton} 
              onPress={handleSaveQuietHours}
              disabled={isUpdating}
            >
              {isUpdating ? (
                <ActivityIndicator size="small" color="#fff" />
              ) : (
                <Text style={styles.saveButtonText}>Guardar Horario</Text>
              )}
            </TouchableOpacity>
          )}
        </View>

        {/* SECCION CHECK-IN POR SMS/WHATSAPP */}
        <View style={[styles.section, { borderTopWidth: 1, borderTopColor: '#e5e7eb', marginTop: 10, paddingTop: 20 }]}>
          <View style={styles.row}>
            <View style={{ flex: 1, marginRight: 10 }}>
              <Text style={styles.sectionTitle}>Check-in por SMS / WhatsApp</Text>
              <Text style={styles.description}>Permite confirmar tu bienestar respondiendo directamente a los mensajes preventivos.</Text>
            </View>
            <Switch
              value={allowSmsWhatsappCheckin}
              onValueChange={handleToggleSmsWhatsappCheckin}
              trackColor={{ false: '#e5e7eb', true: '#fca5a5' }}
              thumbColor={allowSmsWhatsappCheckin ? '#dc2626' : '#f4f3f4'}
              disabled={isUpdating}
            />
          </View>
        </View>

        {/* SECCION ALERTAS ESCALONADAS */}
        <View style={[styles.section, { borderTopWidth: 1, borderTopColor: '#e5e7eb', marginTop: 10, paddingTop: 20 }]}>
          <View style={styles.row}>
            <View style={{ flex: 1, marginRight: 10 }}>
              <Text style={styles.sectionTitle}>Notificación Escalonada</Text>
              <Text style={styles.description}>
                Notifica secuencialmente a tus contactos de emergencia con un retraso, en lugar de alertar a todos a la vez.
              </Text>
            </View>
            <Switch
              value={escalationEnabled}
              onValueChange={handleToggleEscalation}
              trackColor={{ false: '#e5e7eb', true: '#fca5a5' }}
              thumbColor={escalationEnabled ? '#dc2626' : '#f4f3f4'}
              disabled={isUpdating}
            />
          </View>

          {escalationEnabled && (
            <View style={{ marginTop: 15 }}>
              <Text style={styles.timeInputLabel}>Intervalo de escalado (minutos)</Text>
              <View style={styles.row}>
                <TextInput
                  style={{ backgroundColor: '#fff', borderWidth: 1, borderColor: '#e5e7eb', borderRadius: 12, padding: 12, fontSize: 16, fontWeight: '700', color: '#374151', flex: 1, marginRight: 10 }}
                  value={escalationIntervalMinutes}
                  onChangeText={setEscalationIntervalMinutes}
                  keyboardType="numeric"
                  placeholder="15"
                  onFocus={() => {
                    setTimeout(() => {
                      scrollViewRef.current?.scrollToEnd({ animated: true });
                    }, 350);
                  }}
                />
                <TouchableOpacity 
                  style={[styles.saveButton, { marginTop: 0, paddingVertical: 12, paddingHorizontal: 20 }]} 
                  onPress={handleSaveEscalationInterval}
                  disabled={isUpdating}
                >
                  {isUpdating ? (
                    <ActivityIndicator size="small" color="#fff" />
                  ) : (
                    <Text style={styles.saveButtonText}>Guardar</Text>
                  )}
                </TouchableOpacity>
              </View>
            </View>
          )}
        </View>

        {/* SECCION PRIVACIDAD DE RESPUESTAS */}
        <View style={[styles.section, { borderTopWidth: 1, borderTopColor: '#e5e7eb', marginTop: 10, paddingTop: 20 }]}>
          <View style={styles.row}>
            <View style={{ flex: 1, marginRight: 10 }}>
              <Text style={styles.sectionTitle}>Compartir respuestas de apoyo</Text>
              <Text style={styles.description}>Permite que otros contactos de emergencia vean quiénes ya han leído o van en camino en la pantalla pública.</Text>
            </View>
            <Switch
              value={shareContactResponses}
              onValueChange={handleTogglePrivacy}
              trackColor={{ false: '#e5e7eb', true: '#fca5a5' }}
              thumbColor={shareContactResponses ? '#dc2626' : '#f4f3f4'}
              disabled={isUpdating}
            />
          </View>
        </View>

        <View style={styles.infoCard}>
          <Text style={styles.infoText}>
            Nota: Un intervalo más corto ofrece mayor seguridad pero requiere reportes más frecuentes.
          </Text>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f9fafb' },
  contentContainer: { paddingBottom: 150 },
  header: { padding: 30, alignItems: 'center', backgroundColor: '#fff', borderBottomWidth: 1, borderBottomColor: '#f3f4f6' },
  title: { fontSize: 20, fontWeight: '800', color: '#111827', marginTop: 10 },
  subtitle: { fontSize: 14, color: '#6b7280', marginTop: 4, textAlign: 'center' },
  section: { padding: 20 },
  sectionTitle: { fontSize: 16, fontWeight: '700', color: '#374151', marginBottom: 10 },
  description: { fontSize: 13, color: '#6b7280', marginBottom: 20, lineHeight: 18 },
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
  
  row: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  timeInputsContainer: { flexDirection: 'row', gap: 15, marginTop: 15 },
  timeInputWrapper: { flex: 1 },
  timeInputLabel: { fontSize: 12, fontWeight: '700', color: '#6b7280', marginBottom: 5 },
  timeInput: { backgroundColor: '#fff', borderWidth: 1, borderColor: '#e5e7eb', borderRadius: 12, padding: 12, fontSize: 16, fontWeight: '700', color: '#374151', textAlign: 'center' },
  hourMinuteContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#e5e7eb',
    borderRadius: 12,
    paddingHorizontal: 8,
  },
  hourMinuteInput: {
    flex: 1,
    paddingVertical: 12,
    fontSize: 16,
    fontWeight: '700',
    color: '#374151',
    textAlign: 'center',
  },
  timeSeparator: {
    fontSize: 18,
    fontWeight: '700',
    color: '#9ca3af',
    paddingHorizontal: 2,
  },
  saveButton: { backgroundColor: '#dc2626', padding: 15, borderRadius: 12, alignItems: 'center', marginTop: 15 },
  saveButtonText: { color: '#fff', fontSize: 15, fontWeight: '800' }
});
