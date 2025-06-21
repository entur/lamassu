import { useState, useEffect, type RefObject } from 'react';

export function useContainerResponsiveView(
  containerRef: RefObject<HTMLDivElement | null>,
  threshold: number,
  isDisabled: boolean = false
): boolean {
  const [containerWidth, setContainerWidth] = useState(0);

  useEffect(() => {
    const element = containerRef.current;

    if (isDisabled || !element) {
      if (containerWidth !== 0) setContainerWidth(0);
      return;
    }

    const resizeObserver = new ResizeObserver(entries => {
      for (const entry of entries) {
        setContainerWidth(entry.contentRect.width);
      }
    });

    resizeObserver.observe(element);

    setContainerWidth(element.offsetWidth);

    return () => {
      if (element) {
        resizeObserver.unobserve(element);
      }
      resizeObserver.disconnect();
    };
  }, [containerRef, containerWidth, isDisabled, threshold]);

  return containerWidth > 0 && containerWidth < threshold;
}
