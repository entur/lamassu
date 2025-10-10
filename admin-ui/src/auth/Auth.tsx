import { AuthProvider as OidcAuthProvider } from 'react-oidc-context';
import { useConfig } from '../contexts/ConfigContext.tsx';

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const { oidcConfig } = useConfig();

  return (
    <OidcAuthProvider
      {...oidcConfig!}
      onSigninCallback={() => {
        window.history.replaceState({}, document.title, window.location.pathname);
      }}
      redirect_uri={oidcConfig?.redirect_uri || window.location.origin + import.meta.env.BASE_URL}
    >
      {children}
    </OidcAuthProvider>
  );
};
