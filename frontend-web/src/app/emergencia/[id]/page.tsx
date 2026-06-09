import EmergencyClientPage from './EmergencyClientPage';

export default async function EmergencyPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <EmergencyClientPage id={id} />;
}
