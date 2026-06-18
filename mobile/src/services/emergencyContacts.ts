import api from './api';

export interface EmergencyContact {
  id?: number;
  name: string;
  phone: string;
  email?: string;
  relationship?: string;
  is_active?: boolean;
}

export const emergencyContactsService = {
  getAll: async () => {
    const response = await api.get<EmergencyContact[]>('/emergency-contacts');
    return response.data;
  },
  
  create: async (contact: EmergencyContact) => {
    const response = await api.post<EmergencyContact>('/emergency-contacts', contact);
    return response.data;
  },
  
  update: async (id: number, contact: Partial<EmergencyContact>) => {
    const response = await api.put<EmergencyContact>(`/emergency-contacts/${id}`, contact);
    return response.data;
  },
  
  delete: async (id: number) => {
    await api.delete(`/emergency-contacts/${id}`);
  },

  reorder: async (ids: number[]) => {
    const response = await api.post('/emergency-contacts/reorder', { ids });
    return response.data;
  }
};
