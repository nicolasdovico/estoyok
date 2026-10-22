import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async redirects() {
    return [
      {
        source: "/:path*",
        has: [
          {
            type: "host",
            value: "www.estoyok24.com",
          },
        ],
        destination: "https://estoyok24.com/:path*",
        permanent: true,
      },
    ];
  },
};

export default nextConfig;
