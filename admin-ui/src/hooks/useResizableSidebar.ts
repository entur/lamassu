import { useState, useEffect, useCallback } from 'react';

export function useResizableSidebar(initialWidth = 300) {
  const [width, setWidth] = useState<number>(initialWidth);
  const [isResizing, setIsResizing] = useState<boolean>(false);
  const [collapsed, setCollapsed] = useState<boolean>(false);

  const handleMouseMove = useCallback(
    (e: MouseEvent) => {
      if (!isResizing || collapsed) return;
      const newW = e.clientX;
      const min = 100;
      const max = window.innerWidth * 0.8;
      if (newW > min && newW < max) setWidth(newW);
    },
    [isResizing, collapsed]
  );

  const handleMouseUp = useCallback(() => setIsResizing(false), []);

  useEffect(() => {
    if (isResizing) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
      return () => {
        document.removeEventListener('mousemove', handleMouseMove);
        document.removeEventListener('mouseup', handleMouseUp);
      };
    }
  }, [isResizing, handleMouseMove, handleMouseUp]);

  const toggle = () => setCollapsed(prev => !prev);

  return {
    width,
    collapsed,
    isResizing,
    setIsResizing,
    toggle,
  };
}
