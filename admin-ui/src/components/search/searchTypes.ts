import type { ReactNode } from 'react';

export type SearchBoxComponent = ReactNode;

export interface SearchContextProps {
  searchBox: SearchBoxComponent;
  setSearchBox: (component: SearchBoxComponent) => void;
}
