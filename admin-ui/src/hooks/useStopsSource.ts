import { useEffect } from 'react';
import type { GeoJSONSource, Map as MaplibreMap } from 'maplibre-gl';
import type { GeoJSONFeatureCollection } from '../utils/geojsonUtils';

export function useStopsSource(
  rawMapRef: React.RefObject<MaplibreMap | null>,
  geojson: GeoJSONFeatureCollection,
  loading: boolean,
  error: unknown
) {
  useEffect(() => {
    if (loading || error) return;
    if (!geojson.features.length) return;

    const map = rawMapRef.current;
    if (!map) return;

    const source = map.getSource('stops') as GeoJSONSource | undefined;
    if (source) {
      source.setData(geojson);
    } else {
      console.warn('useStopsSource: "stops" source not found');
    }
  }, [rawMapRef, geojson, loading, error]);
}
