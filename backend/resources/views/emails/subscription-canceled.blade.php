<x-mail::message>
# ⚠️ Baja de Suscripción en Estoy Ok

Se ha registrado la baja de una suscripción o finalización de período de prueba en la plataforma:

<x-mail::panel>
**Nombre:** {{ $user->name }}  
**Correo Electrónico:** {{ $user->email }}  
**Teléfono:** {{ $user->phone ?? 'No especificado' }}  
**Motivo:** {{ $reason }}  
**Proveedor:** {{ ucfirst($user->subscription_provider ?? 'Google Play') }}  
**Fecha y Hora:** {{ now()->timezone(config('app.timezone', 'America/Argentina/Buenos_Aires'))->format('d/m/Y H:i:s') }}  
**ID de Usuario:** #{{ $user->id }}
</x-mail::panel>

<x-mail::table>
| Métrica | Valor |
|:--------|:------|
| Usuarios Premium Restantes | **{{ $activePremiumUsers }}** |
</x-mail::table>

Este es un aviso automático para la administración de **Estoy Ok**.

Saludos,<br>
{{ config('app.name') }}
</x-mail::message>
