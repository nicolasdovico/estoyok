<x-mail::message>
# 🎉 ¡Nuevo Usuario Registrado en Estoy Ok!

Se ha registrado una nueva cuenta en la plataforma:

<x-mail::panel>
**Nombre:** {{ $user->name }}  
**Correo Electrónico:** {{ $user->email }}  
**Teléfono:** {{ $user->phone ?? 'No especificado' }}  
**Fecha y Hora:** {{ $user->created_at ? $user->created_at->timezone(config('app.timezone', 'America/Argentina/Buenos_Aires'))->format('d/m/Y H:i:s') : now()->format('d/m/Y H:i:s') }}  
**ID de Usuario:** #{{ $user->id }}
</x-mail::panel>

<x-mail::table>
| Métrica | Valor |
|:--------|:------|
| Total de Usuarios Registrados | **{{ $totalUsers }}** |
</x-mail::table>

Este es un aviso automático para la administración de **Estoy Ok**.

Saludos,<br>
{{ config('app.name') }}
</x-mail::message>
