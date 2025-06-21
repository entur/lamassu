const customPngModules = import.meta.glob('../static/customIcons/*.png', {
  eager: true,
  import: 'default',
}) as Record<string, string>;
const customSvgModules = import.meta.glob('../static/customIcons/*.svg', {
  eager: true,
  import: 'default',
}) as Record<string, string>;
const defaultPngModules = import.meta.glob('../static/defaultIcons/*.png', {
  eager: true,
  import: 'default',
}) as Record<string, string>;
const defaultSvgModules = import.meta.glob('../static/defaultIcons/*.svg', {
  eager: true,
  import: 'default',
}) as Record<string, string>;

function findByName(modules: Record<string, string>, name: string): string | undefined {
  const match = Object.entries(modules).find(([key]) => {
    const path = key.split('?')[0];
    return path.endsWith(`/${name}`);
  });
  return match ? match[1] : undefined;
}

const CUSTOM_FEATURES_LOCAL_STORAGE_KEY = 'useCustomFeatures';
function areCustomFeaturesEnabled(): boolean {
  if (typeof window !== 'undefined' && window.localStorage) {
    const storedValue = localStorage.getItem(CUSTOM_FEATURES_LOCAL_STORAGE_KEY);
    return storedValue ? JSON.parse(storedValue) : true;
  }
  return true;
}

export function getIconUrl(name: string, excludeSVG: boolean = false): string {
  const svgName = `${name}.svg`;
  const pngName = `${name}.png`;
  const useCustom = areCustomFeaturesEnabled();
  if (useCustom) {
    if (!excludeSVG) {
      const customSvg = findByName(customSvgModules, svgName);
      if (customSvg) return customSvg;
    }

    const customPng = findByName(customPngModules, pngName);
    if (customPng) return customPng;
  }
  if (!excludeSVG) {
    const defaultSvg = findByName(defaultSvgModules, svgName);
    if (defaultSvg) return defaultSvg;
  }

  const defaultPng = findByName(defaultPngModules, pngName);
  if (defaultPng) return defaultPng;

  if (!excludeSVG) {
    const fallbackSvg = findByName(defaultSvgModules, 'default.svg');
    if (fallbackSvg) return fallbackSvg;
  }
  const fallbackPng = findByName(defaultPngModules, 'default.png');
  return fallbackPng ?? '';
}
