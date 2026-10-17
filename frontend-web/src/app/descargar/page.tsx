import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Descargar Estoy Ok - Localizador Familiar GPS",
  description:
    "Descargá gratis Estoy Ok en Google Play Store. Localizador familiar GPS en tiempo real, Zonas Seguras, check-in diario de bienestar y alertas SOS por WhatsApp.",
  alternates: {
    canonical: "https://estoyok24.com/descargar",
  },
  openGraph: {
    title: "Descargar Estoy Ok - Localizador Familiar GPS",
    description:
      "Protegé a tu familia en tiempo real: Zonas Seguras, botón Estoy OK y alertas SOS con audio directo a WhatsApp.",
    url: "https://estoyok24.com/descargar",
    siteName: "Estoy Ok",
    images: [
      {
        url: "/images/hero_mockup.jpg",
        width: 1200,
        height: 630,
        alt: "Estoy Ok - Localizador Familiar",
      },
    ],
    locale: "es_LA",
    type: "website",
  },
};

const PLAY_STORE_URL =
  "https://play.google.com/store/apps/details?id=com.estoyok.app";
const ANDROID_INTENT_URL =
  "intent://details?id=com.estoyok.app#Intent;scheme=market;package=com.android.vending;S.browser_fallback_url=https%3A%2F%2Fplay.google.com%2Fstore%2Fapps%2Fdetails%3Fid%3Dcom.estoyok.app;end";

export default function DescargarPage() {
  const redirectScript = `
    (function() {
      var ua = navigator.userAgent || "";
      var isBot = /facebookexternalhit|Facebot|meta-externalagent|Twitterbot|Googlebot|bingbot|crawler|spider/i.test(ua);
      if (isBot) return;
      var isAndroid = /Android/i.test(ua);
      if (isAndroid) {
        window.location.replace("${ANDROID_INTENT_URL}");
        setTimeout(function() {
          window.location.replace("${PLAY_STORE_URL}");
        }, 400);
      } else {
        window.location.replace("${PLAY_STORE_URL}");
      }
    })();
  `;

  return (
    <main className="min-h-screen bg-zinc-950 text-white flex flex-col items-center justify-center px-6 text-center">
      <script dangerouslySetInnerHTML={{ __html: redirectScript }} />
      <div className="max-w-md w-full bg-zinc-900/90 border border-emerald-500/30 rounded-3xl p-8 shadow-2xl flex flex-col items-center gap-5">
        <img
          src="/logo-square.png"
          alt="Estoy Ok"
          className="w-20 h-20 rounded-2xl shadow-lg"
        />
        <h1 className="text-2xl font-bold tracking-tight text-white">
          Abriendo Google Play Store...
        </h1>
        <p className="text-zinc-400 text-sm leading-relaxed">
          Te estamos redirigiendo a la ficha oficial de{" "}
          <span className="text-emerald-400 font-semibold">
            Estoy Ok: Localizador Familiar
          </span>{" "}
          para instalar la aplicación gratis.
        </p>
        <a
          href={PLAY_STORE_URL}
          className="w-full py-4 px-6 rounded-2xl bg-emerald-500 hover:bg-emerald-400 text-zinc-950 font-bold text-base transition shadow-lg shadow-emerald-500/20"
        >
          Descargar en Google Play
        </a>
      </div>
    </main>
  );
}
