import { MetadataRoute } from 'next';

export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: '*',
      allow: '/',
      disallow: ['/api/', '/verify-email/', '/emergencia/'],
    },
    sitemap: 'https://estoyok24.com/sitemap.xml',
  };
}
