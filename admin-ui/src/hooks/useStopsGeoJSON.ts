import { useMemo } from 'react';
import { useStopPlaces } from '../data/useStopPlaces';
import { convertStopPlacesToGeoJSON, type GeoJSONFeatureCollection } from '../utils/geojsonUtils';

export function useStopsGeoJSON(): {
  geojson: GeoJSONFeatureCollection;
  loading: boolean;
  error: unknown;
} {
  const { allData, loading, error } = useStopPlaces();

  const geojson = useMemo<GeoJSONFeatureCollection>(() => {
    if (!allData || allData.length === 0) {
      return { type: 'FeatureCollection', features: [] };
    }
    return convertStopPlacesToGeoJSON(allData);
  }, [allData]);

  return { geojson, loading, error };
}
