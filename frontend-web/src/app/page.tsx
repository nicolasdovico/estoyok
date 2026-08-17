'use client';

import { useState, useEffect } from 'react';
import Link from "next/link";
// import Dashboard from '@/components/Dashboard';

export default function Home() {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);
  const [openFaqIndex, setOpenFaqIndex] = useState<number | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('auth_token');
    const timer = setTimeout(() => {
      setIsAuthenticated(!!token);
    }, 0);
    return () => clearTimeout(timer);
  }, []);

  // Mientras verificamos la autenticación, mostramos un estado de carga simple
  if (isAuthenticated === null) {
    return (
      <div className="min-h-screen bg-neutral-950 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-red-500"></div>
      </div>
    );
  }

  // Si está autenticado, mostrar el Dashboard (Desactivado temporalmente para enfoque móvil)
  // if (isAuthenticated) {
  //   return <Dashboard />;
  // }

  const jsonLdSoftware = {
    '@context': 'https://schema.org',
    '@type': 'SoftwareApplication',
    name: 'Estoy Ok',
    operatingSystem: 'ANDROID',
    applicationCategory: 'SecurityApplication',
    offers: {
      '@type': 'Offer',
      price: '0.00',
      priceCurrency: 'USD',
      priceValidUntil: '2027-12-31',
      description: '7 Días de Prueba Completa Gratis ($0.00 hoy)',
    },
    description: 'Plataforma de seguridad y asistencia familiar con monitoreo pasivo por Wi-Fi seguro y rastreo GPS activo en tiempo real.',
    aggregateRating: {
      '@type': 'AggregateRating',
      ratingValue: '4.9',
      ratingCount: '128',
    },
  };

  const jsonLdFaq = {
    '@context': 'https://schema.org',
    '@type': 'FAQPage',
    mainEntity: [
      {
        '@type': 'Question',
        name: '¿Cuál es la diferencia entre Bienestar Pasivo y Bienestar Activo en Estoy Ok?',
        acceptedAnswer: {
          '@type': 'Answer',
          text: 'El Bienestar Pasivo es una protección invisible diseñada para cuidar la intimidad: la app confirma automáticamente que el usuario está bien al conectarse al Wi-Fi seguro del hogar o mediante sensores de movimiento, sin mostrar su ubicación 24/7 en un mapa. El Bienestar Activo es para cuando están en la calle: activa mapa en tiempo real, Zonas Seguras y telemetría vehicular.',
        },
      },
      {
        '@type': 'Question',
        name: '¿Mi familia puede ver mi ubicación exacta todo el tiempo?',
        acceptedAnswer: {
          '@type': 'Answer',
          text: 'Tú eliges el nivel de privacidad. En modo Bienestar Pasivo, tus contactos solo reciben la confirmación de que estás a salvo sin ver tu posición en el mapa. La ubicación GPS exacta solo se activa en modo Activo o cuando se dispara una alerta de SOS / reporte vencido.',
        },
      },
      {
        '@type': 'Question',
        name: '¿Qué sucede si se me agota la batería o me quedo sin señal?',
        acceptedAnswer: {
          '@type': 'Answer',
          text: 'Estoy Ok registra el estado de los sensores de tu teléfono antes de perder señal. Si la batería baja de 15%, notifica a tus contactos. Si se apaga y el reporte vence, la plataforma despacha las alertas de contingencia con el último punto GPS conocido.',
        },
      },
      {
        '@type': 'Question',
        name: '¿Cómo funciona la respuesta de contingencia por WhatsApp?',
        acceptedAnswer: {
          '@type': 'Answer',
          text: 'Si vence el plazo de check-in y no se recibe respuesta pasiva ni manual, el servidor envía mensajes automáticos por WhatsApp a tus contactos de emergencia con un enlace seguro a la web de crisis.',
        },
      },
    ],
  };

  // Si no está autenticado, mostrar la Landing Page Comercial Premium
  return (
    <div className="flex flex-col min-h-screen bg-neutral-950 text-neutral-100 font-sans antialiased selection:bg-red-500 selection:text-white">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLdSoftware) }}
      />
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLdFaq) }}
      />
      {/* Header / Navigation */}
      <header className="sticky top-0 z-50 px-6 lg:px-12 h-20 flex items-center justify-between bg-neutral-950/80 backdrop-blur-md border-b border-neutral-900">
        <Link className="flex items-center gap-3 group" href="/">
          <img
            src="/logo-square.png"
            alt="Estoy Ok"
            className="w-10 h-10 object-contain group-hover:scale-105 transition-transform duration-300"
          />
          <span className="text-2xl font-black tracking-tight text-white">
            ESTOY <span className="text-red-500">OK</span>
          </span>
        </Link>
        <nav className="hidden md:flex items-center gap-8">
          <Link className="text-sm font-semibold text-neutral-400 hover:text-white transition-colors" href="#features">
            Características
          </Link>
          <Link className="text-sm font-semibold text-neutral-400 hover:text-white transition-colors" href="#crisis">
            Crisis Web
          </Link>
          <Link className="text-sm font-semibold text-neutral-400 hover:text-white transition-colors" href="#pricing">
            Planes
          </Link>
          <Link
            className="inline-flex h-11 items-center justify-center rounded-xl bg-gradient-to-r from-red-600 to-rose-600 px-6 text-sm font-bold text-white shadow-lg shadow-red-500/10 hover:shadow-red-500/30 hover:scale-[1.02] hover:from-red-500 hover:to-rose-500 active:scale-95 transition-all duration-200"
            href="#download"
          >
            Descargar App
          </Link>
        </nav>
        {/* Mobile quick CTA */}
        <div className="flex md:hidden items-center gap-3">
          <Link
            className="inline-flex h-10 items-center justify-center rounded-xl bg-red-600 px-4 text-xs font-bold text-white shadow-md shadow-red-500/10 active:scale-95 transition-all"
            href="#download"
          >
            Descargar
          </Link>
        </div>
      </header>

      <main className="flex-1">
        {/* Hero Section */}
        <section className="relative overflow-hidden pt-20 pb-16 md:pt-32 md:pb-24 lg:pt-40 lg:pb-36 bg-gradient-to-b from-neutral-950 via-neutral-900 to-neutral-950">
          <div className="absolute inset-0 bg-[linear-gradient(to_right,#171717_1px,transparent_1px),linear-gradient(to_bottom,#171717_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)] opacity-40"></div>
          
          {/* Ambient glows */}
          <div className="absolute top-0 left-1/4 -translate-x-1/2 w-80 h-80 rounded-full bg-red-600/10 blur-[120px] pointer-events-none"></div>
          <div className="absolute top-20 right-1/4 translate-x-1/2 w-80 h-80 rounded-full bg-blue-600/10 blur-[120px] pointer-events-none"></div>

          <div className="container px-6 mx-auto relative z-10 text-center max-w-5xl">
            <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-red-950/50 border border-red-500/30 text-xs font-semibold text-red-400 mb-8 shadow-sm">
              <span className="w-2 h-2 rounded-full bg-red-500 animate-pulse"></span>
              Protección Dual: Bienestar Pasivo (Invisible) + Tracking Activo Satelital
            </div>
            
            <h1 className="text-4xl font-extrabold tracking-tight sm:text-6xl md:text-7xl text-white leading-none">
              Tu familia protegida.<br />
              <span className="bg-clip-text text-transparent bg-gradient-to-r from-red-500 via-rose-500 to-indigo-500">
                Sin invadir su privacidad.
              </span>
            </h1>
            
            <p className="mx-auto max-w-3xl text-neutral-400 md:text-xl mt-6 leading-relaxed">
              La primera plataforma de asistencia que combina la <strong className="font-bold text-white">Protección Pasiva Invisible</strong> (confirmación automática por Wi-Fi seguro o movimiento sin violar la intimidad) con el <strong className="font-bold text-white">Rastreo Activo en Tiempo Real</strong> (Zonas Seguras, telemetría vehicular y SOS de emergencia) para cuando necesitas cuidar a los tuyos.
            </p>

            <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mt-10 max-w-md mx-auto sm:max-w-none">
              <Link
                className="relative group inline-flex h-14 w-full sm:w-auto items-center justify-center rounded-2xl bg-gradient-to-r from-red-600 to-rose-600 px-8 text-base font-bold text-white shadow-xl shadow-red-500/20 hover:shadow-red-500/40 hover:scale-[1.03] transition-all duration-300"
                href="#download"
              >
                <div className="absolute -inset-0.5 rounded-2xl bg-gradient-to-r from-red-500 to-rose-500 opacity-20 group-hover:opacity-60 blur-sm transition duration-300 pointer-events-none"></div>
                <span className="relative z-10 flex items-center gap-2">
                  Descargar &amp; Probar Gratis (7 Días)
                  <svg className="w-5 h-5 group-hover:translate-y-0.5 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M19 14l-7 7m0 0l-7-7m7 7V3" />
                  </svg>
                </span>
              </Link>
              <Link
                className="inline-flex h-14 w-full sm:w-auto items-center justify-center rounded-2xl border border-neutral-800 bg-neutral-900/40 hover:bg-neutral-900 px-8 text-base font-bold text-neutral-300 hover:text-white hover:border-neutral-700 transition-all duration-200"
                href="#features"
              >
                Ver Funcionalidades
              </Link>
            </div>
            
            {/* Real App Preview Mockup */}
            <div className="mt-16 md:mt-20 max-w-4xl mx-auto flex justify-center">
              <div className="relative rounded-3xl border border-neutral-800/80 bg-neutral-950/60 p-2 md:p-3 shadow-2xl shadow-green-500/10 hover:border-green-500/30 transition-all duration-300 overflow-hidden">
                <img 
                  src="/images/hero_mockup.jpg" 
                  alt="Aplicación Móvil Estoy Ok - Monitoreo de Bienestar y Estado A Salvo" 
                  className="w-full max-w-3xl h-auto rounded-2xl object-cover hover:scale-[1.01] transition-transform duration-500"
                />
              </div>
            </div>
          </div>
        </section>

        {/* Feature Detail Section (Dual Concept) */}
        <section id="features" className="py-24 px-6 bg-neutral-950 border-t border-neutral-900">
          <div className="container px-4 mx-auto max-w-6xl">
            <div className="text-center max-w-3xl mx-auto mb-20">
              <span className="px-3.5 py-1 text-xs font-bold tracking-widest uppercase border border-red-500/20 text-red-400 rounded-full">
                Dos Modos, Una Sola App
              </span>
              <h2 className="text-3xl font-extrabold sm:text-5xl text-white mt-4">
                Tres Niveles de Seguridad: Pasivo, Activo y Crisis
              </h2>
              <p className="text-neutral-400 mt-4 md:text-lg">
                Elige el nivel de protección que tu familia necesita: desde la tranquilidad pasiva sin rastreo continuo para cuidar la intimidad, hasta la coordinación GPS activa y la respuesta inmediata ante emergencias.
              </p>
            </div>

            <div className="grid lg:grid-cols-3 md:grid-cols-2 gap-8 items-stretch">
              {/* Pillar 1: Passive Wellbeing */}
              <div className="p-8 lg:p-10 rounded-3xl bg-neutral-900/30 border border-neutral-900 flex flex-col justify-between hover:border-red-500/30 transition-all duration-300 group relative overflow-hidden">
                <div className="absolute top-0 right-0 w-32 h-32 bg-red-600/5 rounded-full blur-3xl pointer-events-none"></div>
                <div>
                  <div className="w-12 h-12 rounded-2xl bg-red-950/50 border border-red-500/30 flex items-center justify-center text-red-500 mb-6 group-hover:bg-red-900/30 transition-all">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </div>
                  <span className="text-[10px] font-black uppercase tracking-widest text-red-500 bg-red-950/60 px-2.5 py-1 rounded-md border border-red-500/20">
                    Protección Invisible
                  </span>
                  <h3 className="text-2xl font-bold text-white mt-3">1. Bienestar Pasivo (Sin Rastreo 24/7)</h3>
                  <p className="text-neutral-400 mt-3 text-sm leading-relaxed">
                    Tranquilidad para tu familia sin sentirte vigilado ni obligado a mostrar tu ubicación continua en un mapa. Tu celular confirma automáticamente que estás bien.
                  </p>
                  
                  <ul className="space-y-3.5 mt-6 text-sm text-neutral-300">
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Auto-Check-in por Wi-Fi Seguro:</strong> Al conectarte al Wi-Fi de tu casa o trabajo, la app reporta silenciosamente que estás bien sin tocar el teléfono.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Auto-Check-in por Movimiento:</strong> Si das pasos o usas el móvil, los sensores registran tu bienestar de forma invisible.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Check-in Manual de 1 Toque:</strong> Un botón simple &quot;Estoy OK&quot; una vez al día para calmar ansiedades familiares.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Modo Sueño:</strong> Respeta tus horas de descanso sin generar alertas durante la noche.</span>
                    </li>
                  </ul>
                </div>
                <div className="mt-8 pt-6 border-t border-neutral-900 text-xs text-neutral-400 bg-neutral-950/40 -mx-8 -mb-8 p-6 rounded-b-3xl">
                  💡 <span className="font-semibold text-neutral-300">Ideal para:</span> Adultos mayores, estudiantes universitarios e independientes que exigen 100% de privacidad.
                </div>
              </div>

              {/* Pillar 2: Active Wellbeing */}
              <div className="p-8 lg:p-10 rounded-3xl bg-neutral-900/30 border border-neutral-900 flex flex-col justify-between hover:border-indigo-500/30 transition-all duration-300 group relative overflow-hidden">
                <div className="absolute top-0 right-0 w-32 h-32 bg-indigo-600/5 rounded-full blur-3xl pointer-events-none"></div>
                <div>
                  <div className="w-12 h-12 rounded-2xl bg-indigo-950/50 border border-indigo-500/30 flex items-center justify-center text-indigo-400 mb-6 group-hover:bg-indigo-900/30 transition-all">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                  </div>
                  <span className="text-[10px] font-black uppercase tracking-widest text-indigo-400 bg-indigo-950/60 px-2.5 py-1 rounded-md border border-indigo-500/20">
                    Monitoreo Satelital
                  </span>
                  <h3 className="text-2xl font-bold text-white mt-3">2. Bienestar Activo (GPS &amp; Telemetría)</h3>
                  <p className="text-neutral-400 mt-3 text-sm leading-relaxed">
                    Ubicación en tiempo real y Zonas Seguras cuando necesitas coordinar traslados o cuidar la ruta de los tuyos en la calle.
                  </p>
                  
                  <ul className="space-y-3.5 mt-6 text-sm text-neutral-300">
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-indigo-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Mapa del Núcleo en Vivo:</strong> Visualización en tiempo real con marcadores animados y estado de tránsito (caminando 🚶, bici 🚲, auto 🚗).</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-indigo-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Zonas Seguras Inteligentes:</strong> Avisos automáticos al entrar o salir de Casa, Colegio o Trabajo sin falsas alarmas.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-indigo-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Detección Vehicular &amp; Velocidad:</strong> Identifica viajes en automóvil, mide la velocidad en vivo y alerta sobreexcesos.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-indigo-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Estado de Sensores &amp; Batería:</strong> Notifica si le queda menos de 15% de carga, apaga el GPS o se queda sin señal.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-indigo-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Radar Móvil de Proximidad:</strong> Perímetro dinámico para paseos o compras (vibran si se distancian).</span>
                    </li>
                  </ul>
                </div>
                <div className="mt-8 pt-6 border-t border-neutral-900 text-xs text-neutral-400 bg-neutral-950/40 -mx-8 -mb-8 p-6 rounded-b-3xl">
                  💡 <span className="font-semibold text-neutral-300">Ideal para:</span> Niños, jóvenes en edad escolar, traslados nocturnos y viajes en carretera.
                </div>
              </div>

              {/* Pillar 3: Silent S.O.S */}
              <div className="p-8 lg:p-10 rounded-3xl bg-neutral-900/30 border border-neutral-900 flex flex-col justify-between hover:border-amber-500/30 transition-all duration-300 group relative overflow-hidden">
                <div className="absolute top-0 right-0 w-32 h-32 bg-amber-600/5 rounded-full blur-3xl pointer-events-none"></div>
                <div>
                  <div className="w-12 h-12 rounded-2xl bg-amber-950/50 border border-amber-500/30 flex items-center justify-center text-amber-500 mb-6 group-hover:bg-amber-900/30 transition-all">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                    </svg>
                  </div>
                  <span className="text-[10px] font-black uppercase tracking-widest text-amber-400 bg-amber-950/60 px-2.5 py-1 rounded-md border border-amber-500/20">
                    Respuesta de Auxilio
                  </span>
                  <h3 className="text-2xl font-bold text-white mt-3">3. S.O.S. de Emergencia (Crisis Inminente)</h3>
                  <p className="text-neutral-400 mt-3 text-sm leading-relaxed">
                    Botón de auxilio e integración de impacto para situaciones de peligro real. Activa alarmas críticas y seguimiento prioritario.
                  </p>
                  
                  <ul className="space-y-3.5 mt-6 text-sm text-neutral-300">
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-amber-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">S.O.S. Silencioso:</strong> Dispara alarmas prioritarias de forma imperceptible desde tu teléfono móvil.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-amber-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Rastreo Crítico (Cada 5s):</strong> Acelera el GPS a intervalos de 5 segundos para un seguimiento milimétrico.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-amber-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Grabación Ambiental de 15s:</strong> Captura audio de fondo en vivo para escuchar el contexto de la emergencia.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-amber-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Detección de Choques (G-Force):</strong> Registra desaceleraciones severas (≥ 4.5G) con sirena pre-alerta de 15s.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-amber-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span><strong className="font-bold text-white">Canal de Contingencia:</strong> Transmite coordenadas y alertas prioritarias si se interrumpe la conexión habitual.</span>
                    </li>
                  </ul>
                </div>
                <div className="mt-8 pt-6 border-t border-neutral-900 text-xs text-neutral-400 bg-neutral-950/40 -mx-8 -mb-8 p-6 rounded-b-3xl">
                  💡 <span className="font-semibold text-neutral-300">Ideal para:</span> Situaciones de peligro inminente, seguridad urbana y emergencias reales.
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Crisis Web Section */}
        <section id="crisis" className="py-24 px-6 bg-neutral-900/10 border-t border-neutral-900">
          <div className="container px-4 mx-auto max-w-5xl">
            <div className="grid md:grid-cols-2 gap-12 items-center">
              <div className="text-left">
                <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-red-950 text-red-400 text-xs font-semibold border border-red-500/20 mb-4">
                  Garantía de Contingencia
                </div>
                <h2 className="text-3xl font-extrabold text-white sm:text-4xl leading-tight">
                  Canales de Alerta de Crisis &amp; Respuestas de Apoyo
                </h2>
                <p className="text-neutral-400 mt-4 leading-relaxed text-sm">
                  Si vences tu plazo de reporte sin check-in, Estoy Ok despacha de inmediato notificaciones asincrónicas a tus familiares de confianza.
                </p>

                <div className="space-y-4 mt-8">
                  <div className="flex gap-3">
                    <div className="w-6 h-6 rounded-full bg-red-950 border border-red-500/20 flex items-center justify-center text-red-400 text-xs font-black shrink-0 mt-1">1</div>
                    <div>
                      <h4 className="font-bold text-white text-sm">Mensajería Prioritaria vía WhatsApp</h4>
                      <p className="text-xs text-neutral-400 mt-1">Tus contactos de emergencia reciben un mensaje con un enlace seguro de crisis cifrado.</p>
                    </div>
                  </div>
                  <div className="flex gap-3">
                    <div className="w-6 h-6 rounded-full bg-red-950 border border-red-500/20 flex items-center justify-center text-red-400 text-xs font-black shrink-0 mt-1">2</div>
                    <div>
                      <h4 className="font-bold text-white text-sm">Acceso Directo a la Web de Emergencia</h4>
                      <p className="text-xs text-neutral-400 mt-1">El familiar abre el link y accede instantáneamente a un mapa interactivo con tu última ubicación y nivel de batería, sin necesidad de tener la app instalada.</p>
                    </div>
                  </div>
                  <div className="flex gap-3">
                    <div className="w-6 h-6 rounded-full bg-red-950 border border-red-500/20 flex items-center justify-center text-red-400 text-xs font-black shrink-0 mt-1">3</div>
                    <div>
                      <h4 className="font-bold text-white text-sm">Feedback Interactivo de Apoyo</h4>
                      <p className="text-xs text-neutral-400 mt-1">Los familiares pueden responder desde el sitio web seleccionando *&quot;Voy en camino&quot;* o *&quot;Recibido&quot;* para darte tranquilidad una vez que te localicen.</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Real Crisis Web Dashboard Mockup */}
              <div className="relative mx-auto w-full max-w-xl md:max-w-2xl bg-neutral-950 rounded-3xl border border-neutral-800 p-2 md:p-3 shadow-2xl overflow-hidden hover:scale-[1.02] transition-transform duration-300">
                <img 
                  src="/images/crisis_mockup.jpg" 
                  alt="Sitio Web de Emergencia Estoy Ok - Mapa de Crisis y Respuestas Voy en camino / Recibido" 
                  className="w-full h-auto rounded-2xl object-cover"
                />
              </div>
            </div>
          </div>
        </section>

        {/* Pricing Plan Section (Interactive) */}
        <section id="pricing" className="py-24 px-6 bg-neutral-950 border-t border-neutral-900 relative">
          <div className="absolute bottom-0 right-1/4 w-96 h-96 rounded-full bg-red-600/5 blur-[150px] pointer-events-none"></div>

          <div className="container px-4 mx-auto max-w-5xl text-center">
            <h2 className="text-3xl font-extrabold sm:text-5xl">Prueba la Experiencia Completa</h2>
            <p className="text-neutral-400 mt-4 md:text-lg max-w-2xl mx-auto">
              Comienza hoy mismo a cuidar de tu familia. Descarga la aplicación y accede de inmediato a <strong className="text-white font-bold">7 Días de Prueba Completa por $0.00</strong> sin compromisos.
            </p>

            <div className="grid md:grid-cols-2 gap-8 items-stretch max-w-4xl mx-auto mt-16">
              {/* Plan Gratis */}
              <div className="flex flex-col justify-between p-8 bg-neutral-900/30 border border-neutral-900 rounded-3xl hover:border-neutral-850 transition-all text-left">
                <div>
                  <h3 className="text-lg font-bold text-neutral-400">Plan Inicial</h3>
                  <div className="text-5xl font-black text-white mt-4 flex items-baseline gap-1">
                    $0
                    <span className="text-xs text-neutral-500 font-semibold uppercase tracking-wider">Para siempre</span>
                  </div>
                  <p className="text-xs text-neutral-500 mt-2">Seguridad y monitoreo pasivo diario simplificado.</p>

                  <ul className="space-y-3.5 mt-8 text-sm text-neutral-300">
                    <li className="flex items-center gap-2.5">
                      <svg className="w-4 h-4 text-green-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>Monitoreo de Bienestar Pasivo diario</span>
                    </li>
                    <li className="flex items-center gap-2.5">
                      <svg className="w-4 h-4 text-green-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>Alertas por Email y Push en caso de vencimiento</span>
                    </li>
                    <li className="flex items-center gap-2.5">
                      <svg className="w-4 h-4 text-green-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>Mapa web y móvil básico</span>
                    </li>
                    <li className="flex items-center gap-2.5">
                      <svg className="w-4 h-4 text-green-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>Historial de ubicación (24 horas)</span>
                    </li>
                    <li className="flex items-center gap-2.5">
                      <svg className="w-4 h-4 text-green-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>Máximo 1 Zona Segura (Zona Segura)</span>
                    </li>
                  </ul>
                </div>

                <Link
                  className="mt-8 text-center bg-neutral-800 hover:bg-neutral-700 text-white font-bold py-3.5 rounded-xl transition-colors text-sm"
                  href="/register"
                >
                  Registrarse Gratis
                </Link>
              </div>

              {/* Plan Premium */}
              <div className="relative flex flex-col justify-between p-8 bg-gradient-to-b from-neutral-900 to-red-950/20 border-2 border-red-500 rounded-3xl text-left transform md:scale-105 shadow-2xl">
                <div className="absolute -top-3 left-1/2 -translate-x-1/2 bg-red-600 text-white text-[9px] font-black px-4 py-0.5 rounded-full uppercase tracking-widest">
                  7 DÍAS GRATIS POR $0.00
                </div>
                
                <div>
                  <h3 className="text-lg font-bold text-red-400 mt-2">Experiencia Completa Estoy Ok</h3>
                  <div className="text-5xl font-black text-white mt-4 flex items-baseline gap-1 font-sans">
                    $4.99<span className="text-xs text-neutral-400 font-medium">/ mes (tras 7 días gratis)</span>
                  </div>
                  <p className="text-xs text-red-500/80 mt-2 font-medium">Acceso ilimitado a todas las funcionalidades estrella de protección activa y pasiva.</p>

                  <ul className="space-y-3.5 mt-8 text-sm text-neutral-300">
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <div>
                        <span className="font-bold text-white block">Alertas por WhatsApp</span>
                        <span className="text-xs text-neutral-400 block leading-relaxed">Alertas de inactividad o SOS enviadas de inmediato por canales de alta lectura, sin requerir que tengan la app abierta.</span>
                      </div>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <div>
                        <span className="font-bold text-white block">Auto-Check-in Pasivo Inteligente</span>
                        <span className="text-xs text-neutral-400 block leading-relaxed">Olvídate de los reportes diarios manuales. Confirma tu bienestar de forma invisible al conectar a tu Wi-Fi seguro o con tus pasos diarios.</span>
                      </div>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <div>
                        <span className="font-bold text-white block">S.O.S. Silencioso y Grabación</span>
                        <span className="text-xs text-neutral-400 block leading-relaxed">Envía auxilio instantáneo en alta frecuencia (5s) y graba 15s de audio ambiente disponibles en vivo para tus contactos en situaciones de crisis.</span>
                      </div>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <div>
                        <span className="font-bold text-white block">Detección de Choques (🚗 Fuerza G)</span>
                        <span className="text-xs text-neutral-400 block leading-relaxed">El acelerómetro del móvil detecta impactos automotrices graves, disparando alertas de crisis con coordenadas del siniestro.</span>
                      </div>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <div>
                        <span className="font-bold text-white block">Historial y Rutas (30 Días)</span>
                        <span className="text-xs text-neutral-400 block leading-relaxed">Accede y reproduce la trayectoria exacta, velocidad e historial de sensores de tu núcleo de los últimos 30 días.</span>
                      </div>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <div>
                        <span className="font-bold text-white block">Radares de Proximidad Relativos</span>
                        <span className="text-xs text-neutral-400 block leading-relaxed">Crea perímetros móviles dinámicos con tus hijos. Ambos teléfonos vibrarán de forma persistente si se distancian demasiado.</span>
                      </div>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <div>
                        <span className="font-bold text-white block">SOS Ilimitado y Coordinación de Rescate</span>
                        <span className="text-xs text-neutral-400 block leading-relaxed">Emisión ilimitada de alertas SOS sin tiempo de espera. Cuando un familiar responde &quot;Voy en camino&quot; o &quot;Enterado&quot;, todos los contactos y tú reciben avisos inmediatos por WhatsApp y Push.</span>
                      </div>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <div>
                        <span className="font-bold text-white block">Monitoreo de Sensores y Batería</span>
                        <span className="text-xs text-neutral-400 block leading-relaxed">Recibe notificaciones inmediatas si un miembro apaga su GPS, activa el Modo Avión, pierde señal o le queda menos de 15% de batería.</span>
                      </div>
                    </li>
                  </ul>
                </div>

                <Link
                  className="mt-8 text-center bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-500 hover:to-rose-500 text-white font-extrabold py-4 rounded-xl transition-all shadow-lg shadow-red-500/20 text-sm"
                  href="#download"
                >
                  Comenzar Prueba Gratis de 7 Días
                </Link>
              </div>
            </div>

            <div className="mt-16 text-xs text-neutral-500 flex items-center justify-center gap-4">
              <span>🔒 SSL Seguro</span>
              <span>•</span>
              <span>🛡️ Privacidad Garantizada</span>
              <span>•</span>
              <span>💳 Cancela cuando quieras</span>
            </div>
          </div>
        </section>

        {/* Download Section */}
        <section id="download" className="py-24 px-6 bg-neutral-950 border-t border-neutral-900 relative overflow-hidden">
          <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 rounded-full bg-red-500/5 blur-[120px] pointer-events-none"></div>
          <div className="container px-4 mx-auto max-w-4xl text-center relative z-10">
            <span className="px-3 py-1 text-xs font-bold tracking-widest uppercase border border-red-500/20 text-red-400 rounded-full">Descarga Estoy Ok</span>
            <h2 className="text-3xl font-extrabold sm:text-5xl text-white mt-4">Disponible en tu dispositivo móvil</h2>
            <p className="text-neutral-400 mt-4 max-w-2xl mx-auto">
              Toda la funcionalidad de geolocalización activa, check-in diario de bienestar y alertas críticas está disponible de forma exclusiva a través de nuestras aplicaciones para smartphones.
            </p>
            
            <div className="flex flex-col sm:flex-row items-center justify-center gap-6 mt-12">
              {/* Google Play Store Simulator */}
              <a 
                href="/download/android" 
                onClick={(e) => {
                  e.preventDefault();
                  alert("La aplicación para Android está disponible en la carpeta android-native. Puedes compilarla directamente en Android Studio o generar el APK.");
                }}
                className="flex items-center gap-3 bg-neutral-900 border border-neutral-800 rounded-2xl px-6 py-3.5 text-left hover:bg-neutral-850 hover:border-neutral-700 active:scale-95 transition-all w-full sm:w-auto justify-center"
              >
                <svg className="w-8 h-8 text-neutral-300" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M5.23 2.062a2.38 2.38 0 0 0-.58.375l10.98 10.98 3.5-3.5-13.9-7.855zm-1.07 1.48v16.92L14.73 12 4.16 3.542zm11.75 9.638l3.65 3.65c.29-.16.54-.39.73-.66l-4.38-2.99zm-4.93 1.28L4.65 21.79c.2.14.43.21.68.21.36 0 .7-.1 1-.27l13.68-7.73-3.67-3.67z" />
                </svg>
                <div>
                  <div className="text-[10px] text-neutral-500 uppercase font-black">Consíguelo en</div>
                  <div className="text-base font-bold text-white leading-tight">Google Play</div>
                </div>
              </a>
            </div>

            {/* Direct APK Download option for Android debug/testing */}
            <div className="mt-8">
              <span className="text-sm text-neutral-500">¿Eres betatester o desarrollador?</span>
              <div className="mt-2">
                <a 
                  href="/download/apk"
                  onClick={(e) => {
                    e.preventDefault();
                    alert("Para instalar en Android, compila el proyecto de la carpeta android-native/ en Android Studio o descarga el APK generado en tus compilaciones locales.");
                  }}
                  className="inline-flex items-center gap-2 text-xs font-semibold text-neutral-400 hover:text-white border border-neutral-800 bg-neutral-900/20 rounded-full px-4 py-2 hover:border-neutral-700 transition-all"
                >
                  📥 Descargar APK (Android Beta)
                </a>
              </div>
            </div>
          </div>
        </section>

        {/* FAQ Section (Interactive Accordion) */}
        <section id="faq" className="py-24 px-6 bg-neutral-900/20 border-t border-neutral-900">
          <div className="container px-4 mx-auto max-w-4xl">
            <div className="text-center mb-16">
              <span className="px-3 py-1 text-xs font-bold tracking-widest uppercase border border-red-500/20 text-red-400 rounded-full">Preguntas Frecuentes</span>
              <h2 className="text-3xl font-extrabold sm:text-5xl text-white mt-4">Dudas comunes resueltas</h2>
              <p className="text-neutral-400 mt-4">Todo lo que necesitas saber sobre el funcionamiento de Estoy Ok.</p>
            </div>

            <div className="space-y-4">
              {[
                {
                  q: "¿Cuál es la diferencia entre Bienestar Pasivo y Bienestar Activo en Estoy Ok?",
                  a: "El Bienestar Pasivo confirma que te encuentras a salvo automáticamente mediante tu Wi-Fi seguro de casa o tu movimiento, sin activar rastreo continuo ni compartir tu ubicación en un mapa (respetando tu privacidad al 100%). El Bienestar Activo habilita el seguimiento GPS en tiempo real, Zonas Seguras y telemetría vehicular para cuando necesites coordinar traslados o cuidar a tus hijos en la calle."
                },
                {
                  q: "¿Mi familia puede ver mi ubicación exacta todo el tiempo?",
                  a: "No si utilizas el modo de Bienestar Pasivo. En este modo, tu ubicación geográfica solo se vuelve accesible para tus contactos de emergencia en caso de que venza tu temporizador de seguridad sin confirmación previa o si presionas de forma voluntaria el botón S.O.S. de emergencia."
                },
                {
                  q: "¿Qué es y cómo funciona el botón de \"Estoy Ok\"?",
                  a: "Es un sistema de bienestar pasivo diseñado para proteger tu privacidad. Solo debes pulsar el botón una vez al día para confirmar que te encuentras bien y reiniciar tu plazo de seguridad. Si el plazo se vence y olvidas reportarte, el sistema despacha alertas automáticas prioritarias a todos tus contactos de emergencia."
                },
                {
                  q: "¿Cómo funciona el Auto-Check-in inteligente?",
                  a: "Es una característica exclusiva del Plan PRO. En lugar de presionar el botón manualmente, la app móvil de Estoy Ok utiliza sensores en segundo plano de manera silenciosa: detecta si tu celular se conecta a la red Wi-Fi segura de tu hogar o si registras más de 100 pasos en una hora mediante el podómetro integrado para confirmar tu bienestar de forma automática."
                },
                {
                  q: "¿Cómo funciona el S.O.S. Silencioso de Emergencia?",
                  a: "Es un botón de auxilio instantáneo para situaciones de peligro inmediato. Al presionarlo en la app móvil, se dispara de forma discreta una alerta crítica: la tasa de actualización de tu ubicación aumenta a cada 5 segundos y se graban 15 segundos de audio ambiente de fondo de forma imperceptible. Tu núcleo familiar recibe notificaciones prioritarias y WhatsApp (según el plan) con un enlace para ver tu ubicación y escuchar el audio."
                },
                {
                  q: "¿Cómo invito a mis familiares a unirse a mi Núcleo?",
                  a: "Crear y unirse a un núcleo es sumamente simple. En la sección \"Mis Núcleos\", selecciona el núcleo que creaste y copia el código de invitación único de 10 caracteres. Compártelo con tu familiar (por ejemplo, por WhatsApp). Tu familiar solo tendrá que crear su cuenta, pulsar en \"Unirse a un Núcleo\" e ingresar el código para estar vinculados."
                },
                {
                  q: "¿Qué son las Zonas Seguras y cómo nos alertan?",
                  a: "Las Zonas Seguras son áreas delimitadas que configuras en el mapa (como la casa, escuela o trabajo). Cuando el GPS del celular de un miembro del núcleo ingresa o sale de este radio de seguridad, todos los demás integrantes reciben una notificación Push instantánea en tiempo real."
                },
                {
                  q: "¿Cómo funciona la detección de conducción y alertas de velocidad?",
                  a: "La app móvil de Estoy Ok detecta de manera inteligente si te encuentras viajando en un automóvil mediante el sensor de GPS. Si la velocidad detectada supera los 25 km/h de manera sostenida por más de 1 minuto, el estado del usuario cambia automáticamente a conducción (🚗) y el núcleo puede ver un coche desplazándose en el mapa con su velocidad en vivo. Si en algún momento se supera el límite de velocidad establecido por el creador del núcleo (ej. 120 km/h), el sistema registra el incidente en el backend y envía una notificación push inmediata al creador del núcleo para prevenir posibles imprudencias."
                },
                {
                  q: "¿Cómo funciona la detección automática de accidentes vehiculares?",
                  a: "Utiliza el sensor del acelerómetro físico en teléfonos inteligentes para registrar desaceleraciones extremas o impactos severos (umbrales superiores a 4.5G) característicos de un accidente de tránsito. Si el sistema detecta un impacto y el dispositivo permanece inmóvil por 3 segundos (indicando una colisión), se inicia una pre-alerta de 15 segundos con un sonido fuerte de sirena. Si no se cancela pulsando 'Estoy bien', se activa de inmediato el protocolo de crisis máxima: se envía la ubicación GPS exacta y una grabación ambiental de audio a los familiares vía WhatsApp y notificaciones Push críticas."
                },
                {
                  q: "¿Qué pasa si mi celular se queda sin batería o no tiene señal?",
                  a: "Estoy Ok cuenta con prevención activa: detecta cuando tu batería baja del 15% y alerta a tus familiares. Además, el panel del núcleo diferencia en tiempo real si el dispositivo tiene el GPS apagado, el rastreo desactivado voluntariamente o si está sin señal de internet. Si el celular se apaga por completo y expira tu temporizador, el sistema despacha alertas prioritarias vía WhatsApp."
                },
                {
                  q: "¿Cómo funciona la prueba gratuita de 7 días y la cancelación?",
                  a: "Al iniciar tu registro o seleccionar el Plan PRO, obtienes 7 días de acceso completo por $0.00. El día 5 te enviamos una notificación preventiva por push e email. Al finalizar los 7 días (día 8), comienza la suscripción mensual flexible de $4.99/mes. Puedes cancelar en cualquier momento durante los 7 días desde tus Ajustes o tiendas de aplicaciones sin que se te efectúe ningún cobro."
                }
              ].map((faq, index) => {
                const isOpen = openFaqIndex === index;
                return (
                  <div 
                    key={index} 
                    className="border border-neutral-900 bg-neutral-950/50 rounded-2xl overflow-hidden transition-colors"
                  >
                    <button
                      onClick={() => setOpenFaqIndex(isOpen ? null : index)}
                      className="w-full px-6 py-5 flex items-center justify-between text-left font-bold text-white hover:text-red-400 hover:bg-neutral-900/30 transition-all focus:outline-none"
                    >
                      <span>{faq.q}</span>
                      <span className={`text-lg transition-transform duration-300 ${isOpen ? 'rotate-180 text-red-500' : 'text-neutral-500'}`}>
                        {isOpen ? '−' : '+'}
                      </span>
                    </button>
                    <div 
                      className={`transition-all duration-300 ease-in-out overflow-hidden ${isOpen ? 'max-h-60 border-t border-neutral-900' : 'max-h-0'}`}
                    >
                      <p className="px-6 py-5 text-sm text-neutral-400 leading-relaxed">
                        {faq.a}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </section>
      </main>


      <footer className="py-12 border-t border-neutral-900 bg-neutral-950 text-center text-neutral-500 text-xs">
        <p>© 2026 ESTOY OK. Todos los derechos reservados.</p>
        <p className="mt-2 text-[10px] text-neutral-600">Protección integral y redundancia de comunicación para la seguridad de tu familia.</p>
        <div className="mt-4 flex items-center justify-center gap-6 text-xs text-neutral-400">
          <Link href="/politica-de-privacidad" className="hover:text-red-400 transition-colors underline underline-offset-4">
            Política de Privacidad
          </Link>
        </div>
      </footer>
    </div>
  );
}
