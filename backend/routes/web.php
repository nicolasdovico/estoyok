<?php

use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return response()->json([
        'service' => 'Estoy Ok API',
        'status' => 'operational',
    ]);
});

Route::get('/descargar', function (\Illuminate\Http\Request $request) {
    $playStoreUrl = 'https://play.google.com/store/apps/details?id=com.estoyok.app';
    $androidIntentUrl = 'intent://details?id=com.estoyok.app#Intent;scheme=market;package=com.android.vending;S.browser_fallback_url=https%3A%2F%2Fplay.google.com%2Fstore%2Fapps%2Fdetails%3Fid%3Dcom.estoyok.app;end';

    $html = <<<HTML
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Descargar Estoy Ok - Localizador Familiar GPS</title>
    <meta name="description" content="Descargá gratis Estoy Ok en Google Play Store. Localizador familiar GPS en tiempo real, Zonas Seguras, check-in diario de bienestar y alertas SOS por WhatsApp.">
    <meta property="og:title" content="Descargar Estoy Ok - Localizador Familiar GPS">
    <meta property="og:description" content="Protegé a tu familia en tiempo real: Zonas Seguras, botón Estoy OK y alertas SOS con audio directo a WhatsApp.">
    <meta property="og:url" content="https://estoyok24.com/descargar">
    <meta property="og:site_name" content="Estoy Ok">
    <meta property="og:type" content="website">
    <script>
        (function() {
            var ua = navigator.userAgent || "";
            if (/facebookexternalhit|Facebot|meta-externalagent|Twitterbot|Googlebot|bingbot|crawler|spider/i.test(ua)) return;
            if (/Android/i.test(ua)) {
                window.location.replace("{$androidIntentUrl}");
                setTimeout(function() { window.location.replace("{$playStoreUrl}"); }, 400);
            } else {
                window.location.replace("{$playStoreUrl}");
            }
        })();
    </script>
</head>
<body style="background:#09090b;color:#fff;font-family:sans-serif;display:flex;align-items:center;justify-content:center;min-height:100vh;margin:0;text-align:center;">
    <div style="padding:24px;">
        <h1 style="font-size:20px;margin-bottom:12px;">Abriendo Google Play Store...</h1>
        <a href="{$playStoreUrl}" style="display:inline-block;padding:14px 28px;background:#10b981;color:#09090b;font-weight:bold;border-radius:12px;text-decoration:none;">Descargar en Google Play</a>
    </div>
</body>
</html>
HTML;

    return response($html, 200)->header('Content-Type', 'text/html; charset=UTF-8');
});

