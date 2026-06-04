import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';

// REEMPLAZAR con tu IP local para dispositivos reales
const BASE_URL = 'http://10.20.30.16:8000/api';

const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

api.interceptors.request.use(async (config) => {
  const token = await AsyncStorage.getItem('auth_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
