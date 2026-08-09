import type { Metadata } from "next";
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

export const metadata: Metadata = {
  metadataBase: new URL("https://estoyok.com"),
  title: {
    default: "Estoy Ok - Protección Pasiva & Rastreo Satelital Familiar",
    template: "%s | Estoy Ok"
  },
  description: "Plataforma de asistencia familiar con Bienestar Pasivo (monitoreo invisible por Wi-Fi seguro/pasos sin rastreo 24/7) y Rastreo Activo en Tiempo Real, Zonas Seguras, telemetría vehicular y SOS de emergencia.",
  keywords: [
    "seguridad familiar",
    "monitoreo pasivo",
    "check-in diario",
    "rastreo gps familiar",
    "zonas seguras",
    "telemetria vehicular",
    "exceso de velocidad",
    "sos silencioso",
    "alertas whatsapp",
    "adultos mayores"
  ],
  authors: [{ name: "Estoy Ok Team" }],
  creator: "Estoy Ok",
  publisher: "Estoy Ok",
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
    title: "Estoy Ok - Protección Pasiva & Rastreo Satelital Familiar",
    description: "La primera plataforma de asistencia que combina Protección Pasiva Invisible con Rastreo GPS Activo en Tiempo Real.",
    url: "https://estoyok.com",
    siteName: "Estoy Ok",
    images: [
      {
        url: "/images/hero_mockup.jpg",
        width: 1200,
        height: 630,
        alt: "Estoy Ok App - Monitoreo de Seguridad Familiar",
      },
    ],
    locale: "es_ES",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: "Estoy Ok - Protección Pasiva & Rastreo Satelital Familiar",
    description: "Plataforma de seguridad familiar con monitoreo pasivo invisible y rastreo activo GPS.",
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
