import type { StopPlaceContext } from './StopPlaceContext.tsx';

let fetchedStopPlaces: StopPlaceContext | undefined = undefined;

export const fetchStopPlaces = async (): Promise<StopPlaceContext> => {
  const response = await fetch(`${import.meta.env.BASE_URL}stopPlaces.json`);
  fetchedStopPlaces = await response.json();

  return Object.assign({}, fetchedStopPlaces);
};
