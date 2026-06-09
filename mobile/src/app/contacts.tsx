import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, Modal, TextInput, Alert, ActivityIndicator } from 'react-native';
import { emergencyContactsService, EmergencyContact } from '@/services/emergencyContacts';
import { Plus, Trash2, Edit2, Shield, Phone, Mail, User } from 'lucide-react-native';

export default function ContactsScreen() {
  const [contacts, setContacts] = useState<EmergencyContact[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingContact, setEditingContact] = useState<EmergencyContact | null>(null);
  
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    email: '',
    relationship: '',
  });

  const fetchContacts = async () => {
    try {
      const data = await emergencyContactsService.getAll();
      setContacts(data);
    } catch (error) {
      Alert.alert('Error', 'No pudimos cargar tus contactos.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchContacts();
  }, []);

  const handleSave = async () => {
    if (!formData.name || !formData.phone) {
      Alert.alert('Error', 'Nombre y teléfono son obligatorios.');
      return;
    }

    try {
      if (editingContact) {
        await emergencyContactsService.update(editingContact.id!, formData);
      } else {
        await emergencyContactsService.create(formData);
      }
      setModalVisible(false);
      setEditingContact(null);
      setFormData({ name: '', phone: '', email: '', relationship: '' });
      fetchContacts();
    } catch (error) {
      Alert.alert('Error', 'No pudimos guardar el contacto.');
    }
  };

  const handleDelete = (id: number) => {
    Alert.alert(
      'Eliminar Contacto',
      '¿Estás seguro de que quieres eliminar este contacto?',
      [
        { text: 'Cancelar', style: 'cancel' },
        { 
          text: 'Eliminar', 
          style: 'destructive',
          onPress: async () => {
            try {
              await emergencyContactsService.delete(id);
              fetchContacts();
            } catch (error) {
              Alert.alert('Error', 'No pudimos eliminar el contacto.');
            }
          }
        }
      ]
    );
  };

  const openEditModal = (contact: EmergencyContact) => {
    setEditingContact(contact);
    setFormData({
      name: contact.name,
      phone: contact.phone,
      email: contact.email || '',
      relationship: contact.relationship || '',
    });
    setModalVisible(true);
  };

  const renderItem = ({ item }: { item: EmergencyContact }) => (
    <View style={styles.contactCard}>
      <View style={styles.contactInfo}>
        <Text style={styles.contactName}>{item.name}</Text>
        <Text style={styles.contactRelation}>{item.relationship || 'Sin relación especificada'}</Text>
        <View style={styles.contactDetail}>
          <Phone size={14} color="#6b7280" />
          <Text style={styles.detailText}>{item.phone}</Text>
        </View>
        {item.email && (
          <View style={styles.contactDetail}>
            <Mail size={14} color="#6b7280" />
            <Text style={styles.detailText}>{item.email}</Text>
          </View>
        )}
      </View>
      <View style={styles.actions}>
        <TouchableOpacity onPress={() => openEditModal(item)} style={styles.actionButton}>
          <Edit2 size={20} color="#3b82f6" />
        </TouchableOpacity>
        <TouchableOpacity onPress={() => handleDelete(item.id!)} style={styles.actionButton}>
          <Trash2 size={20} color="#ef4444" />
        </TouchableOpacity>
      </View>
    </View>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Shield size={32} color="#dc2626" />
        <Text style={styles.title}>Contactos de Emergencia</Text>
        <Text style={styles.subtitle}>A quienes avisaremos si algo sucede.</Text>
      </View>

      {isLoading ? (
        <ActivityIndicator size="large" color="#dc2626" style={{ marginTop: 50 }} />
      ) : (
        <FlatList
          data={contacts}
          keyExtractor={(item) => item.id!.toString()}
          renderItem={renderItem}
          contentContainerStyle={styles.list}
          ListEmptyComponent={
            <View style={styles.empty}>
              <User size={48} color="#d1d5db" />
              <Text style={styles.emptyText}>No tienes contactos aún.</Text>
            </View>
          }
        />
      )}

      <TouchableOpacity 
        style={styles.fab} 
        onPress={() => {
          setEditingContact(null);
          setFormData({ name: '', phone: '', email: '', relationship: '' });
          setModalVisible(true);
        }}
      >
        <Plus size={24} color="#fff" />
      </TouchableOpacity>

      <Modal visible={modalVisible} animationType="slide" transparent>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>
              {editingContact ? 'Editar Contacto' : 'Nuevo Contacto'}
            </Text>
            
            <TextInput
              style={styles.input}
              placeholder="Nombre"
              value={formData.name}
              onChangeText={(text) => setFormData({ ...formData, name: text })}
            />
            <TextInput
              style={styles.input}
              placeholder="Teléfono (ej: +54...)"
              keyboardType="phone-pad"
              value={formData.phone}
              onChangeText={(text) => setFormData({ ...formData, phone: text })}
            />
            <TextInput
              style={styles.input}
              placeholder="Email (opcional)"
              keyboardType="email-address"
              autoCapitalize="none"
              value={formData.email}
              onChangeText={(text) => setFormData({ ...formData, email: text })}
            />
            <TextInput
              style={styles.input}
              placeholder="Relación (ej: Madre, Amigo)"
              value={formData.relationship}
              onChangeText={(text) => setFormData({ ...formData, relationship: text })}
            />

            <View style={styles.modalActions}>
              <TouchableOpacity 
                style={[styles.modalButton, styles.cancelButton]} 
                onPress={() => setModalVisible(false)}
              >
                <Text style={styles.cancelButtonText}>Cancelar</Text>
              </TouchableOpacity>
              <TouchableOpacity 
                style={[styles.modalButton, styles.saveButton]} 
                onPress={handleSave}
              >
                <Text style={styles.saveButtonText}>Guardar</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f9fafb' },
  header: { padding: 20, alignItems: 'center', backgroundColor: '#fff', borderBottomWidth: 1, borderBottomColor: '#f3f4f6' },
  title: { fontSize: 20, fontWeight: '800', color: '#111827', marginTop: 10 },
  subtitle: { fontSize: 14, color: '#6b7280', marginTop: 4 },
  list: { padding: 15, paddingBottom: 100 },
  contactCard: { 
    flexDirection: 'row', 
    backgroundColor: '#fff', 
    padding: 15, 
    borderRadius: 15, 
    marginBottom: 12,
    shadowColor: '#000', shadowOffset: { width: 0, height: 1 }, shadowOpacity: 0.05, shadowRadius: 2, elevation: 2
  },
  contactInfo: { flex: 1 },
  contactName: { fontSize: 16, fontWeight: '700', color: '#111827' },
  contactRelation: { fontSize: 12, color: '#dc2626', fontWeight: '600', marginBottom: 8 },
  contactDetail: { flexDirection: 'row', alignItems: 'center', gap: 6, marginTop: 2 },
  detailText: { fontSize: 14, color: '#4b5563' },
  actions: { justifyContent: 'space-around' },
  actionButton: { padding: 8 },
  fab: { 
    position: 'absolute', bottom: 30, right: 30, 
    width: 56, height: 56, borderRadius: 28, 
    backgroundColor: '#dc2626', justifyContent: 'center', alignItems: 'center',
    elevation: 4, shadowColor: '#000', shadowOffset: { width: 0, height: 2 }, shadowOpacity: 0.25, shadowRadius: 4
  },
  empty: { alignItems: 'center', marginTop: 100 },
  emptyText: { color: '#9ca3af', marginTop: 10 },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.5)', justifyContent: 'flex-end' },
  modalContent: { backgroundColor: '#fff', borderTopLeftRadius: 30, borderTopRightRadius: 30, padding: 30 },
  modalTitle: { fontSize: 20, fontWeight: '800', marginBottom: 20, textAlign: 'center' },
  input: { backgroundColor: '#f3f4f6', padding: 15, borderRadius: 12, marginBottom: 12, fontSize: 16 },
  modalActions: { flexDirection: 'row', gap: 12, marginTop: 10 },
  modalButton: { flex: 1, padding: 15, borderRadius: 12, alignItems: 'center' },
  cancelButton: { backgroundColor: '#f3f4f6' },
  saveButton: { backgroundColor: '#dc2626' },
  cancelButtonText: { color: '#4b5563', fontWeight: '700' },
  saveButtonText: { color: '#fff', fontWeight: '700' },
});
