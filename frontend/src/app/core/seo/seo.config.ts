import { Lang } from '../../i18n/locale.util';
import { SeoConfig } from './seo.service';

/**
 * Tabla ruta → metadatos SEO en los dos idiomas. Las rutas privadas (área de profesor/alumno)
 * llevan `noindex`. Las rutas van sin prefijo; el SeoService añade `/es` cuando toca.
 */
type LocalizedSeo = Record<Lang, SeoConfig>;

const PUBLIC_SEO: Record<string, LocalizedSeo> = {
  '/': {
    en: {
      title: 'ChineseReads Academy — Tools for Chinese Teachers',
      description:
        'Organise your Chinese students, generate study plans, worksheets and dictations, and give every student premium access to ChineseReads. Built for private Mandarin teachers.',
      path: '/',
    },
    es: {
      title: 'ChineseReads Academy — Herramientas para profesores de chino',
      description:
        'Organiza a tus alumnos de chino, genera planes de estudio, fichas y dictados, y da a cada alumno acceso premium a ChineseReads. Pensado para profesores particulares de mandarín.',
      path: '/',
    },
  },
  '/para-profesores': {
    en: {
      title: 'For Chinese Teachers — Groups, Materials and Student Access | ChineseReads Academy',
      description:
        'What a private Chinese teacher gets with Academy: groups with invite codes, generated materials per group and ChineseReads premium for every active student.',
      path: '/para-profesores',
    },
    es: {
      title:
        'Para profesores de chino — Grupos, materiales y acceso de alumnos | ChineseReads Academy',
      description:
        'Qué obtiene un profesor particular de chino con Academy: grupos con código de invitación, materiales generados por grupo y premium en ChineseReads para cada alumno activo.',
      path: '/para-profesores',
    },
  },
  '/precios': {
    en: {
      title: 'Pricing — Yearly Teacher Plan | ChineseReads Academy',
      description:
        'One yearly plan for Chinese teachers with up to ten students included. Students never pay.',
      path: '/precios',
    },
    es: {
      title: 'Precios — Plan anual para profesores | ChineseReads Academy',
      description:
        'Un único plan anual para profesores de chino con hasta diez alumnos incluidos. Los alumnos nunca pagan.',
      path: '/precios',
    },
  },
  '/privacy-policy': {
    en: {
      title: 'Privacy Policy | ChineseReads Academy',
      description: 'How ChineseReads Academy handles personal data.',
      path: '/privacy-policy',
    },
    es: {
      title: 'Política de privacidad | ChineseReads Academy',
      description: 'Cómo trata ChineseReads Academy los datos personales.',
      path: '/privacy-policy',
    },
  },
  '/terms-of-use': {
    en: {
      title: 'Terms of Use | ChineseReads Academy',
      description: 'Terms of use of ChineseReads Academy.',
      path: '/terms-of-use',
    },
    es: {
      title: 'Términos de uso | ChineseReads Academy',
      description: 'Términos de uso de ChineseReads Academy.',
      path: '/terms-of-use',
    },
  },
};

const PRIVATE_SEO: LocalizedSeo = {
  en: {
    title: 'ChineseReads Academy',
    description: 'Private area of ChineseReads Academy.',
    noindex: true,
  },
  es: {
    title: 'ChineseReads Academy',
    description: 'Área privada de ChineseReads Academy.',
    noindex: true,
  },
};

const NOT_FOUND_SEO: LocalizedSeo = {
  en: {
    title: 'Page not found | ChineseReads Academy',
    description: 'The page you are looking for does not exist.',
    noindex: true,
  },
  es: {
    title: 'Página no encontrada | ChineseReads Academy',
    description: 'La página que buscas no existe.',
    noindex: true,
  },
};

export const PUBLIC_PATHS = Object.keys(PUBLIC_SEO);

/** Resuelve los metadatos para una ruta SIN prefijo de idioma. */
export function resolveSeo(path: string, lang: Lang): SeoConfig {
  const clean = (path.split('?')[0].split('#')[0] || '/').replace(/\/+$/, '') || '/';
  const found = PUBLIC_SEO[clean];
  if (found) return { ...found[lang], path: clean };
  if (clean.startsWith('/teacher') || clean.startsWith('/student')) {
    return { ...PRIVATE_SEO[lang], path: clean };
  }
  return { ...NOT_FOUND_SEO[lang], path: clean };
}
