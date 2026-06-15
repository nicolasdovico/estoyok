import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, Alert, ScrollView } from 'react-native';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { useAuth } from '@/context/AuthContext';
import api from '@/services/api';

export default function VerifyEmailScreen() {
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const { login } = useAuth();
  const router = useRouter();
  const { email } = useLocalSearchParams<{ email: string }>();

  const handleVerify = async () => {
    if (!code || code.length !== 6) {
      Alert.alert('Error', 'Por favor ingresa el código de 6 dígitos');
      return;
    }

    setLoading(true);
    try {
      const response = await api.post('/verify-email', { 
        email, 
        code 
      });
      await login(response.data.token, response.data.user);
      router.replace('/');
    } catch (error: any) {
      const message = error.response?.data?.message || 'Código inválido o expirado';
      Alert.alert('Error', message);
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setResending(true);
    try {
      await api.post('/resend-otp', { email });
      Alert.alert('Éxito', 'Se ha enviado un nuevo código a tu email');
    } catch (error: any) {
      const message = error.response?.data?.message || 'Error al reenviar código';
      Alert.alert('Error', message);
    } finally {
      setResending(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.header}>
        <Text style={styles.logo}>ESTOY OK</Text>
        <Text style={styles.subtitle}>Verifica tu email</Text>
      </View>

      <View style={styles.form}>
        <Text style={styles.info}>
          Hemos enviado un código de 6 dígitos a:
          {"\n"}
          <Text style={styles.emailText}>{email}</Text>
        </Text>

        <Text style={styles.label}>Código de Verificación</Text>
        <TextInput
          style={[styles.input, { letterSpacing: 10, textAlign: 'center' }]}
          placeholder="123456"
          value={code}
          onChangeText={setCode}
          keyboardType="number-pad"
          maxLength={6}
        />

        <TouchableOpacity 
          style={[styles.button, loading && styles.buttonDisabled]} 
          onPress={handleVerify}
          disabled={loading}
        >
          {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Verificar</Text>}
        </TouchableOpacity>

        <TouchableOpacity 
          style={styles.resendButton} 
          onPress={handleResend}
          disabled={resending}
        >
          {resending ? (
            <ActivityIndicator color="#dc2626" size="small" />
          ) : (
            <Text style={styles.resendText}>¿No recibiste el código? Reenviar</Text>
          )}
        </TouchableOpacity>

        <TouchableOpacity 
          style={styles.backButton} 
          onPress={() => router.back()}
        >
          <Text style={styles.backText}>Volver al registro</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flexGrow: 1,
    backgroundColor: '#fff',
    padding: 20,
    justifyContent: 'center',
  },
  header: {
    alignItems: 'center',
    marginBottom: 30,
    marginTop: 40,
  },
  logo: {
    fontSize: 32,
    fontWeight: '900',
    color: '#dc2626',
    letterSpacing: -2,
  },
  subtitle: {
    fontSize: 18,
    color: '#4b5563',
    fontWeight: '600',
    marginTop: 5,
  },
  form: {
    width: '100%',
  },
  info: {
    fontSize: 15,
    color: '#6b7280',
    textAlign: 'center',
    marginBottom: 30,
    lineHeight: 22,
  },
  emailText: {
    fontWeight: '700',
    color: '#374151',
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
    marginBottom: 8,
    textAlign: 'center',
  },
  input: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 12,
    padding: 15,
    fontSize: 24,
    marginBottom: 20,
    fontWeight: 'bold',
  },
  button: {
    backgroundColor: '#dc2626',
    borderRadius: 12,
    padding: 18,
    alignItems: 'center',
    marginTop: 10,
  },
  buttonDisabled: {
    opacity: 0.7,
  },
  buttonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '700',
  },
  resendButton: {
    marginTop: 25,
    padding: 10,
    alignItems: 'center',
  },
  resendText: {
    color: '#dc2626',
    fontWeight: '600',
    fontSize: 14,
  },
  backButton: {
    marginTop: 10,
    padding: 10,
    alignItems: 'center',
  },
  backText: {
    color: '#6b7280',
    fontSize: 14,
  },
});
