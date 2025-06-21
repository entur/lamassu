// src/components/map/LayerControl.tsx
import { Box, Stack, useTheme } from '@mui/material';
import { useMap } from 'react-map-gl/maplibre';
import { useTranslation } from 'react-i18next';
import { useEffect, useState, useMemo } from 'react';
import { useLayerVisibility } from '../../hooks/useLayerVisibility.ts';
import { LayerSwitch } from './LayerSwitch';
import LayersIcon from '@mui/icons-material/Layers';
import MapIcon from '@mui/icons-material/Map'; // Example icon

// Updated imports
import { MapControlUnit, RenderMapPanel, type PanelUIDefinition } from './MapControlUnit';

import {
  LAYER_ID_STOPS_CIRCLE,
  LAYER_ID_STOPS_ICON,
  LAYER_ID_STOPS_TEXT,
  LAYER_ID_OSM_RASTER,
} from '../../map/mapStyle';

const mapLayerStyleDefinitions = [
  { id: LAYER_ID_STOPS_CIRCLE, labelKey: 'map.layers.circles' },
  { id: LAYER_ID_STOPS_ICON, labelKey: 'map.layers.icons' },
  { id: LAYER_ID_STOPS_TEXT, labelKey: 'map.layers.text' },
  { id: LAYER_ID_OSM_RASTER, labelKey: 'map.layers.basemap' },
];

export function LayerControl() {
  const { current: mapRef } = useMap();
  const { t } = useTranslation();
  const theme = useTheme();

  const { visibility: layerVisibility, toggle: toggleLayer } = useLayerVisibility(
    mapLayerStyleDefinitions.map(l => l.id)
  );

  const [activePanelControlId, setActivePanelControlId] = useState<string | null>(null);

  // Define all your control panels here
  const controlPanelDefinitions = useMemo<PanelUIDefinition[]>(() => {
    const layerPanelContent = (
      <Stack spacing={1}>
        {mapLayerStyleDefinitions.map(({ id, labelKey }) => (
          <LayerSwitch
            key={id}
            id={id}
            label={t(labelKey, id)}
            checked={layerVisibility[id] ?? true}
            onChange={toggleLayer}
          />
        ))}
      </Stack>
    );

    return [
      {
        controlId: 'layers-panel-control', // Unique ID for this control unit
        icon: <LayersIcon />,
        panelTitle: t('map.layers.title', 'Map Layers'),
        panelContent: layerPanelContent,
        ariaLabelOpen: t('map.layers.toggleOpen', 'Open layer controls'),
        ariaLabelCloseButton: t('map.layers.toggleClose', 'Close layer controls'),
        panelMinWidth: 220,
      },
      {
        controlId: 'another-panel-control',
        icon: <MapIcon />,
        panelTitle: 'Another Panel',
        panelContent: <div>Content for another panel. This demonstrates the concept.</div>,
        ariaLabelOpen: 'Open another panel',
        ariaLabelCloseButton: 'Close another panel',
        panelMinWidth: 200,
      },
      // Add more panel definitions as needed
    ];
  }, [t, layerVisibility, toggleLayer]); // Ensure dependencies are correct

  // Effect for applying map layer visibility (specific to the layers panel)
  useEffect(() => {
    if (!mapRef || activePanelControlId !== 'layers-panel-control') return; // Only apply if layers panel is active
    const map = mapRef.getMap();
    const apply = () => {
      mapLayerStyleDefinitions.forEach(({ id }) => {
        const vis = layerVisibility[id] ? 'visible' : 'none';
        if (map.getLayer(id)) {
          if (map.getLayoutProperty(id, 'visibility') !== vis) {
            map.setLayoutProperty(id, 'visibility', vis);
          }
        }
      });
    };
    const handleStyleData = () => {
      if (map.isStyleLoaded()) apply();
    };
    map.on('styledata', handleStyleData);
    if (map.isStyleLoaded()) apply();
    return () => {
      map.off('styledata', handleStyleData);
    };
  }, [mapRef, layerVisibility, activePanelControlId]); // Re-run if active panel changes too

  const handleRequestPanelToggle = (controlId: string) => {
    setActivePanelControlId(prevId => (prevId === controlId ? null : controlId));
  };

  const currentActivePanelDef = controlPanelDefinitions.find(
    def => def.controlId === activePanelControlId
  );

  return (
    <Box // Main container for the entire control area (buttons + active panel)
      sx={{
        position: 'absolute',
        top: theme.spacing(1.5),
        right: theme.spacing(1.5),
        zIndex: 10, // zIndex for the whole control area
        display: 'flex', // Arrange button group and active panel horizontally
        alignItems: 'flex-start', // Align to the top
      }}
    >
      {/* Container for the vertical stack of buttons - this is what slides */}
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          gap: theme.spacing(1), // Space between buttons
          transition: theme.transitions.create(['transform'], {
            duration: theme.transitions.duration.short,
            easing: theme.transitions.easing.easeInOut,
          }),
          transform: theme.spacing(1),
          zIndex: theme.zIndex.drawer + 1, // Ensure buttons are above the panel area
        }}
      >
        {controlPanelDefinitions.map(definition => (
          <MapControlUnit
            key={definition.controlId}
            definition={definition}
            isActive={activePanelControlId === definition.controlId}
            onRequestToggle={handleRequestPanelToggle}
          />
        ))}
      </Box>

      {/* Area where the active panel's content will be rendered */}
      {currentActivePanelDef && (
        <Box
          sx={{
            marginLeft: theme.spacing(1), // This is the gap
            // Width is determined by the panel content itself
            // zIndex: theme.zIndex.drawer, // Panel is below buttons
          }}
        >
          <RenderMapPanel
            definition={currentActivePanelDef}
            isOpen={!!activePanelControlId} // True if any panel is active and matches this one
            onCloseRequest={() => setActivePanelControlId(null)} // Panel's own close button action
          />
        </Box>
      )}
    </Box>
  );
}
