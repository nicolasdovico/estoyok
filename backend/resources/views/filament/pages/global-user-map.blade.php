<x-filament-panels::page>
    <div
        x-data="{
            usersData: @js($locations),
            map: null,
            initMap() {
                if (typeof L === 'undefined') {
                    setTimeout(() => this.initMap(), 150);
                    return;
                }
                if (this.map) return;

                this.map = L.map(this.$refs.mapContainer, {
                    center: [-34.6037, -58.3816],
                    zoom: 4,
                    maxZoom: 18,
                    minZoom: 2,
                });

                L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
                    attribution: '&copy; CARTO &copy; OpenStreetMap',
                    subdomains: 'abcd',
                    maxZoom: 20
                }).addTo(this.map);

                const cluster = L.markerClusterGroup({
                    showCoverageOnHover: false,
                    maxClusterRadius: 40,
                    spiderfyOnMaxZoom: true,
                });

                const latLngs = [];

                this.usersData.forEach(user => {
                    if (user.latitude && user.longitude) {
                        const pos = [user.latitude, user.longitude];
                        latLngs.push(pos);

                        const initials = user.name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase() || 'OK';
                        const markerClass = user.is_offline ? 'custom-user-marker offline' : 'custom-user-marker';

                        const icon = L.divIcon({
                            className: 'custom-icon-wrapper',
                            html: `<div class='${markerClass}' style='width: 32px; height: 32px;'>${initials}</div>`,
                            iconSize: [32, 32],
                            iconAnchor: [16, 16],
                            popupAnchor: [0, -16]
                        });

                        const popup = `
                            <div style='font-family: inherit; font-size: 12px; color: #1E293B; min-width: 180px;'>
                                <div style='font-weight: 700; font-size: 14px; margin-bottom: 2px; color: #0F172A;'>${user.name}</div>
                                <div style='color: #64748B; font-size: 11px; margin-bottom: 8px;'>${user.email}</div>
                                <hr style='border: 0; border-top: 1px solid #E2E8F0; margin: 6px 0;' />
                                <div style='display: flex; justify-content: space-between; margin-bottom: 4px;'>
                                    <span style='color: #64748B;'>Estado:</span>
                                    <span style='font-weight: 600; color: ${user.is_offline ? '#64748B' : '#10B981'};'>
                                        ${user.is_offline ? '⚪ Inactivo' : '🟢 En Línea'}
                                    </span>
                                </div>
                                ${user.battery_level !== null ? `
                                <div style='display: flex; justify-content: space-between; margin-bottom: 4px;'>
                                    <span style='color: #64748B;'>Batería:</span>
                                    <span style='font-weight: 600;'>🔋 ${Math.round(user.battery_level)}%</span>
                                </div>` : ''}
                                <div style='display: flex; justify-content: space-between; margin-bottom: 4px;'>
                                    <span style='color: #64748B;'>Último reporte:</span>
                                    <span style='font-weight: 600;'>${user.last_seen_human}</span>
                                </div>
                                <div style='margin-top: 8px; font-size: 10px; color: #94A3B8; font-family: monospace;'>
                                    Lat: ${Number(user.latitude).toFixed(4)}, Lng: ${Number(user.longitude).toFixed(4)}
                                </div>
                            </div>
                        `;

                        const marker = L.marker(pos, { icon }).bindPopup(popup);
                        cluster.addLayer(marker);
                    }
                });

                this.map.addLayer(cluster);

                if (latLngs.length > 0) {
                    this.map.fitBounds(latLngs, { padding: [50, 50], maxZoom: 14 });
                }
            },
            fitBounds() {
                if (!this.map || this.usersData.length === 0) return;
                const bounds = this.usersData.filter(u => u.latitude && u.longitude).map(u => [u.latitude, u.longitude]);
                if (bounds.length > 0) {
                    this.map.fitBounds(bounds, { padding: [50, 50], maxZoom: 14 });
                }
            },
            focusUser(lat, lng) {
                if (!this.map) return;
                this.map.flyTo([lat, lng], 15, { animate: true, duration: 1.2 });
                this.$refs.mapContainer.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }"
        x-init="initMap()"
        class="space-y-6"
    >
        {{-- Métricas de Magnitud --}}
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5 shadow-sm">
                <div class="flex items-center justify-between">
                    <div>
                        <p class="text-xs font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400">Total Usuarios</p>
                        <p class="text-2xl font-bold text-gray-900 dark:text-white mt-1">{{ $totalUsers }}</p>
                    </div>
                    <div class="p-3 bg-amber-50 dark:bg-amber-950/40 rounded-xl text-amber-600 dark:text-amber-400">
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path>
                        </svg>
                    </div>
                </div>
            </div>

            <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5 shadow-sm">
                <div class="flex items-center justify-between">
                    <div>
                        <p class="text-xs font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400">Con Coordenadas GPS</p>
                        <p class="text-2xl font-bold text-emerald-600 dark:text-emerald-400 mt-1">{{ $usersWithLocation }}</p>
                    </div>
                    <div class="p-3 bg-emerald-50 dark:bg-emerald-950/40 rounded-xl text-emerald-600 dark:text-emerald-400">
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
                        </svg>
                    </div>
                </div>
            </div>

            <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-xl p-5 shadow-sm">
                <div class="flex items-center justify-between">
                    <div>
                        <p class="text-xs font-semibold uppercase tracking-wider text-gray-500 dark:text-gray-400">En Línea (&lt; 15 min)</p>
                        <p class="text-2xl font-bold text-blue-600 dark:text-blue-400 mt-1">{{ $activeNow }}</p>
                    </div>
                    <div class="p-3 bg-blue-50 dark:bg-blue-950/40 rounded-xl text-blue-600 dark:text-blue-400">
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
                        </svg>
                    </div>
                </div>
            </div>
        </div>

        {{-- Contenedor del Mapa --}}
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-2xl p-4 shadow-sm relative overflow-hidden">
            <div class="flex flex-wrap items-center justify-between gap-4 mb-4">
                <div class="flex items-center gap-2">
                    <span class="inline-block w-3 h-3 rounded-full bg-emerald-500 animate-pulse"></span>
                    <h2 class="text-base font-semibold text-gray-900 dark:text-white">Ubicaciones Globales en Tiempo Real</h2>
                </div>

                <div class="flex items-center gap-3">
                    <button type="button" @click="fitBounds()" class="px-3 py-1.5 text-xs font-medium bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-200 rounded-lg transition-colors flex items-center gap-1.5">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 8V4m0 0h4M4 4l5 5m11-1V4m0 0h-4m4 0l-5 5M4 16v4m0 0h4m-4 0l5-5m11 5l-5-5m5 5v-4m0 4h-4"></path>
                        </svg>
                        Ajustar Vista Global
                    </button>
                    <button type="button" onclick="location.reload()" class="px-3 py-1.5 text-xs font-medium bg-amber-500 hover:bg-amber-600 text-white rounded-lg transition-colors flex items-center gap-1.5">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
                        </svg>
                        Refrescar
                    </button>
                </div>
            </div>

            {{-- Leaflet Map DOM Element --}}
            <div x-ref="mapContainer" wire:ignore class="w-full h-[620px] rounded-xl z-0 border border-gray-100 dark:border-gray-800"></div>
        </div>

        {{-- Tabla de Últimas Conexiones --}}
        <div class="bg-white dark:bg-gray-900 border border-gray-200 dark:border-gray-800 rounded-2xl p-5 shadow-sm overflow-hidden">
            <h3 class="text-sm font-semibold text-gray-900 dark:text-white mb-4">Detalle de Dispositivos Conectados</h3>
            
            <div class="overflow-x-auto">
                <table class="w-full text-left text-sm text-gray-600 dark:text-gray-300">
                    <thead class="text-xs uppercase bg-gray-50 dark:bg-gray-800/60 text-gray-500 dark:text-gray-400">
                        <tr>
                            <th class="px-4 py-3 rounded-l-lg">Usuario</th>
                            <th class="px-4 py-3">Estado</th>
                            <th class="px-4 py-3">Batería</th>
                            <th class="px-4 py-3">Coordenadas</th>
                            <th class="px-4 py-3">Último Reporte</th>
                            <th class="px-4 py-3 rounded-r-lg text-right">Acción</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
                        @forelse($locations as $item)
                            <tr class="hover:bg-gray-50/50 dark:hover:bg-gray-800/30 transition-colors">
                                <td class="px-4 py-3">
                                    <div class="font-medium text-gray-900 dark:text-white">{{ $item['name'] }}</div>
                                    <div class="text-xs text-gray-400">{{ $item['email'] }}</div>
                                </td>
                                <td class="px-4 py-3">
                                    @if(!$item['is_offline'])
                                        <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300">
                                            <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                                            En Línea
                                        </span>
                                    @else
                                        <span class="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400">
                                            <span class="w-1.5 h-1.5 rounded-full bg-gray-400"></span>
                                            Inactivo
                                        </span>
                                    @endif
                                </td>
                                <td class="px-4 py-3">
                                    @if($item['battery_level'] !== null)
                                        <span class="font-mono text-xs">{{ round($item['battery_level']) }}%</span>
                                    @else
                                        <span class="text-gray-400 text-xs">N/A</span>
                                    @endif
                                </td>
                                <td class="px-4 py-3 font-mono text-xs">
                                    {{ number_format($item['latitude'], 4) }}, {{ number_format($item['longitude'], 4) }}
                                </td>
                                <td class="px-4 py-3 text-xs">
                                    {{ $item['last_seen_human'] }}
                                </td>
                                <td class="px-4 py-3 text-right">
                                    <button type="button" @click="focusUser({{ $item['latitude'] }}, {{ $item['longitude'] }})" class="text-xs font-medium text-amber-600 dark:text-amber-400 hover:underline">
                                        Ubicar 🎯
                                    </button>
                                </td>
                            </tr>
                        @empty
                            <tr>
                                <td colspan="6" class="px-4 py-6 text-center text-gray-400">
                                    No hay ubicaciones registradas aún en el sistema.
                                </td>
                            </tr>
                        @endforelse
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    @assets
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="" />
    <link rel="stylesheet" href="https://unpkg.com/leaflet.markercluster@1.5.3/dist/MarkerCluster.css" />
    <link rel="stylesheet" href="https://unpkg.com/leaflet.markercluster@1.5.3/dist/MarkerCluster.Default.css" />
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
    <script src="https://unpkg.com/leaflet.markercluster@1.5.3/dist/leaflet.markercluster.js"></script>
    <style>
        .custom-user-marker {
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            background: #10B981;
            color: #0F172A;
            font-weight: bold;
            font-size: 11px;
            box-shadow: 0 0 12px rgba(16, 185, 129, 0.6), 0 2px 4px rgba(0,0,0,0.3);
            border: 2px solid #FFFFFF;
            transition: transform 0.2s ease;
        }
        .custom-user-marker.offline {
            background: #64748B;
            box-shadow: 0 2px 4px rgba(0,0,0,0.3);
        }
        .custom-user-marker:hover {
            transform: scale(1.15);
            z-index: 1000 !important;
        }
    </style>
    @endassets
</x-filament-panels::page>
