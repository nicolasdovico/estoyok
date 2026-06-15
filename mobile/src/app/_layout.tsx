import { DarkTheme, DefaultTheme, ThemeProvider } from '@react-navigation/native';
import { Stack } from 'expo-router';
import { useColorScheme, View, ActivityIndicator } from 'react-native';
import { AuthProvider, useAuth } from '@/context/AuthContext';
import { useEffect } from 'react';
import { useRouter, useSegments } from 'expo-router';
import '@/services/locationTask'; // Registro de tarea de fondo

function RootLayoutNav() {
  const { user, isLoading } = useAuth();
  const segments = useSegments();
  const router = useRouter();
  const colorScheme = useColorScheme();

  useEffect(() => {
    if (isLoading) return;

    const isAuthScreen = segments[0] === 'login' || segments[0] === 'register' || segments[0] === 'verify-email';

    if (!user && !isAuthScreen) {
      router.replace('/login');
    } else if (user && isAuthScreen) {
      router.replace('/');
    }
  }, [user, isLoading, segments]);

  if (isLoading) {
    return (
      <View style={{ flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: colorScheme === 'dark' ? '#000' : '#fff' }}>
        <ActivityIndicator size="large" color="#2563eb" />
      </View>
    );
  }

  return (
    <ThemeProvider value={colorScheme === 'dark' ? DarkTheme : DefaultTheme}>
      <Stack>
        <Stack.Screen name="index" options={{ title: 'Estoy Ok', headerShown: true }} />
        <Stack.Screen name="login" options={{ title: 'Iniciar Sesión', headerShown: false }} />
        <Stack.Screen name="register" options={{ title: 'Crear Cuenta', headerShown: false }} />
        <Stack.Screen name="verify-email" options={{ title: 'Verificar Email', headerShown: false }} />
        <Stack.Screen name="contacts" options={{ title: 'Contactos', headerShown: true }} />
        <Stack.Screen name="settings" options={{ title: 'Configuración', headerShown: true }} />
      </Stack>
    </ThemeProvider>
  );
}

export default function RootLayout() {
  return (
    <AuthProvider>
      <RootLayoutNav />
    </AuthProvider>
  );
}
