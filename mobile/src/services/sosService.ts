import api from './api';

export const startSos = async () => {
  const response = await api.post('/emergency-alerts/sos');
  return response.data;
};

export const uploadSosAudio = async (alertId: string, audioUri: string) => {
  const formData = new FormData();
  const filename = audioUri.split('/').pop() || 'sos_recording.m4a';
  const match = /\.(\w+)$/.exec(filename);
  const type = match ? `audio/${match[1]}` : `audio/m4a`;

  formData.append('audio', {
    uri: audioUri,
    name: filename,
    type,
  } as any);

  const response = await api.post(`/emergency-alerts/${alertId}/audio`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return response.data;
};
