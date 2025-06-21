import { useEffect } from 'react';
import { useMap } from 'react-map-gl/maplibre';
import { getIconUrl } from '../utils/iconLoaderUtils.ts';

const images = [
  { name: 'railStation' },
  { name: 'onstreetBus' },
  { name: 'liftStation' },
  { name: 'ferryStop' },
  { name: 'metroStation' },
  { name: 'onstreetTram' },
  { name: 'harbourPort' },
  { name: 'parentStopPlace' },
];

export function RegisterIcons() {
  const { current: mapRef } = useMap();

  useEffect(() => {
    if (!mapRef) return;
    const map = mapRef.getMap();

    const handleMapLoad = () => {
      images.forEach(async ({ name }) => {
        if (!map.hasImage(name)) {
          const response = await map.loadImage(getIconUrl(name, true));
          if (response.data) {
            map.addImage(name, response.data);
          }
        }
      });
    };

    if (map.isStyleLoaded()) {
      handleMapLoad();
    } else {
      map.once('load', handleMapLoad);
    }

    return () => {
      map.off('load', handleMapLoad);
    };
  }, [mapRef]);

  return null;
}
