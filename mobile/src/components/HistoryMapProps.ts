export interface HistoryMapProps {
  mapRef: any;
  points: Array<{
    id: number;
    latitude: number;
    longitude: number;
    recorded_at: string;
    accuracy?: number;
  }>;
  onMapReady: () => void;
  activePoint: any;
  playbackIndex: number;
  getSpeedText: () => string;
  memberInfo: any;
  style: any;
}
