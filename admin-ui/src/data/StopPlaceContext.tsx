export type Name = {
  value: string;
};

export type StopPlace = {
  id: string;
  version: number;
  name: Name;
  geometry: {
    legacyCoordinates: [number, number][];
  };
  stopPlaceType: string;
  __typename: string;
};

export type StopPlaceData = {
  stopPlace: StopPlace[];
};

export type StopPlaceContext = {
  data: StopPlaceData;
};
