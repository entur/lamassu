import { Map } from 'react-map-gl/maplibre';
import { type MapRef } from 'react-map-gl/maplibre';
import 'maplibre-gl/dist/maplibre-gl.css';
import { RegisterIcons } from '../../map/RegisterIcons.tsx';
import type { MapLibreEvent, StyleSpecification } from 'maplibre-gl';

interface MapContainerProps {
  mapStyle: string | StyleSpecification;
  onLoad: (e: MapLibreEvent) => void;
  mapRef: React.Ref<MapRef>;
  children?: React.ReactNode;
}

export function MapContainer({ mapStyle, onLoad, mapRef, children }: MapContainerProps) {
  return (
    <Map
      ref={mapRef}
      initialViewState={{ longitude: 10.0, latitude: 65.5, zoom: 4 }}
      mapStyle={mapStyle}
      onLoad={onLoad}
    >
      <RegisterIcons />
      {children}
    </Map>
  );
}
