import React, { useState } from 'react';
import type { ReactNode } from 'react';
import type { SearchBoxComponent } from './searchTypes.ts';
import { SearchContext } from './SearchContextInstance.ts';

interface SearchProviderProps {
  children: ReactNode;
  initialSearchBox?: SearchBoxComponent;
}

export const SearchProvider: React.FC<SearchProviderProps> = ({
  children,
  initialSearchBox = null,
}) => {
  const [searchBox, setSearchBox] = useState<SearchBoxComponent>(initialSearchBox);

  return (
    <SearchContext.Provider value={{ searchBox, setSearchBox }}>{children}</SearchContext.Provider>
  );
};
