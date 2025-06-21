import { useContext } from 'react';
import { SearchContext } from './SearchContextInstance.ts';

export const useSearch = () => {
  const context = useContext(SearchContext);
  if (!context) {
    throw new Error('useSearch must be used within a SearchProvider');
  }
  return context;
};
