import { createContext, useState, useContext, useEffect, type ReactNode, useCallback } from 'react';

const LOCAL_STORAGE_KEY = 'useCustomFeatures';

interface CustomizationContextType {
  useCustomFeatures: boolean;
  toggleCustomFeatures: () => void;
  setCustomFeatures: (enabled: boolean) => void;
}

const CustomizationContext = createContext<CustomizationContextType | undefined>(undefined);

export const CustomizationProvider = ({ children }: { children: ReactNode }) => {
  const [useCustomFeatures, setUseCustomFeaturesState] = useState<boolean>(() => {
    if (typeof window !== 'undefined' && window.localStorage) {
      const storedValue = localStorage.getItem(LOCAL_STORAGE_KEY);
      return storedValue ? JSON.parse(storedValue) : true;
    }
    return true;
  });

  useEffect(() => {
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(useCustomFeatures));
    }
  }, [useCustomFeatures]);

  const toggleCustomFeatures = useCallback(() => {
    setUseCustomFeaturesState(prev => !prev);
  }, []);

  const setCustomFeatures = useCallback((enabled: boolean) => {
    setUseCustomFeaturesState(enabled);
  }, []);

  return (
    <CustomizationContext.Provider
      value={{ useCustomFeatures, toggleCustomFeatures, setCustomFeatures }}
    >
      {children}
    </CustomizationContext.Provider>
  );
};

export const useCustomization = () => {
  const context = useContext(CustomizationContext);
  if (!context) {
    throw new Error('useCustomization must be used within a CustomizationProvider');
  }
  return context;
};
