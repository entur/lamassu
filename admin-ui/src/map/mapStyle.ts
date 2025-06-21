// src/map/mapStyle.ts

import type { StyleSpecification } from 'maplibre-gl';
import type { Theme } from '@mui/material/styles';
import type { LayerSpecification } from '@maplibre/maplibre-gl-style-spec';

// Define constants for layer IDs - good practice!
export const LAYER_ID_OSM_RASTER = 'osm-raster-tiles';
export const LAYER_ID_STOPS_CIRCLE = 'stops-circle';
export const LAYER_ID_STOPS_ICON = 'stops-icon';
export const LAYER_ID_STOPS_TEXT = 'stops-text';

export function createMapStyle(theme: Theme): StyleSpecification {
  // 1) Any existing base layers (e.g., OSM raster)
  const existingLayers: LayerSpecification[] = [
    {
      id: LAYER_ID_OSM_RASTER, // Use constant
      type: 'raster' as const,
      source: 'osm',
    },
  ];

  // 2) Stop‐place layers (dynamic, zoom‐dependent)
  // Split into three distinct layers
  const stopPlaceLayers: LayerSpecification[] = [
    // 2a. Circle layer (always visible, but radius interpolates with zoom)
    {
      id: LAYER_ID_STOPS_CIRCLE, // Use constant, renamed
      type: 'circle' as const,
      source: 'stops', // this must match your GeoJSON source ID
      paint: {
        // At zoom 0 → radius 1px; at zoom 8 → radius 3px; at zoom 16 → radius 8px
        'circle-radius': ['interpolate', ['linear'], ['zoom'], 0, 8, 8, 3, 16, 25],
        'circle-color':
          theme.palette.mode === 'dark' ? theme.palette.primary.light : theme.palette.primary.dark,
        'circle-stroke-width': 1,
        'circle-stroke-color':
          theme.palette.mode === 'dark'
            ? theme.palette.secondary.light
            : theme.palette.secondary.main,
        'circle-opacity': 0.6,
      },
      // Initial visibility is 'visible' by default, but can be set here if needed
      // layout: { visibility: 'visible' },
    },
    // 2b. Symbol (icon) — only at zoom ≥ 10 (as per original)
    {
      id: LAYER_ID_STOPS_ICON, // New layer for icons
      type: 'symbol' as const,
      source: 'stops', // same GeoJSON source
      minzoom: 10, // do not render icons below zoom 10
      layout: {
        // Use the feature’s “icon” property (registered by your RegisterIcons)
        'icon-image': ['get', 'icon'],

        // Interpolate icon size: at zoom 8 → 0.15, zoom 12 → 0.2, zoom 16 → 0.4 (as per original)
        'icon-size': ['interpolate', ['linear'], ['zoom'], 8, 0.15, 12, 0.2, 16, 0.4],

        'icon-allow-overlap': true,
        'icon-ignore-placement': true, // Allows icons to be placed even if they overlap

        // Ensure text-field and text-related layout properties are NOT here
        // Initial visibility is 'visible' by default
      },
      paint: {
        // Icon paint properties would go here if any (e.g., icon-color for SDF icons)
        // Initial opacity is 1 by default
      },
    },
    // 2c. Symbol (text) — only at zoom ≥ 10 (as per original)
    {
      id: LAYER_ID_STOPS_TEXT, // New layer for text labels
      type: 'symbol' as const,
      source: 'stops', // same GeoJSON source
      minzoom: 10, // do not render text below zoom 10
      layout: {
        // Ensure icon-image and icon-related layout properties are NOT here

        // Text label beneath each icon:
        'text-field': ['get', 'name'],
        'text-font': ['Open Sans Regular'], // Ensure this font is available via glyphs
        'text-offset': [0, 2.8], // Position below the icon
        'text-anchor': 'top',
        'text-size': 10,
        'text-allow-overlap': true, // Allows text to be placed even if they overlap
        'text-ignore-placement': true, // Allows text to be placed even if they overlap
        // Note: text-allow-overlap and text-ignore-placement can sometimes lead to cluttered maps.
        // Consider removing text-ignore-placement if performance or clutter is an issue.
      },
      paint: {
        'text-color':
          theme.palette.mode === 'dark' ? theme.palette.text.primary : theme.palette.text.primary,
        'text-halo-color':
          theme.palette.mode === 'dark'
            ? theme.palette.background.paper
            : theme.palette.background.paper,
        'text-halo-width': 6,
        // Keep your existing text opacity rule:
        'text-opacity': ['interpolate', ['linear'], ['zoom'], 13, 0, 13.01, 1],
      },
    },
  ];

  return {
    version: 8,
    name: 'Inanna Map Style (Zoom‐Dependent)',
    // Glyphs endpoint (remains unchanged)
    glyphs: theme.typography.fontFamily
      ? `https://fonts.openmaptiles.org/{fontstack}/{range}.pbf`
      : 'https://demotiles.maplibre.org/font/{fontstack}/{range}.pbf',

    sources: {
      // 1) Basemap (OpenStreetMap raster)
      osm: {
        type: 'raster',
        tiles: ['https://a.tile.openstreetmap.org/{z}/{x}/{y}.png'],
        tileSize: 256,
        attribution: '&copy; OpenStreetMap Contributors',
        maxzoom: 19,
      },
      // 2) Stops—initially empty; will be replaced/updated by your MapView via GeoJSON
      stops: {
        type: 'geojson',
        data: {
          type: 'FeatureCollection',
          features: [],
        },
      },
    },

    layers: [
      ...existingLayers,
      // Insert the stops layers in the desired order (circle below icon, icon below text)
      ...stopPlaceLayers,
    ],
  };
}
