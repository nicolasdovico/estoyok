@props(['url'])
@php
    $logoPath = public_path('images/logo.png');
    $logoSrc = file_exists($logoPath)
        ? 'data:image/png;base64,' . base64_encode(file_get_contents($logoPath))
        : 'https://api.estoyok24.com/images/logo.png';
@endphp
<tr>
<td class="header">
<a href="{{ $url }}" style="display: inline-block;">
<img src="{{ $logoSrc }}" class="logo-app" alt="{{ config('app.name', 'Estoy Ok') }}" style="max-height: 78px; width: auto; display: block; margin: 0 auto;">
</a>
</td>
</tr>

