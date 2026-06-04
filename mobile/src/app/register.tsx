import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, ActivityIndicator, Alert, ScrollView } from 'react-native';
import { useRouter, Link } from 'expo-router';
import { useAuth } from '@/context/AuthContext';
import api from '@/services/api';

export default function RegisterScreen() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirmation, setPasswordConfirmation] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const router = useRouter();

  const handleRegister = async () => {
    if (!name || !email || !password || !passwordConfirmation) {
      Alert.alert('Error', 'Por favor completa todos los campos');
      return;
    }

    if (password !== passwordConfirmation) {
      Alert.alert('Error', 'Las contraseñas no coinciden');
      return;
    }

    setLoading(true);
    try {
      await api.post('/register', { 
        name, 
        email, 
        password, 
        password_confirmation: passwordConfirmation 
      });
      router.push(`/verify-email?email=${encodeURIComponent(email)}`);
    } catch (error: any) {
      const message = error.response?.data?.message || 'Error al crear cuenta';
      Alert.alert('Error', message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.header}>
        <Text style={styles.logo}>ESTOY OK</Text>
        <Text style={styles.subtitle}>Crea tu cuenta de seguridad</Text>
      </View>

      <View style={styles.form}>
        <Text style={styles.label}>Nombre Completo</Text>
        <TextInput
          style={styles.input}
          placeholder="Juan Pérez"
          value={name}
          onChangeText={setName}
        />

        <Text style={styles.label}>Email</Text>
        <TextInput
          style={styles.input}
          placeholder="tu@ejemplo.com"
          value={email}
          onChangeText={setEmail}
          autoCapitalize="none"
          keyboardType="email-address"
        />

        <Text style={styles.label}>Contraseña</Text>
        <TextInput
          style={styles.input}
          placeholder="••••••••"
          value={password}
          onChangeText={setPassword}
          secureTextEntry
        />

        <Text style={styles.label}>Confirmar Contraseña</Text>
        <TextInput
          style={styles.input}
          placeholder="••••••••"
          value={passwordConfirmation}
          onChangeText={setPasswordConfirmation}
          secureTextEntry
        />

        <TouchableOpacity 
          style={[styles.button, loading && styles.buttonDisabled]} 
          onPress={handleRegister}
          disabled={loading}
        >
          {loading ? <ActivityIndicator color="#fff" /> : <Text style={styles.buttonText}>Registrarse</Text>}
        </TouchableOpacity>

        <View style={styles.footer}>
          <Text style={styles.footerText}>¿Ya tienes cuenta? </Text>
          <Link href="/login" asChild>
            <TouchableOpacity>
              <Text style={styles.link}>Inicia sesión</Text>
            </TouchableOpacity>
          </Link>
        </View>
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
    fontSize: 14,
    color: '#666',
  },
  form: {
    width: '100%',
  },
  label: {
    fontSize: 14,
    fontWeight: '600',
    color: '#374151',
    marginBottom: 8,
  },
  input: {
    borderWidth: 1,
    borderColor: '#d1d5db',
    borderRadius: 12,
    padding: 15,
    fontSize: 16,
    marginBottom: 15,
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
  footer: {
    flexDirection: 'row',
    justifyContent: 'center',
    marginTop: 30,
    marginBottom: 40,
  },
  footerText: {
    color: '#6b7280',
  },
  link: {
    color: '#dc2626',
    fontWeight: '700',
  },
});
