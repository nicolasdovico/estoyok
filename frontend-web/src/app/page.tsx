'use client';

import { useState, useEffect } from 'react';
import Link from "next/link";
import Dashboard from '@/components/Dashboard';

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

  // Si está autenticado, mostrar el Dashboard
  if (isAuthenticated) {
    return <Dashboard />;
  }

  // Si no está autenticado, mostrar la Landing Page Comercial Premium
  return (
    <div className="flex flex-col min-h-screen bg-neutral-950 text-neutral-100 font-sans antialiased selection:bg-red-500 selection:text-white">
      {/* Header / Navigation */}
      <header className="sticky top-0 z-50 px-6 lg:px-12 h-20 flex items-center justify-between bg-neutral-950/80 backdrop-blur-md border-b border-neutral-900">
        <Link className="flex items-center gap-2 group" href="/">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-red-500 to-rose-600 flex items-center justify-center shadow-lg shadow-red-500/20 group-hover:scale-105 transition-transform duration-300">
            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
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
          <Link className="text-sm font-semibold text-neutral-400 hover:text-white transition-colors" href="/login">
            Iniciar Sesión
          </Link>
          <Link
            className="inline-flex h-11 items-center justify-center rounded-xl bg-gradient-to-r from-red-600 to-rose-600 px-6 text-sm font-bold text-white shadow-lg shadow-red-500/10 hover:shadow-red-500/30 hover:scale-[1.02] hover:from-red-500 hover:to-rose-500 active:scale-95 transition-all duration-200"
            href="/register"
          >
            Registrarse
          </Link>
        </nav>
        {/* Mobile quick CTA */}
        <div className="flex md:hidden items-center gap-3">
          <Link className="text-sm font-bold text-neutral-400 hover:text-white transition-colors px-2" href="/login">
            Ingresar
          </Link>
          <Link
            className="inline-flex h-10 items-center justify-center rounded-xl bg-red-600 px-4 text-xs font-bold text-white shadow-md shadow-red-500/10 active:scale-95 transition-all"
            href="/register"
          >
            Registro
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
            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-red-950/45 border border-red-500/20 text-xs font-semibold text-red-400 mb-8 animate-pulse">
              <span className="w-2 h-2 rounded-full bg-red-500"></span>
              Seguridad Familiar Integral: Activa &amp; Pasiva
            </div>
            
            <h1 className="text-4xl font-extrabold tracking-tight sm:text-6xl md:text-7xl text-white leading-none">
              Tu familia protegida.<br />
              <span className="bg-clip-text text-transparent bg-gradient-to-r from-red-500 via-rose-500 to-indigo-500">
                Sin invadir su privacidad.
              </span>
            </h1>
            
            <p className="mx-auto max-w-2xl text-neutral-400 md:text-xl mt-6 leading-relaxed">
              La primera plataforma de asistencia que combina el **Rastreo GPS Activo** en tiempo real con un sistema inteligente de **Bienestar Pasivo** (Wi-Fi seguro y movimiento) con alertas redundantes de emergencia.
            </p>

            <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mt-10 max-w-md mx-auto sm:max-w-none">
              <Link
                className="relative group inline-flex h-14 w-full sm:w-auto items-center justify-center rounded-2xl bg-gradient-to-r from-red-600 to-rose-600 px-8 text-base font-bold text-white shadow-xl shadow-red-500/20 hover:shadow-red-500/40 hover:scale-[1.03] transition-all duration-300"
                href="/register"
              >
                <div className="absolute -inset-0.5 rounded-2xl bg-gradient-to-r from-red-500 to-rose-500 opacity-20 group-hover:opacity-60 blur-sm transition duration-300 pointer-events-none"></div>
                <span className="relative z-10 flex items-center gap-2">
                  Registrarse Gratis
                  <svg className="w-5 h-5 group-hover:translate-x-1 transition-transform" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M13 5l7 7-7 7M5 5l7 7-7 7" />
                  </svg>
                </span>
              </Link>
              <Link
                className="inline-flex h-14 w-full sm:w-auto items-center justify-center rounded-2xl border border-neutral-800 bg-neutral-900/40 hover:bg-neutral-900 px-8 text-base font-bold text-neutral-300 hover:text-white hover:border-neutral-700 transition-all duration-200"
                href="#features"
              >
                Saber más
              </Link>
            </div>
            
            {/* Visual device mockups (CSS Only) */}
            <div className="mt-16 md:mt-24 rounded-3xl border border-neutral-900 bg-neutral-950 p-4 shadow-2xl max-w-4xl mx-auto">
              <div className="w-full h-8 bg-neutral-900/60 rounded-t-2xl flex items-center px-4 gap-2 border-b border-neutral-900">
                <span className="w-3.5 h-3.5 rounded-full bg-red-500/80"></span>
                <span className="w-3.5 h-3.5 rounded-full bg-yellow-500/80"></span>
                <span className="w-3.5 h-3.5 rounded-full bg-green-500/80"></span>
                <div className="mx-auto bg-neutral-950/80 rounded-md text-[10px] text-neutral-500 px-10 py-1 font-mono border border-neutral-800/40 truncate">
                  https://estoyok.com/dashboard
                </div>
              </div>
              <div className="aspect-[16/9] w-full bg-neutral-900/30 rounded-b-2xl overflow-hidden flex flex-col md:flex-row items-center justify-center p-6 gap-8">
                {/* Simulated Check-in Phone View */}
                <div className="w-[185px] aspect-[9/16] bg-neutral-950 rounded-[32px] border-4 border-neutral-800 p-4 flex flex-col justify-between shadow-2xl shrink-0">
                  <div className="w-12 h-3.5 bg-neutral-850 rounded-full mx-auto mb-2"></div>
                  
                  <div className="flex-1 flex flex-col justify-between py-2 text-center">
                    <div>
                      <div className="text-[9px] text-neutral-500 uppercase tracking-widest font-black">Estado</div>
                      <div className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-green-950 border border-green-500/20 text-[9px] font-bold text-green-400 mt-1">
                        <span className="w-1.5 h-1.5 rounded-full bg-green-400"></span>
                        A Salvo
                      </div>
                    </div>

                    <div className="flex flex-col items-center">
                      <div className="w-18 h-18 rounded-full bg-gradient-to-tr from-green-500 to-emerald-600 flex items-center justify-center shadow-lg shadow-green-500/15 border-4 border-green-950/40">
                        <span className="text-white text-[10px] font-black">ESTOY OK</span>
                      </div>
                      <p className="text-[8px] text-neutral-500 mt-2.5 px-2">
                        Presiona una vez al día para confirmar bienestar.
                      </p>
                    </div>

                    <div className="bg-neutral-900/60 border border-neutral-850 p-2 rounded-xl text-[8px] text-neutral-400 text-left">
                      📡 Auto-Check-in Activo:<br/>
                      🏠 Red Wi-Fi hogar detectada.
                    </div>
                  </div>
                </div>

                {/* Simulated Desktop Dashboard map preview */}
                <div className="flex-1 w-full h-full min-h-[160px] bg-neutral-950/60 rounded-2xl border border-neutral-850 p-4 flex flex-col justify-between text-left">
                  <div className="flex items-center justify-between border-b border-neutral-900 pb-2">
                    <div className="text-xs font-bold text-neutral-200">Mapa del Núcleo Familiar</div>
                    <div className="text-[9px] text-neutral-500">Localizaciones en tiempo real</div>
                  </div>
                  <div className="flex-1 flex items-center justify-center relative bg-neutral-900/30 rounded-xl my-3 border border-neutral-900 overflow-hidden">
                    <div className="absolute inset-0 bg-[radial-gradient(#ffffff02_1px,transparent_1px)] bg-[size:12px_12px]"></div>
                    {/* User Marker */}
                    <div className="absolute top-1/2 left-1/3 -translate-y-1/2 -translate-x-1/2 flex flex-col items-center">
                      <div className="px-2 py-0.5 rounded bg-red-600 text-white text-[8px] font-bold shadow-lg">Hijo (Lucas)</div>
                      <div className="w-2 h-2 rounded-full bg-red-600 mt-1 ring-4 ring-red-500/20 animate-pulse"></div>
                    </div>
                    {/* Geofence safe circle */}
                    <div className="absolute top-1/2 left-1/3 -translate-y-1/2 -translate-x-1/2 w-20 h-20 rounded-full border-2 border-dashed border-red-500/10 bg-red-500/5 flex items-center justify-center">
                      <span className="text-[7px] text-red-500/30 font-mono">Colegio 200m</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 text-[10px]">
                    <span className="w-2.5 h-2.5 rounded-full bg-green-500 flex items-center justify-center text-[7px] text-white font-bold">✓</span>
                    <span className="text-neutral-400">Lucas ingresó a la Zona Segura Colegio</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Feature Detail Section (Dual Concept) */}
        <section id="features" className="py-24 px-6 bg-neutral-950 border-t border-neutral-900">
          <div className="container px-4 mx-auto max-w-6xl">
            <div className="text-center max-w-3xl mx-auto mb-20">
              <h2 className="text-3xl font-extrabold sm:text-5xl">Tres Niveles de Seguridad Familiar</h2>
              <p className="text-neutral-400 mt-4 md:text-lg">
                Desde el monitoreo pasivo diario para respetar la intimidad, hasta el rastreo activo por zonas y la respuesta inmediata ante crisis extremas.
              </p>
            </div>

            <div className="grid lg:grid-cols-3 md:grid-cols-2 gap-8 items-stretch">
              {/* Pillar 1: Passive Wellbeing */}
              <div className="p-8 lg:p-10 rounded-3xl bg-neutral-900/30 border border-neutral-900 flex flex-col justify-between hover:border-red-500/20 transition-all duration-300 group">
                <div>
                  <div className="w-12 h-12 rounded-2xl bg-red-950/40 border border-red-500/20 flex items-center justify-center text-red-500 mb-6 group-hover:bg-red-900/20 transition-all">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </div>
                  <h3 className="text-2xl font-bold text-white">1. Bienestar Pasivo (Botón &amp; Sensores)</h3>
                  <p className="text-neutral-400 mt-3 leading-relaxed">
                    Pensado para resguardar la intimidad familiar. Confirma tu bienestar una vez al día de forma activa o deja que el celular lo haga por ti en segundo plano.
                  </p>
                  
                  <ul className="space-y-3.5 mt-6 text-sm text-neutral-300">
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**Check-in Manual de 1 toque:** Un gran botón rojo para calmar ansiedades familiares.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**Auto-Check-in por Wi-Fi:** Al conectarte al Wi-Fi seguro configurado de tu casa, la app hace check-in silencioso por ti.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**Detección de Movimiento:** Si das más de 100 pasos en una hora, la app asume que estás bien y reporta actividad de bienestar automáticamente.</span>
                    </li>
                  </ul>
                </div>
                <div className="mt-8 pt-6 border-t border-neutral-900 text-xs text-neutral-500">
                  Ideal para adultos mayores y estudiantes independientes.
                </div>
              </div>

              {/* Pillar 2: Active Wellbeing */}
              <div className="p-8 lg:p-10 rounded-3xl bg-neutral-900/30 border border-neutral-900 flex flex-col justify-between hover:border-indigo-500/20 transition-all duration-300 group">
                <div>
                  <div className="w-12 h-12 rounded-2xl bg-indigo-950/40 border border-indigo-500/20 flex items-center justify-center text-indigo-400 mb-6 group-hover:bg-indigo-900/20 transition-all">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                  </div>
                  <h3 className="text-2xl font-bold text-white">2. Bienestar Activo (Núcleos &amp; Mapa)</h3>
                  <p className="text-neutral-400 mt-3 leading-relaxed">
                    Ubicación satelital y Zonas Seguras inteligentes para cuando necesitas coordinación familiar rápida y precisa en tus trayectos.
                  </p>
                  
                  <ul className="space-y-3.5 mt-6 text-sm text-neutral-300">
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-indigo-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**Mapa Familiar Compartido:** Visualización en vivo de los integrantes autorizados de tu núcleo.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-indigo-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**Zonas Seguras:** Configura radios de alerta y recibe notificaciones push cuando entren o salgan de casa, colegio o trabajo.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-indigo-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**Alertas de Batería Baja &amp; Sensores:** Revisa el porcentaje de carga y si tienen el GPS desactivado o están sin señal.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-indigo-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**Detección de Conducción &amp; Velocidad:** Identifica automáticamente cuando un familiar viaja en auto (🚗), mostrando su velocidad en vivo y alertando si supera el límite establecido.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-indigo-400 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**Historial de Rutas:** Revisa el camino recorrido por los integrantes de tu núcleo con estimaciones de velocidad y barra de tiempo.</span>
                    </li>
                  </ul>
                </div>
                <div className="mt-8 pt-6 border-t border-neutral-900 text-xs text-neutral-500">
                  Ideal para coordinar traslados y verificar rutas en tiempo real.
                </div>
              </div>

              {/* Pillar 3: Silent S.O.S */}
              <div className="p-8 lg:p-10 rounded-3xl bg-neutral-900/30 border border-neutral-900 flex flex-col justify-between hover:border-amber-500/20 transition-all duration-300 group">
                <div>
                  <div className="w-12 h-12 rounded-2xl bg-amber-950/40 border border-amber-500/20 flex items-center justify-center text-amber-500 mb-6 group-hover:bg-amber-900/20 transition-all">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                    </svg>
                  </div>
                  <h3 className="text-2xl font-bold text-white">3. S.O.S. de Emergencia (Crisis Activa)</h3>
                  <p className="text-neutral-400 mt-3 leading-relaxed">
                    Un botón de auxilio instantáneo para situaciones de peligro inminente. Activa alertas críticas inmediatas y un rastreo continuo de alta fidelidad.
                  </p>
                  
                  <ul className="space-y-3.5 mt-6 text-sm text-neutral-300">
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-amber-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**S.O.S. Silencioso de Emergencia:** Dispara alarmas prioritarias de forma imperceptible y discreta desde tu teléfono móvil.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-amber-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**Rastreo Crítico de Alta Frecuencia:** Al activarse, la tasa de actualización del GPS se acelera a cada 5 segundos para un seguimiento preciso.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-amber-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**Grabación Ambiental Automática:** Captura 15 segundos de audio de fondo de manera silenciosa para que tu familia escuche qué está sucediendo.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-amber-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**Detección de Accidentes por Acelerómetro:** Monitorea desaceleraciones extremas de colisión ($\ge 4.5$G) seguidas de inmovilidad física, iniciando una pre-alerta acústica de 15 segundos antes de alarmar a tus contactos.</span>
                    </li>
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-amber-500 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>**Canal de Respaldo por SMS:** Envía mensajes de texto de emergencia automáticos por red telefónica si pierdes conexión a internet.</span>
                    </li>
                  </ul>
                </div>
                <div className="mt-8 pt-6 border-t border-neutral-900 text-xs text-neutral-500">
                  Ideal para situaciones imprevistas, seguridad urbana y emergencias reales.
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
                      <h4 className="font-bold text-white text-sm">Mensajería Prioritaria vía WhatsApp &amp; SMS</h4>
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

              {/* Simulated Emergency Page on Phone */}
              <div className="relative mx-auto w-full max-w-[280px] bg-neutral-950 rounded-[40px] border-[6px] border-neutral-800 p-4 shadow-2xl">
                <div className="w-16 h-4 bg-neutral-800 rounded-full mx-auto mb-3"></div>
                <div className="text-center">
                  <div className="w-9 h-9 rounded-full bg-red-950 border border-red-500/30 flex items-center justify-center mx-auto text-red-500 animate-bounce mb-2">
                    ⚠️
                  </div>
                  <div className="text-[9px] font-black uppercase text-red-500 tracking-wider">Reporte Vencido</div>
                  <h4 className="text-xs font-bold text-white mt-0.5">Sofía Dovico no ha reportado</h4>
                  <p className="text-[8px] text-neutral-500">Último reporte hace 24 horas</p>
                </div>

                {/* Map preview */}
                <div className="w-full h-32 bg-neutral-900 rounded-xl my-4 relative border border-neutral-800 overflow-hidden">
                  <div className="absolute inset-0 bg-[radial-gradient(#ffffff03_1px,transparent_1px)] bg-[size:8px_8px]"></div>
                  <div className="absolute top-1/2 left-1/2 -translate-y-1/2 -translate-x-1/2 text-center">
                    <div className="px-1.5 py-0.5 rounded bg-red-600 text-white text-[7px] font-bold">Último Registro</div>
                    <div className="w-2.5 h-2.5 rounded-full bg-red-600 mx-auto mt-0.5 ring-4 ring-red-600/30"></div>
                  </div>
                </div>

                {/* Interaction Feedback buttons */}
                <div className="space-y-2">
                  <div className="text-[9px] font-bold text-neutral-400 text-center uppercase tracking-wide">Respuestas de apoyo</div>
                  <div className="grid grid-cols-2 gap-2">
                    <button className="bg-red-600 text-white text-[8px] font-extrabold py-2.5 rounded-lg active:scale-95 transition-all">
                      VOY EN CAMINO
                    </button>
                    <button className="bg-neutral-850 text-neutral-200 text-[8px] font-extrabold py-2.5 rounded-lg active:scale-95 transition-all">
                      RECIBIDO
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Pricing Plan Section (Interactive) */}
        <section id="pricing" className="py-24 px-6 bg-neutral-950 border-t border-neutral-900 relative">
          <div className="absolute bottom-0 right-1/4 w-96 h-96 rounded-full bg-red-600/5 blur-[150px] pointer-events-none"></div>

          <div className="container px-4 mx-auto max-w-5xl text-center">
            <h2 className="text-3xl font-extrabold sm:text-5xl">Planes diseñados para tu tranquilidad</h2>
            <p className="text-neutral-400 mt-4 md:text-lg max-w-2xl mx-auto">
              Empieza hoy a cuidar de tu familia. Prueba todas las funciones avanzadas gratis por 7 días.
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
                      <span>Check-in fijo cada 24 horas</span>
                    </li>
                    <li className="flex items-center gap-2.5">
                      <svg className="w-4 h-4 text-green-500 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <span>Alertas por Email y Push</span>
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
                  RECOMENDADO
                </div>
                
                <div>
                  <h3 className="text-lg font-bold text-red-400 mt-2">Premium PRO</h3>
                  <div className="text-5xl font-black text-white mt-4 flex items-baseline gap-1 font-sans">
                    $4.99<span className="text-xs text-neutral-400 font-medium">/ mes</span>
                  </div>
                  <p className="text-xs text-red-500/80 mt-2 font-medium">La tranquilidad absoluta de tu núcleo familiar en piloto automático.</p>

                  <ul className="space-y-3.5 mt-8 text-sm text-neutral-300">
                    <li className="flex items-start gap-2.5">
                      <svg className="w-4 h-4 text-red-500 shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M5 13l4 4L19 7" />
                      </svg>
                      <div>
                        <span className="font-bold text-white block">Alertas por WhatsApp &amp; SMS</span>
                        <span className="text-xs text-neutral-400 block leading-relaxed">Alertas de inactividad o SOS enviadas de inmediato por canales de alta lectura, sin requerir que tengan internet o la app abierta.</span>
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
                        <span className="font-bold text-white block">Monitoreo de Sensores y Batería</span>
                        <span className="text-xs text-neutral-400 block leading-relaxed">Recibe notificaciones inmediatas si un miembro apaga su GPS, activa el Modo Avión, pierde señal o le queda menos de 15% de batería.</span>
                      </div>
                    </li>
                  </ul>
                </div>

                <Link
                  className="mt-8 text-center bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-500 hover:to-rose-500 text-white font-extrabold py-4 rounded-xl transition-all shadow-lg shadow-red-500/20 text-sm"
                  href="/register"
                >
                  Prueba Gratis de 7 Días
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
                  q: "¿Qué es y cómo funciona el botón de \"Estoy Ok\"?",
                  a: "Es un sistema de bienestar pasivo diseñado para proteger tu privacidad. Solo debes pulsar el botón una vez al día para confirmar que te encuentras bien y reiniciar tu plazo de seguridad. Si el plazo se vence y olvidas reportarte, el sistema despacha alertas automáticas prioritarias a todos tus contactos de emergencia."
                },
                {
                  q: "¿Cómo funciona el Auto-Check-in inteligente?",
                  a: "Es una característica exclusiva del Plan PRO. En lugar de presionar el botón manualmente, la app móvil de Estoy Ok utiliza sensores en segundo plano de manera silenciosa: detecta si tu celular se conecta a la red Wi-Fi segura de tu hogar o si registras más de 100 pasos en una hora mediante el podómetro integrado para confirmar tu bienestar de forma automática."
                },
                {
                  q: "¿Cómo funciona el S.O.S. Silencioso de Emergencia?",
                  a: "Es un botón de auxilio instantáneo para situaciones de peligro inmediato. Al presionarlo en la app móvil, se dispara de forma discreta una alerta crítica: la tasa de actualización de tu ubicación aumenta a cada 5 segundos y se graban 15 segundos de audio ambiente de fondo de forma imperceptible. Tu núcleo familiar recibe notificaciones prioritarias, SMS y WhatsApp (según el plan) con un enlace para ver tu ubicación y escuchar el audio."
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
                  a: "Utiliza el sensor del acelerómetro físico en teléfonos inteligentes para registrar desaceleraciones extremas o impactos severos (umbrales superiores a 4.5G) característicos de un accidente de tránsito. Si el sistema detecta un impacto y el dispositivo permanece inmóvil por 3 segundos (indicando una colisión), se inicia una pre-alerta de 15 segundos con un sonido fuerte de sirena. Si no se cancela pulsando 'Estoy bien', se activa de inmediato el protocolo de crisis máxima: se envía la ubicación GPS exacta y una grabación ambiental de audio a los familiares vía WhatsApp, SMS y notificaciones Push críticas."
                },
                {
                  q: "¿Qué pasa si mi celular se queda sin batería o no tiene señal?",
                  a: "Estoy Ok cuenta con prevención activa: detecta cuando tu batería baja del 15% y alerta a tus familiares. Además, el panel del núcleo diferencia en tiempo real si el dispositivo tiene el GPS apagado, el rastreo desactivado voluntariamente o si está sin señal de internet. Si el celular se apaga por completo y expira tu temporizador, el sistema despacha alertas prioritarias vía WhatsApp y SMS."
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
      </footer>
    </div>
  );
}
