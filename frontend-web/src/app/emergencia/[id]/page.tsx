import type { Metadata } from 'next';
import EmergencyClientPage from './EmergencyClientPage';

export const metadata: Metadata = {
  title: 'Alerta de Emergencia Activa - Estoy Ok',
  description: 'Visualización de contingencia y mapa de rescate en tiempo real.',
  robots: {
    index: false,
    follow: false,
  },
};

export default async function EmergencyPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <EmergencyClientPage id={id} />;
}
