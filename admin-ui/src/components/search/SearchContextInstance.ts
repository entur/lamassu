import { createContext } from 'react';
import type { SearchContextProps } from './searchTypes.ts';

export const SearchContext = createContext<SearchContextProps | undefined>(undefined);
