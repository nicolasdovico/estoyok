import Link from 'next/link';

export const metadata = {
  title: 'Política de Privacidad - Estoy Ok',
  description: 'Política de privacidad y protección de datos personales de la plataforma Estoy Ok.',
};

export default function PoliticaDePrivacidadPage() {
  return (
    <div className="flex flex-col min-h-screen bg-neutral-950 text-neutral-100 font-sans antialiased selection:bg-red-500 selection:text-white">
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
        <Link
          className="inline-flex h-10 items-center justify-center rounded-xl bg-neutral-900 border border-neutral-800 px-4 text-xs font-bold text-neutral-300 hover:text-white hover:bg-neutral-800 transition-all"
          href="/"
        >
          ← Volver a Inicio
        </Link>
      </header>

      <main className="flex-1 py-12 px-6 lg:px-12 max-w-4xl mx-auto w-full">
        {/* Badge & Title */}
        <div className="mb-10 text-center md:text-left border-b border-neutral-900 pb-8">
          <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-emerald-950/45 border border-emerald-500/20 text-xs font-semibold text-emerald-400 mb-4">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
            Transparencia y Protección de Datos
          </div>
          <h1 className="text-3xl md:text-5xl font-extrabold tracking-tight text-white mb-3">
            Política de Privacidad
          </h1>
          <p className="text-xs md:text-sm text-neutral-400">
            Última actualización: <span className="text-neutral-200 font-semibold">31 de Julio de 2026</span>
          </p>
        </div>

        {/* Introduction Banner */}
        <div className="p-6 rounded-2xl bg-neutral-900/60 border border-neutral-800 backdrop-blur-sm mb-10 leading-relaxed text-sm text-neutral-300">
          En <strong className="text-white">Estoy Ok</strong> (en adelante, &quot;la Aplicación&quot; o &quot;el Servicio&quot;), valoramos y respetamos profundamente la privacidad de nuestros usuarios. Esta Política de Privacidad explica de forma clara y transparente cómo recopilamos, utilizamos, almacenamos y protegemos la información personal y de geolocalización de los usuarios que utilizan nuestra aplicación móvil y plataforma web.
          <br /><br />
          Al descargar, instalar o utilizar <strong className="text-white">Estoy Ok</strong>, aceptas las prácticas descritas en esta política.
        </div>

        {/* Content Sections */}
        <div className="space-y-10 text-neutral-300 text-sm leading-relaxed">
          {/* Section 1 */}
          <section className="p-6 md:p-8 rounded-2xl bg-neutral-900/40 border border-neutral-900">
            <h2 className="text-xl font-bold text-white mb-4 flex items-center gap-2">
              <span className="flex items-center justify-center w-7 h-7 rounded-lg bg-red-500/10 text-red-400 text-xs font-black">1</span>
              Información que Recopilamos
            </h2>
            <p className="mb-4 text-neutral-400">
              Para brindar los servicios de protección familiar, geocercas y auto-checkin pasivo de bienestar, recopilamos los siguientes datos:
            </p>
            <ul className="space-y-3 pl-2">
              <li className="flex items-start gap-3">
                <span className="text-red-500 mt-0.5">•</span>
                <div>
                  <strong className="text-white">Información de Registro y Cuenta:</strong> Nombre y apellido, dirección de correo electrónico, fotografía de perfil / Avatar (opcional) y número de teléfono (opcional, para alertas por SMS/WhatsApp).
                </div>
              </li>
              <li className="flex items-start gap-3">
                <span className="text-red-500 mt-0.5">•</span>
                <div>
                  <strong className="text-white">Datos de Geolocalización (Primer y Segundo Plano):</strong> Coordenadas GPS (latitud y longitud), velocidad y precisión de ubicación.
                  <div className="mt-2 p-3.5 rounded-xl bg-neutral-950/80 border border-neutral-800 text-xs text-neutral-400">
                    <strong className="text-emerald-400 block mb-1">📡 Ubicación en Segundo Plano:</strong> Recopilamos la ubicación del dispositivo incluso cuando la aplicación está cerrada o no se está utilizando en pantalla, únicamente con la finalidad de:
                    <ul className="list-disc pl-4 mt-1.5 space-y-1">
                      <li>Detectar entradas y salidas automáticas de Perímetros Seguros (Geocercas).</li>
                      <li>Procesar el Auto-Check-in Pasivo por Wi-Fi Seguro.</li>
                      <li>Calcular alertas de inactividad de bienestar y telemetría de impacto/SOS en emergencias.</li>
                    </ul>
                  </div>
                </div>
              </li>
              <li className="flex items-start gap-3">
                <span className="text-red-500 mt-0.5">•</span>
                <div>
                  <strong className="text-white">Información de Red e Identificadores de Dispositivo:</strong> Nombre de la red Wi-Fi conectada (SSID) para la función de Wi-Fi Seguro, Token de notificaciones Push (Expo / Firebase Cloud Messaging) y estado de batería del dispositivo para alertas de batería baja a familiares.
                </div>
              </li>
              <li className="flex items-start gap-3">
                <span className="text-red-500 mt-0.5">•</span>
                <div>
                  <strong className="text-white">Contactos de Emergencia y Círculos Familiares:</strong> Datos de los contactos de emergencia y miembros de círculos familiares añadidos voluntariamente por el usuario.
                </div>
              </li>
            </ul>
          </section>

          {/* Section 2 */}
          <section className="p-6 md:p-8 rounded-2xl bg-neutral-900/40 border border-neutral-900">
            <h2 className="text-xl font-bold text-white mb-4 flex items-center gap-2">
              <span className="flex items-center justify-center w-7 h-7 rounded-lg bg-red-500/10 text-red-400 text-xs font-black">2</span>
              Uso de la Información
            </h2>
            <p className="mb-3 text-neutral-400">
              Utilizamos la información recopilada exclusivamente para los siguientes fines:
            </p>
            <ul className="grid md:grid-cols-2 gap-3">
              <li className="p-3.5 rounded-xl bg-neutral-950/60 border border-neutral-850 text-xs">
                🎯 Visualización en tiempo real de los miembros autorizados dentro del mapa de tu Círculo Familiar.
              </li>
              <li className="p-3.5 rounded-xl bg-neutral-950/60 border border-neutral-850 text-xs">
                🟢 Disparo automático de notificaciones de bienestar (Check-in diario y Auto-Check-in por Wi-Fi).
              </li>
              <li className="p-3.5 rounded-xl bg-neutral-950/60 border border-neutral-850 text-xs">
                🚨 Envío de alertas inmediatas a tus contactos de emergencia en caso de detección de impacto/caída o disparo SOS.
              </li>
              <li className="p-3.5 rounded-xl bg-neutral-950/60 border border-neutral-850 text-xs">
                🛡️ Notificaciones de entrada o salida de Perímetros Seguros configurados y administración segura de tu cuenta.
              </li>
            </ul>
          </section>

          {/* Section 3 */}
          <section className="p-6 md:p-8 rounded-2xl bg-gradient-to-b from-emerald-950/20 to-neutral-900/40 border border-emerald-500/20">
            <h2 className="text-xl font-bold text-white mb-4 flex items-center gap-2">
              <span className="flex items-center justify-center w-7 h-7 rounded-lg bg-emerald-500/10 text-emerald-400 text-xs font-black">3</span>
              Privacidad Absoluta: No Venta ni Monitoreo Administrativo
            </h2>
            <ul className="space-y-3">
              <li className="p-4 rounded-xl bg-neutral-950/80 border border-neutral-800 text-xs">
                <strong className="text-emerald-400 text-sm block mb-1">🚫 No Venta ni Alquiler a Terceros</strong>
                NUNCA venderemos, alquilaremos, cederemos ni comercializaremos tus datos personales, ubicación en tiempo real ni historial de trayectos a anunciantes, empresas de publicidad, <em>data brokers</em> ni terceros.
              </li>
              <li className="p-4 rounded-xl bg-neutral-950/80 border border-neutral-800 text-xs">
                <strong className="text-emerald-400 text-sm block mb-1">🔒 Sin Monitoreo Administrativo</strong>
                El personal técnico y los administradores de Estoy Ok NO monitorean, vigilan ni rastrean de forma individual la ubicación de los usuarios. El procesamiento de geolocalización es 100% automatizado por servidores seguros.
              </li>
              <li className="p-4 rounded-xl bg-neutral-950/80 border border-neutral-800 text-xs">
                <strong className="text-emerald-400 text-sm block mb-1">👨‍👩‍👧‍👦 Privacidad Scoped (Círculos Privados)</strong>
                Tu ubicación solo es visible para las personas que tú hayas aceptado explícitamente invitar o unirte dentro de un Círculo Familiar.
              </li>
            </ul>
          </section>

          {/* Section 4 */}
          <section className="p-6 md:p-8 rounded-2xl bg-neutral-900/40 border border-neutral-900">
            <h2 className="text-xl font-bold text-white mb-4 flex items-center gap-2">
              <span className="flex items-center justify-center w-7 h-7 rounded-lg bg-red-500/10 text-red-400 text-xs font-black">4</span>
              Almacenamiento y Seguridad de los Datos
            </h2>
            <div className="space-y-3">
              <p>
                <strong className="text-white">Cifrado en Tránsito y Reposo:</strong> Toda la comunicación entre la aplicación móvil y nuestros servidores se realiza mediante conexiones encriptadas <strong className="text-emerald-400">HTTPS (TLS/SSL)</strong>. Los datos almacenados cuentan con medidas de seguridad técnicas e infraestructurales avanzadas.
              </p>
              <p>
                <strong className="text-white">Retención de Datos:</strong> Los registros de ubicación histórica se conservan por un periodo máximo limitado (según el plan del usuario: 24 horas para cuentas gratuitas, 30 días para cuentas Premium) y posteriormente se eliminan de forma automatizada.
              </p>
            </div>
          </section>

          {/* Section 5 */}
          <section className="p-6 md:p-8 rounded-2xl bg-neutral-900/40 border border-neutral-900">
            <h2 className="text-xl font-bold text-white mb-4 flex items-center gap-2">
              <span className="flex items-center justify-center w-7 h-7 rounded-lg bg-red-500/10 text-red-400 text-xs font-black">5</span>
              Control de Permisos y Derechos del Usuario
            </h2>
            <ul className="space-y-2 text-neutral-400">
              <li>• <strong className="text-white">Desactivación de Rastreo:</strong> Puedes pausar o desactivar el permiso de ubicación en segundo plano en cualquier momento desde los Ajustes del teléfono o de la App.</li>
              <li>• <strong className="text-white">Derecho de Acceso y Rectificación:</strong> Puedes modificar o actualizar tus datos personales directamente desde tu perfil en la app.</li>
              <li>• <strong className="text-white">Eliminación de Cuenta y Datos:</strong> Tienes derecho a solicitar la eliminación permanente de tu cuenta y de todos tus datos asociados desde Ajustes -&gt; Eliminar Cuenta, o escribiendo a <a href="mailto:estoyok24@gmail.com" className="text-red-400 hover:underline">estoyok24@gmail.com</a>.</li>
            </ul>
          </section>

          {/* Section 6 */}
          <section className="p-6 md:p-8 rounded-2xl bg-neutral-900/40 border border-neutral-900">
            <h2 className="text-xl font-bold text-white mb-4 flex items-center gap-2">
              <span className="flex items-center justify-center w-7 h-7 rounded-lg bg-red-500/10 text-red-400 text-xs font-black">6</span>
              Servicios de Terceros
            </h2>
            <p className="text-neutral-400 mb-2">Utilizamos proveedores de infraestructura confiables que cumplen con estrictos estándares de seguridad:</p>
            <ul className="space-y-1.5 pl-4 list-disc text-xs text-neutral-300">
              <li><strong>Google Play Services / Apple CoreLocation:</strong> Para procesamiento del sensor GPS.</li>
              <li><strong>Firebase / Expo Push:</strong> Para entrega de notificaciones push de emergencia.</li>
              <li><strong>Procesadores de Pago (Stripe / Mercado Pago / PayPal):</strong> Para suscripciones Premium. Estoy Ok NO almacena números de tarjetas de crédito/débito en sus servidores.</li>
            </ul>
          </section>

          {/* Section 7 */}
          <section className="p-6 md:p-8 rounded-2xl bg-neutral-900/60 border border-neutral-800 text-center md:text-left">
            <h2 className="text-xl font-bold text-white mb-3 flex items-center gap-2 justify-center md:justify-start">
              <span className="flex items-center justify-center w-7 h-7 rounded-lg bg-red-500/10 text-red-400 text-xs font-black">7</span>
              Contacto y Consultas
            </h2>
            <p className="text-neutral-400 mb-4 text-xs">
              Si tienes preguntas, dudas o inquietudes sobre esta Política de Privacidad o el tratamiento de tus datos personales, puedes comunicarte con nuestro equipo en:
            </p>
            <div className="inline-flex flex-wrap gap-4 text-xs font-semibold">
              <a href="mailto:estoyok24@gmail.com" className="px-4 py-2 rounded-xl bg-neutral-950 border border-neutral-800 text-red-400 hover:border-red-500/40 transition-colors flex items-center gap-2">
                ✉️ estoyok24@gmail.com
              </a>
              <a href="https://instagram.com/estoyok24" target="_blank" rel="noopener noreferrer" className="px-4 py-2 rounded-xl bg-neutral-950 border border-neutral-800 text-pink-400 hover:border-pink-500/40 transition-colors flex items-center gap-2">
                📸 Instagram: @estoyok24
              </a>
            </div>
          </section>
        </div>
      </main>

      <footer className="py-8 border-t border-neutral-900 bg-neutral-950 text-center text-neutral-500 text-xs">
        <p>© 2026 ESTOY OK. Todos los derechos reservados.</p>
        <p className="mt-2 text-[10px] text-neutral-600">Protección integral y redundancia de comunicación para la seguridad de tu familia.</p>
      </footer>
    </div>
  );
}
