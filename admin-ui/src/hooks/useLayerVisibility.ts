import { useCallback, useEffect, useState } from 'react';

export function useLayerVisibility(layerIds: string[]) {
  const [visibility, setVisibility] = useState<Record<string, boolean>>(() => {
    try {
      const saved = JSON.parse(localStorage.getItem('mapLayerVisibility') || '{}');
      return Object.fromEntries(layerIds.map(id => [id, saved[id] ?? true]));
    } catch {
      return Object.fromEntries(layerIds.map(id => [id, true]));
    }
  });

  useEffect(() => {
    localStorage.setItem('mapLayerVisibility', JSON.stringify(visibility));
  }, [visibility]);

  const toggle = useCallback((id: string, value: boolean) => {
    setVisibility(v => ({ ...v, [id]: value }));
  }, []);

  return { visibility, toggle };
}
