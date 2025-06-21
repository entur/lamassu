import { NavigationControl, GeolocateControl } from 'react-map-gl/maplibre';

export function MapControls() {
  return (
    <>
      <NavigationControl position="bottom-right" />
      <GeolocateControl position="bottom-right" />
    </>
  );
}
