import type { StopPlace } from '../data/StopPlaceContext';

export type GeoJSONFeature = {
  type: 'Feature';
  geometry: { type: 'Point'; coordinates: [number, number] };
  properties: {
    id: string;
    name: string;
    icon: string;
  };
};

export type GeoJSONFeatureCollection = {
  type: 'FeatureCollection';
  features: GeoJSONFeature[];
};

export function convertStopPlacesToGeoJSON(stopPlaces: StopPlace[]): GeoJSONFeatureCollection {
  const features = stopPlaces
    .map(sp => {
      const [lng, lat] = sp.geometry.legacyCoordinates?.[0] ?? ['0', '0'];

      let iconName: string;
      if (sp.__typename === 'ParentStopPlace') {
        iconName = 'parentStopPlace';
      } else if (sp.stopPlaceType) {
        iconName = sp.stopPlaceType;
      } else {
        iconName = 'default';
      }

      return {
        type: 'Feature' as const,
        geometry: {
          type: 'Point' as const,
          coordinates: [lng, lat] as [number, number],
        },
        properties: {
          id: sp.id,
          name: sp.name.value,
          icon: iconName,
          originalData: sp,
        },
      };
    })
    .filter(
      feature => !isNaN(feature.geometry.coordinates[0]) && !isNaN(feature.geometry.coordinates[1])
    );

  return {
    type: 'FeatureCollection',
    features: features,
  };
}
