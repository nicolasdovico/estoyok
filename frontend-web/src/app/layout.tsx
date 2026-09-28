import type { Metadata, Viewport } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const viewport: Viewport = {
  themeColor: "#09090b",
  colorScheme: "dark",
  width: "device-width",
  initialScale: 1,
};

export const metadata: Metadata = {
  metadataBase: new URL("https://estoyok24.com"),
  alternates: {
    canonical: "https://estoyok24.com",
  },
  verification: {
    google: "Pls-Ut8K7-0alukkT42v6nC4Ro1YzkKXKRBohn54Oiw",
  },
  title: {
    default: "Estoy Ok - Seguridad y Localizador Familiar GPS | Protección Activa y Pasiva",
    template: "%s | Estoy Ok"
  },
  description: "Plataforma integral de seguridad familiar y bienestar. Combina Bienestar Pasivo (check-in automático por Wi-Fi de casa o movimiento) con Rastreo GPS en tiempo real, Zonas Seguras, telemetría vehicular y botón SOS con WhatsApp.",
  keywords: [
    "seguridad familiar",
    "localizador familiar gps",
    "rastreo gps en tiempo real",
    "cuidado de adultos mayores",
    "check-in diario de bienestar",
    "zonas seguras y geocercas",
    "boton de panico familiar",
    "alertas de emergencia whatsapp",
    "deteccion de choques automotriz",
    "telemetria vehicular",
    "sos silencioso",
    "estoy ok app",
    "proteccion familiar activa y pasiva"
  ],
  authors: [{ name: "Estoy Ok", url: "https://estoyok24.com" }],
  creator: "Estoy Ok",
  publisher: "Estoy Ok",
  applicationName: "Estoy Ok",
  category: "Safety & Security",
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      "max-video-preview": -1,
      "max-image-preview": "large",
      "max-snippet": -1,
    },
  },
  openGraph: {
    title: "Estoy Ok - Seguridad y Localizador Familiar GPS | Protección Activa y Pasiva",
    description: "Plataforma de asistencia familiar con Bienestar Pasivo (monitoreo invisible sin invadir privacidad) y Rastreo Activo en Tiempo Real.",
    url: "https://estoyok24.com",
    siteName: "Estoy Ok",
    images: [
      {
        url: "/images/hero_mockup.jpg",
        width: 1200,
        height: 630,
        alt: "Estoy Ok - Plataforma de Seguridad y Localizador Familiar",
        type: "image/jpeg",
      },
    ],
    locale: "es_LA",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "Estoy Ok - Seguridad y Localizador Familiar GPS",
    description: "Plataforma de seguridad y asistencia familiar con Bienestar Pasivo y Rastreo Activo GPS.",
    images: ["/images/hero_mockup.jpg"],
  },
  icons: {
    icon: [
      { url: "/favicon.png", type: "image/png" },
      { url: "/logo-square.png", sizes: "192x192", type: "image/png" },
      { url: "/icon.png", sizes: "512x512", type: "image/png" }
    ],
    apple: [
      { url: "/apple.icon.png", sizes: "180x180", type: "image/png" }
    ]
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="es"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}
