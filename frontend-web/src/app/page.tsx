'use client';

import { useState, useEffect } from 'react';
import Link from "next/link";
import Dashboard from '@/components/Dashboard';

export default function Home() {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);

  useEffect(() => {
    // Verificar si hay un token en el localStorage
    const token = localStorage.getItem('auth_token');
    setIsAuthenticated(!!token);
  }, []);

  // Mientras verificamos la autenticación, mostramos un estado de carga simple
  if (isAuthenticated === null) {
    return <div className="min-h-screen bg-white flex items-center justify-center">
      <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-red-600"></div>
    </div>;
  }

  // Si está autenticado, mostrar el Dashboard
  if (isAuthenticated) {
    return <Dashboard />;
  }

  // Si no está autenticado, mostrar la Landing Page
  return (
    <div className="flex flex-col min-h-screen bg-white">
      {/* Navigation */}
      <header className="px-4 lg:px-6 h-16 flex items-center border-b border-gray-100">
        <Link className="flex items-center justify-center" href="/">
          <span className="text-2xl font-black text-red-600 tracking-tighter">ESTOY OK</span>
        </Link>
        <nav className="ml-auto flex gap-4 sm:gap-6">
          <Link className="text-sm font-medium hover:text-red-600 transition-colors" href="#features">
            Funciones
          </Link>
          <Link className="text-sm font-medium hover:text-red-600 transition-colors" href="#premium">
            Premium
          </Link>
          <Link className="text-sm font-medium hover:text-red-600 transition-colors" href="/login">
            Acceso
          </Link>
        </nav>
      </header>

      <main className="flex-1">
        {/* Hero Section */}
        <section className="w-full py-12 md:py-24 lg:py-32 xl:py-48 bg-gray-50">
          <div className="container px-4 md:px-6 mx-auto">
            <div className="flex flex-col items-center space-y-4 text-center">
              <div className="space-y-2">
                <h1 className="text-4xl font-bold tracking-tighter sm:text-5xl md:text-6xl lg:text-7xl/none text-gray-900">
                  Tu familia a salvo, <br />
                  <span className="text-red-600">siempre.</span>
                </h1>
                <p className="mx-auto max-w-[700px] text-gray-500 md:text-xl dark:text-gray-400 mt-4">
                  La única plataforma que combina monitoreo activo en tiempo real con un sistema de bienestar pasivo inteligente.
                </p>
              </div>
              <div className="space-x-4 mt-8">
                <Link
                  className="inline-flex h-12 items-center justify-center rounded-xl bg-red-600 px-8 text-sm font-bold text-white shadow transition-colors hover:bg-red-700 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-red-700 disabled:pointer-events-none disabled:opacity-50"
                  href="/register"
                >
                  Registrarse Gratis
                </Link>
                <Link
                  className="inline-flex h-12 items-center justify-center rounded-xl border border-gray-200 bg-white px-8 text-sm font-bold text-gray-900 shadow-sm transition-colors hover:bg-gray-100 hover:text-gray-900 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-gray-950 disabled:pointer-events-none disabled:opacity-50"
                  href="#features"
                >
                  Saber más
                </Link>
              </div>
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section id="features" className="w-full py-12 md:py-24 lg:py-32">
          <div className="container px-4 md:px-6 mx-auto">
            <div className="grid gap-12 lg:grid-cols-2 items-start">
              <div className="flex flex-col justify-center space-y-4">
                <div className="inline-block rounded-lg bg-red-100 px-3 py-1 text-sm font-bold text-red-600">
                  Monitoreo Pasivo
                </div>
                <h2 className="text-3xl font-bold tracking-tighter sm:text-4xl text-gray-900">
                  El botón &quot;Estoy Ok&quot;
                </h2>
                <p className="text-gray-500 md:text-lg/relaxed lg:text-base/relaxed xl:text-lg/relaxed">
                  Simple y efectivo. El usuario confirma su bienestar una vez al día con un solo botón. 
                  Si olvida hacerlo, el sistema alerta automáticamente a sus contactos de confianza.
                </p>
                <ul className="grid gap-2 py-4">
                  <li className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Check-in diario simplificado
                  </li>
                  <li className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Alertas automáticas por inactividad
                  </li>
                  <li className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Notificaciones críticas vía WhatsApp
                  </li>
                </ul>
              </div>
              <div className="flex flex-col justify-center space-y-4">
                <div className="inline-block rounded-lg bg-blue-100 px-3 py-1 text-sm font-bold text-blue-600">
                  Monitoreo Activo
                </div>
                <h2 className="text-3xl font-bold tracking-tighter sm:text-4xl text-gray-900">
                  Rastreo Inteligente
                </h2>
                <p className="text-gray-500 md:text-lg/relaxed lg:text-base/relaxed xl:text-lg/relaxed">
                  Ubicación en tiempo real y geovallas automáticas. Recibe avisos cuando tus seres 
                  queridos llegan a casa, la escuela o el trabajo.
                </p>
                <ul className="grid gap-2 py-4">
                  <li className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Mapa en tiempo real (estilo Life360)
                  </li>
                  <li className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Perímetros de seguridad (Geofencing)
                  </li>
                  <li className="flex items-center gap-2">
                    <svg className="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Historial de ubicaciones extendido
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </section>

        {/* Premium Section */}
        <section id="premium" className="w-full py-12 md:py-24 lg:py-32 bg-gray-900 text-white">
          <div className="container px-4 md:px-6 mx-auto">
            <div className="flex flex-col items-center justify-center space-y-4 text-center">
              <div className="space-y-2">
                <h2 className="text-3xl font-bold tracking-tighter sm:text-5xl">Pásate a Premium</h2>
                <p className="max-w-[900px] text-gray-400 md:text-xl/relaxed lg:text-base/relaxed xl:text-xl/relaxed">
                  Lleva la seguridad de tu familia al siguiente nivel con funciones avanzadas y alertas prioritarias.
                </p>
              </div>
            </div>
            <div className="mx-auto grid max-w-5xl items-start gap-8 py-12 lg:grid-cols-2">
              <div className="flex flex-col p-8 bg-gray-800 rounded-2xl shadow-xl border border-gray-700">
                <h3 className="text-xl font-bold mb-4">Plan Gratis</h3>
                <div className="text-4xl font-bold mb-6">$0</div>
                <ul className="space-y-3 text-gray-400 flex-1">
                  <li className="flex items-center gap-2">
                    <svg className="w-4 h-4 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Check-in cada 24 horas
                  </li>
                  <li className="flex items-center gap-2">
                    <svg className="w-4 h-4 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Alertas por Email y Push
                  </li>
                  <li className="flex items-center gap-2">
                    <svg className="w-4 h-4 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Mapa (48h de historial)
                  </li>
                </ul>
              </div>
              <div className="flex flex-col p-8 bg-white text-gray-900 rounded-2xl shadow-xl border-4 border-red-600 transform lg:scale-105">
                <div className="absolute top-0 right-0 bg-red-600 text-white text-xs font-bold px-3 py-1 rounded-bl-xl uppercase">
                  Recomendado
                </div>
                <h3 className="text-xl font-bold mb-4">Plan Premium</h3>
                <div className="text-4xl font-bold mb-2">PRO</div>
                <p className="text-sm text-gray-500 mb-6">Suscripción Mensual / Anual</p>
                <ul className="space-y-3 text-gray-600 flex-1">
                  <li className="flex items-center gap-2">
                    <svg className="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Ventanas de check-in editables
                  </li>
                  <li className="flex items-center gap-2">
                    <svg className="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Alertas críticas vía WhatsApp
                  </li>
                  <li className="flex items-center gap-2">
                    <svg className="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Mapa (30 días de historial)
                  </li>
                  <li className="flex items-center gap-2">
                    <svg className="w-4 h-4 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Geofencing ilimitado
                  </li>
                </ul>
                <button className="mt-8 bg-red-600 text-white font-bold py-4 rounded-xl hover:bg-red-700 transition-colors">
                  Prueba Gratis 7 días
                </button>
              </div>
            </div>
          </div>
        </section>
      </main>

      <footer className="flex flex-col gap-2 sm:flex-row py-6 w-full shrink-0 items-center px-4 md:px-6 border-t border-gray-100">
        <p className="text-xs text-gray-500">© 2026 ESTOY OK. Todos los derechos reservados.</p>
        <nav className="sm:ml-auto flex gap-4 sm:gap-6">
          <Link className="text-xs hover:underline underline-offset-4 text-gray-500" href="#">
            Términos de Servicio
          </Link>
          <Link className="text-xs hover:underline underline-offset-4 text-gray-500" href="#">
            Privacidad
          </Link>
        </nav>
      </footer>
    </div>
  );
}
