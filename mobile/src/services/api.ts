import axios from 'axios';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { DeviceEventEmitter } from 'react-native';

// REEMPLAZAR con tu IP local para dispositivos reales
//const BASE_URL = 'http://10.20.30.16:8000/api';
const BASE_URL = 'https://backend-api-production-aec1.up.railway.app/api';

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

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response && error.response.status === 401) {
      DeviceEventEmitter.emit('force_logout');
    }
    return Promise.reject(error);
  }
);

export default api;
